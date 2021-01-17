package com.example.mychats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    val auth by lazy{
        FirebaseAuth.getInstance()
    }

    val db by lazy{
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(auth.currentUser == null){
            startActivity(  Intent(this, LoginActivity::class.java))
        }else{
            var docRef = db.collection("users").document(auth.uid!!)
            docRef.get()
                .addOnSuccessListener { document ->
                    if(document.exists()){
                        startActivity( Intent(this, MainActivity::class.java))
                    }else{
                        startActivity( Intent(this, SignUpActivity::class.java))
                    }
                }
                .addOnFailureListener {
                    Log.e("ERR", "Exception -> $it")
                }

        }
    }
}