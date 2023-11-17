package com.wegielek.signalychinese

import android.app.Application

class SignalyChineseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SignalyChineseApplication
            private set
    }

}