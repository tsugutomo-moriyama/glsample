package com.example.gl11.entity

data class Frame(
    val id:Long? = null,
    val type:Type,
    val memo:String,
    val updateAt: Long = System.currentTimeMillis()
){
    enum class Type{
        COLOR{},
        BMP{},
    }
}