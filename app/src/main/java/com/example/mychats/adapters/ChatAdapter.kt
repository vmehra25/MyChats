package com.example.mychats.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.mychats.R
import com.example.mychats.models.ChatEvent
import com.example.mychats.models.DateHeader
import com.example.mychats.models.Message
import com.example.mychats.utils.formatAsHeader
import com.example.mychats.utils.formatAsTime
import kotlinx.android.synthetic.main.list_item_chat_recieve_message.view.*
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.*
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.likeBtnChat
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.messageContent
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.messageTime
import kotlinx.android.synthetic.main.list_item_date_header.view.*

class ChatAdapter(private val list:MutableList<ChatEvent>, private val mCurrentUid:String):RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var likedFunction : ((String, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout:Int ->
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        }
        return when(viewType){
            TEXT_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recieve_message))
            }
            TEXT_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_message))
            }
            DATE_HEADER -> {
                DateViewHolder(inflate(R.layout.list_item_date_header))
            }
            else -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recieve_message))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = list[position]){
            is DateHeader -> {
                holder.itemView.textView.text = item.date
            }
            is Message -> {
                holder.itemView.apply {
                    messageContent.text = item.message
                    messageTime.text = item.sentAt.formatAsTime()
                }
                when(getItemViewType(position)){
                    TEXT_MESSAGE_RECEIVED -> {
                        holder.itemView.materialCardView2.setOnClickListener(object: DoubleClickListener() {
                            override fun onDoubleClick(view: View?) {
                                likedFunction?.invoke(item.messageId, !item.liked)
                            }
                        })
                        holder.itemView.likeBtnChat.setOnClickListener {
                            likedFunction?.invoke(item.messageId, !item.liked)
                        }
                        holder.itemView.likeBtnChat.apply {
                            isVisible = ((position == itemCount - 1) || item.liked)
                            isSelected = item.liked
                        }
                    }
                    TEXT_MESSAGE_SENT -> {
                        holder.itemView.likeBtnChat.apply {
                            isVisible = item.liked
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int): Int {
        return when(val event = list[position]){
            is Message -> {
                if(event.senderId == mCurrentUid){
                    TEXT_MESSAGE_SENT
                }else{
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is DateHeader -> {
                DATE_HEADER
            }
            else -> {
                UNSUPPORTED
            }
        }
    }





    class DateViewHolder(view:View):RecyclerView.ViewHolder(view)

    class MessageViewHolder(view:View):RecyclerView.ViewHolder(view)

    companion object{
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
    }



}

open abstract class DoubleClickListener:View.OnClickListener {
    var lastClickTime:Long = 0
    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if(clickTime - lastClickTime < DOUBLE_CLICK_TIME){
            onDoubleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onDoubleClick(view: View?)

    companion object{
        private const val DOUBLE_CLICK_TIME:Long = 300
    }
}
