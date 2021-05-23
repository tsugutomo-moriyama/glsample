package com.example.gl11.entity

data class Article(
    val id:Long? = null,
    val title:String,
    val contents:String,
    override val lat:Long,
    override val lon:Long,
    val user:User,
    val like:List<User>,
    val reply:List<Reply>,
    val frame:Frame,
    val fontColor:Int,
    val updateAt: Long = System.currentTimeMillis()
):Position(lat, lon){
}