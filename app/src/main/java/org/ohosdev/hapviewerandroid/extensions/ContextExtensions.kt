package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import org.ohosdev.hapviewerandroid.app.HapViewerApp

val Context.thisApp get() = applicationContext as HapViewerApp

fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * 获取资源中的 `Bitmap`
 * @return 如果图像不是 `Bitmap` 就返回 `null`
 * */
fun Context.getBitmap(@DrawableRes resId: Int): Bitmap? {
    val drawable = AppCompatResources.getDrawable(this, resId)
    return if (drawable is BitmapDrawable) drawable.bitmap else null
}

fun Context.openUrl(url: String) {
    val tabsIntent = CustomTabsIntent.Builder()
        .build()
    tabsIntent.launchUrl(this, Uri.parse(url))
}