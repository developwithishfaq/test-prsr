package com.down.adm_parser.interview.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MyBoundService : Service() {
    inner class MyBinder : Binder() {
        fun getService(): MyBoundService {
            return this@MyBoundService
        }
    }
    private val binder = MyBinder()

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }
}