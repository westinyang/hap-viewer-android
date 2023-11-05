package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.ohosdev.hapviewerandroid.app.HapViewerApp

val Context.thisApp get() = applicationContext as HapViewerApp

fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED