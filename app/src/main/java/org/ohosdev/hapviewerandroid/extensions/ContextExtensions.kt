package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.content.pm.PackageManager
import org.ohosdev.hapviewerandroid.app.HapViewerApp

val Context.thisApp get() = applicationContext as HapViewerApp

fun Context.isPermissionGranted(permission: String) =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED