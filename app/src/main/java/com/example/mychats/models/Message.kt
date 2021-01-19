package com.example.mychats.models

import android.content.Context
import java.util.*

interface ChatEvent{
    val sentAt:Date
}

data class Message(
    val message:String,
    val senderId:String,
    val messageId:String,
    val type:String = "TEXT",
    val status:Int = 1,
    val liked:Boolean = false,
    override val sentAt:Date = Date()
):ChatEvent{
    constructor():this("", "", "", "", 1, false, Date())

}

data class DateHeader(
    override val sentAt: Date,
    val context: Context
):ChatEvent{

}
