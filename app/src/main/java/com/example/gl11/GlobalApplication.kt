package com.example.gl11

import android.app.Application
import com.example.gl11.entity.User

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
    var profile: User? = null
}