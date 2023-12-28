package org.ohosdev.hapviewerandroid.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import org.ohosdev.hapviewerandroid.app.HapViewerApp

val Context.thisApp get() = applicationContext as HapViewerApp

fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * 获取资源中的 `Bitmap`
 * */
fun Context.getBitmap(@DrawableRes resId: Int): Bitmap? {
    return BitmapFactory.decodeResource(resources, resId)
}

/**
 * 使用 CustomTabs 打开网页链接
 * */
fun Context.openUrl(url: String) {
    val tabsIntent = CustomTabsIntent.Builder()
        .build()
    tabsIntent.launchUrl(this, Uri.parse(url))
}


/**
 * 获取具有复数的字符串
 * @see Resources.getQuantityString
 * */
fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) =
    resources.getQuantityString(id, quantity, *formatArgs)

/**
 * 复制文字到剪贴板
 * */
fun Context.copyText(text: CharSequence) {
    val manager = getSystemService<ClipboardManager>()!!
    manager.setPrimaryClip(ClipData.newPlainText(null, text))
}