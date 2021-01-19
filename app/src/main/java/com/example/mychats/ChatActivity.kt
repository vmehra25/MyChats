package com.example.mychats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

const val UID = "uid"
const val NAME = "name"
const val PHOTO = "photo"

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }


}