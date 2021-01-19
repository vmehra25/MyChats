package com.example.mychats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mychats.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*

const val UID = "uid"
const val NAME = "name"
const val PHOTO = "photo"

class ChatActivity : AppCompatActivity() {

    private val friendUid by lazy{
        intent.getStringExtra(UID)
    }

    private val friendName by lazy {
        intent.getStringExtra(NAME)
    }

    private val friendImageUrl by lazy {
        intent.getStringExtra(PHOTO)
    }

    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    private val database by lazy {
        FirebaseDatabase.getInstance()
    }

    lateinit var currentUser:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }


        nameTvChat.text = friendName
        Picasso.get().load(friendImageUrl).into(userImageViewChat)

    }


    private fun getMessagesReference(friendId: String): DatabaseReference {
        return database.reference.child("messages/${getMessagesId(friendId)}")
    }

    private fun getInboxReference(toUser:String, fromUser:String): DatabaseReference {
        return database.reference.child("chats/$toUser/$fromUser")
    }

    private fun getMessagesId(friendId:String):String{
        if(friendId > mCurrentUid){
            return mCurrentUid + friendId
        }
        return friendId + mCurrentUid
    }




}