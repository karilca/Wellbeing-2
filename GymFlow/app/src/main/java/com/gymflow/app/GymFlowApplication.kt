package com.gymflow.app

import android.app.Application

class GymFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: GymFlowApplication
            private set
    }
}