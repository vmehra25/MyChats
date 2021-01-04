package com.example.mychats

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlin.coroutines.Continuation

class SignUpActivity : AppCompatActivity() {
    val storage by lazy{
        FirebaseStorage.getInstance()
    }
    val auth by lazy{
        FirebaseAuth.getInstance()
    }
    lateinit var downloadUrl:String
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userImageInput.setOnClickListener{
            checkPermissionForImage()
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

    private fun uploadImage(it:Uri){
        btnSubmitProfileInfo.isEnabled = false
        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(it)
        uploadTask.addOnFailureListener {
            if(::progressDialog.isInitialized){
                progressDialog.dismiss()
            }
            Toast.makeText(this@SignUpActivity, "Upload failed", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            btnSubmitProfileInfo
            if(::progressDialog.isInitialized){
                progressDialog.dismiss()
            }
            Toast.makeText(this@SignUpActivity, "Uploaded", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener {
            val progress = 100.0 * it.bytesTransferred / it.totalByteCount
            progressDialog = createProgressDialog("Uploading: $progress", false)
            progressDialog.show()
        }
    }
}