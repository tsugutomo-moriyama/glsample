package com.example.gl11.entity

data class User(
    val id:Long? = null,
    val name:String,
    val thumbnail:String,
    val follow:List<User>,
    val follower:List<User>,
    val block:List<User>,
    val updateAt: Long = System.currentTimeMillis()
) {
}