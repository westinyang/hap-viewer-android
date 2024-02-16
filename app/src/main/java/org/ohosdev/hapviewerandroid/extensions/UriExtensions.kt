package org.ohosdev.hapviewerandroid.extensions

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

private const val TAG = "UriExtensions"

val Uri.isExternalStorageDocument get() = authority.equals("com.android.externalstorage.documents")
val Uri.isDownloadsDocument get() = authority.equals("com.android.providers.downloads.documents")
val Uri.isMediaDocument get() = authority.equals("com.android.providers.media.documents")

/**
 * - 当 `scheme` 为 `file` 并且 [Uri.getPath] 不为 `null` 时，使用 `DocumentFile.fromFile` 获取；
 * - 当 `scheme` 为 `content` 时，使用 `DocumentFile.fromSingleUri` 获取；
 * - 否则返回 `null`。
 * */
fun Uri.toDocumentFile(context: Context) = when (scheme) {
    SCHEME_FILE -> path?.let { DocumentFile.fromFile(File(it)) }
    SCHEME_CONTENT -> DocumentFile.fromSingleUri(context, this)!!
    else -> null
}

/**
 * 判断uri是否可以被读取
 *
 * 和 [DocumentFile.canRead] 不同的是，此方法不会检查 MimeType，因为部分应用不支持获取 MimeType。
 * */
fun Uri.canRead(context: Context) = when (scheme) {
    SCHEME_FILE -> path?.let { File(it).canRead() } ?: false
    SCHEME_CONTENT -> (context.checkCallingOrSelfUriPermission(this, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            == PackageManager.PERMISSION_GRANTED)

    else -> false
}