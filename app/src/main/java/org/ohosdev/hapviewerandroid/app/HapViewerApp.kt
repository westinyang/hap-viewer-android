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
        FileUtil.del(File(cacheDir, DIR_PATH_EXTERNAL_FILES))
        externalCacheDirs.forEach { FileUtil.del(File(it, DIR_PATH_EXTERNAL_FILES)) }
    }

    companion object {
        lateinit var instance: HapViewerApp
    }
}