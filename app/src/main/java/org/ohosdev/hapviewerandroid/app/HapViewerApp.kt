package org.ohosdev.hapviewerandroid.app

import android.app.Application
import cn.hutool.core.io.FileUtil
import java.io.File

class HapViewerApp : Application() {
    lateinit var appPreference: AppPreference

    override fun onCreate() {
        super.onCreate()
        instance = this
        appPreference = AppPreference(this)
        deleteExternalFilesCaches()
    }

    private fun deleteExternalFilesCaches() {
        File(cacheDir, DIR_PATH_EXTERNAL_FILES).deleteRecursively()
        externalCacheDirs.forEach { File(it, DIR_PATH_EXTERNAL_FILES).deleteRecursively() }
    }

    companion object {
        lateinit var instance: HapViewerApp
    }
}