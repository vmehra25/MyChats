package com.example.mychats.auth

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mychats.MainActivity
import com.example.mychats.R
import com.example.mychats.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.ByteArrayOutputStream


class SignUpActivity : AppCompatActivity() {
    val storage by lazy{
        FirebaseStorage.getInstance()
    }
    val auth by lazy{
        FirebaseAuth.getInstance()
    }

    val db by lazy{
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl:String
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userImageInput.setOnClickListener{
            checkPermissionForImage()
        }

        btnSubmitProfileInfo.setOnClickListener {
            btnSubmitProfileInfo.isEnabled = false
            if(etName.text.isEmpty()){
                Toast.makeText(this, "Enter username", Toast.LENGTH_LONG).show()
            }else if(!::downloadUrl.isInitialized){
                Toast.makeText(this, "Choose picture", Toast.LENGTH_LONG).show()
            }else{
                val name = etName.text.toString()
                val user = User(name, downloadUrl, downloadUrl, auth.uid!!)
                db.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    btnSubmitProfileInfo.isEnabled = true
                }
            }
        }
    }

    private fun checkPermissionForImage() {
        if((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) &&
            (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){
            val read_permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val write_permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(
                    read_permission,
                    1001
            )
            requestPermissions(
                    write_permission,
                    1002
            )

        }else{
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, 1003)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 1003){
            data?.data.let {
                userImageInput.setImageURI(it)
                uploadImage(it!!)
            }
        }
    }

    private fun uploadImage(it: Uri){
        btnSubmitProfileInfo.isEnabled = false
        val ref = storage.reference.child("uploads/" + auth.uid.toString())

        val bmp = MediaStore.Images.Media.getBitmap(contentResolver, it)
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        val data: ByteArray = baos.toByteArray()
        val uploadTask = ref.putBytes(data)


        uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnFailureListener {
            if(::progressDialog.isInitialized){
                progressDialog.dismiss()
            }
            Toast.makeText(this@SignUpActivity, "Upload failed", Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener { task ->
            if(task.isSuccessful){
                btnSubmitProfileInfo.isEnabled = true
                downloadUrl = task.result.toString()
                Log.d("URL", "Download url: $downloadUrl")
                Toast.makeText(this@SignUpActivity, "Uploaded", Toast.LENGTH_SHORT).show()
            }

            if(::progressDialog.isInitialized){
                progressDialog.dismiss()
            }

        }
    }
}