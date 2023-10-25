package org.ohosdev.hapviewerandroid.app

import android.app.Application

class HapViewerApp : Application() {
    lateinit var appPreference: AppPreference

    override fun onCreate() {
        super.onCreate()
        appPreference = AppPreference(this)
    }
}