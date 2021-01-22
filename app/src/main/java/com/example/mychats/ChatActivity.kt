package com.example.mychats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mychats.adapters.ChatAdapter
import com.example.mychats.models.*
import com.example.mychats.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
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

    private val messages = mutableListOf<ChatEvent>()
    lateinit var chatAdapter:ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)


        val emojiPop = EmojiPopup.Builder.fromRootView(rootView).build(messageEtChat)
        smileBtnChat.setOnClickListener{
            emojiPop.toggle()
        }

        toolbarChat.setNavigationOnClickListener {
            finish()
        }

        chatAdapter = ChatAdapter(messages, mCurrentUid)
        messageRvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
        listenToMessages()

        chatAdapter.likedFunction = { messageId: String, liked: Boolean ->
            updateLikes(messageId, liked)
        }


        Log.d("CHATS_ACTIVITY", "Started Activity")

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }


        nameTvChat.text = friendName
        Picasso.get().load(friendImageUrl).into(userImageViewChat)

        imageSentBtnChat.setOnClickListener {
            Log.d("CHATS_ACTIVITY", "sendBtnChat.setOnClickListener")
            messageEtChat.text?.let {
                if(it.isNotEmpty()){
                    Log.d("CHATS_ACTIVITY", "Started to send message")
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

    }

    private fun updateLikes(messageId: String, liked: Boolean) {
        getMessagesReference(friendUid!!).child(messageId).updateChildren(mapOf("liked" to liked))
    }

    private fun sendMessage(msg: String) {
        val id = getMessagesReference(friendUid!!).push().key
        checkNotNull(id) { "Id is null" }
        val msgMap = Message(msg, mCurrentUid, id)
        getMessagesReference(friendUid!!).child(id).setValue(msgMap).addOnSuccessListener {
            Log.d("CHATS_ACTIVITY", "completed")
        }.addOnFailureListener {
            Log.d("CHATS_ACTIVITY", it.localizedMessage)
        }

        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(msgMap: Message) {
        val inboxMap = Inbox(
                msgMap.message,
                friendUid!!,
                friendName!!,
                friendImageUrl!!,
                msgMap.sentAt,
                0
        )
        getInboxReference(mCurrentUid, friendUid!!).setValue(inboxMap)

        getInboxReference(friendUid!!, mCurrentUid).addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Inbox::class.java)
                inboxMap.apply {
                    from = msgMap.senderId
                    name = currentUser.name
                    image = currentUser.thumbImageUrl
                    count = 1
                }
                if(value?.from == msgMap.senderId){
                    inboxMap.count = value.count + 1
                }
                getInboxReference(friendUid!!, mCurrentUid).setValue(inboxMap)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun listenToMessages(){
        getMessagesReference(friendUid!!)
                .orderByKey()
                .addChildEventListener(object :ChildEventListener{
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val msg = snapshot.getValue(Message::class.java)
                        addMessage(msg!!)
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
    }

    private fun addMessage(msg: Message) {
        val eventBefore = messages.lastOrNull()
        if((eventBefore != null && !eventBefore.sentAt.isSameDayAs(msg.sentAt) ) || eventBefore == null){
            messages.add(
                    DateHeader(msg.sentAt, this)
            )
        }
        messages.add(msg)

        chatAdapter.notifyItemInserted(messages.size - 1)
        messageRvChat.scrollToPosition(messages.size - 1)

    }

    private fun markAsRead(){
        getInboxReference(friendUid!!, mCurrentUid).child("count").setValue(0)
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