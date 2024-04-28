package org.ohosdev.hapviewerandroid.extensions

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.material.resources.MaterialAttributes
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.HapViewerApp
import java.io.File

val Context.thisApp get() = applicationContext as HapViewerApp

/**
 * 自动获取缓存目录。如果外部存储可用，就使用外部存储缓存目录，否则获取内部缓存目录
 * @see Context.getExternalCacheDir
 * @see Context.getCacheDir
 * */
val Context.autoCacheDir: File get() = externalCacheDir ?: cacheDir

fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * 获取资源中的 `Bitmap`
 * @see BitmapFactory.decodeResource
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

/**
 * 本地化的分隔符，中文为”，“，英文为“, ”
 * */
val Context.localisedSeparator get() = getString(R.string.symbol_separator)

/**
 * 本地化的冒号，中文为”：“，英文为“: ”
 * */
val Context.localisedColon get() = getString(R.string.symbol_colon)

/**
 * 返回所提供属性 `attributeResId` 的布尔值，如果属性不是布尔值或不存在于当前主题中，则返回 `defaultValue`。
 * */
@SuppressLint("RestrictedApi")
fun Context.resolveBoolean(@AttrRes attributeResId: Int, defaultValue: Boolean) =
    MaterialAttributes.resolveBoolean(this, attributeResId, defaultValue)
