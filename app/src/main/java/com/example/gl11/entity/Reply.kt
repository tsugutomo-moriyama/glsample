package com.example.gl11.entity

data class Reply(
    val id:Long? = null,
    val contents:String,
    val user:User,
    val like:List<User>,
    val reply:List<Reply>,
    val updateAt: Long = System.currentTimeMillis()
){
}