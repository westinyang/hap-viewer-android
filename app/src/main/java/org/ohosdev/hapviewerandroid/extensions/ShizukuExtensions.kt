package org.ohosdev.hapviewerandroid.extensions

import android.app.Activity
import android.content.pm.PackageManager
import org.ohosdev.hapviewerandroid.util.ShizukuUtil
import rikka.shizuku.Shizuku

val ShizukuUtil.ShizukuStatus.isGranted get() = this == ShizukuUtil.ShizukuStatus.GRANTED

fun Activity.requestShizukuPermission(requestCode: Int) {
    runCatching {
        Shizuku.requestPermission(requestCode)
    }.onFailure {
        it.printStackTrace()
        onRequestPermissionsResult(
            requestCode, arrayOf(ShizukuUtil.PERMISSION), intArrayOf(PackageManager.PERMISSION_DENIED)
        )
    }
}