package com.example.gl11.entity

data class Account(
    val id:Long? = null,
    val email:String,
    val password:String,
    val updateAt: Long = System.currentTimeMillis()
){
}