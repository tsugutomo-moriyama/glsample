package com.example.gl11.ui.home.view

import com.example.gl11.entity.DataType
import com.example.gl11.entity.User

class FilterDto(
    val type: DataType,
    val user: User? = null,
    var isSelected:Boolean = false
) {
}