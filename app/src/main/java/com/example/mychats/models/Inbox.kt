package com.example.mychats.models

import java.util.*

data class Inbox(
        val message:String,
        var from:String,
        var name:String,
        var image:String,
        val time: Date,
        var count:Int = 0
){
    constructor(): this("", "", "", "", Date(), 0)

}
