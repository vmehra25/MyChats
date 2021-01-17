package com.example.mychats.models

data class User(
        val name:String,
        val imageUrl:String,
        val thumbImageUrl:String,
        val uid:String,
        val deviceToken:String,
        val status:String,
        val onlineStatus:Long
) {
    // Empty Constructor for Firebase always required
    constructor(): this("", "", "", "", "", "", System.currentTimeMillis())

    constructor(name:String, imageUrl: String, thumbImageUrl: String, uid: String): this(
            name,
            imageUrl,
            thumbImageUrl,
            uid,
            "",
            "Hey there I'm using MyChats",
            System.currentTimeMillis()
    )

}