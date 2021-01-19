package com.example.mychats

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.mychats.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_items.view.*

class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    fun bind(user: User, onClick:(name:String, photo:String, uid:String) -> Unit){
        with(itemView){
            countTv.isVisible = false
            timeTv.isVisible = false
            titleTv.text = user.name
            subTitleTv.text = user.status
            Picasso.get()
                    .load(user.imageUrl)
                    .placeholder(R.drawable.flag_india)
                    .error(R.drawable.flag_india)
                    .into(userImageView)

            setOnClickListener {
                onClick.invoke(user.name, user.thumbImageUrl, user.uid)
            }
        }
    }
}