package org.ohosdev.hapviewerandroid.extensions

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import org.ohosdev.hapviewerandroid.app.DIR_PATH_EXTERNAL_FILES
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

val Uri.isExternalStorageDocument get() = authority.equals("com.android.externalstorage.documents")
val Uri.isDownloadsDocument get() = authority.equals("com.android.providers.downloads.documents")
val Uri.isMediaDocument get() = authority.equals("com.android.providers.media.documents")


/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param selection     (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
@Throws(IOException::class)
fun Uri.getDataColumn(
    context: Context,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    context.also {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = it.contentResolver.query(
                this, projection, selection, selectionArgs, null
            )
            if (cursor?.moveToFirst() == true) {
                val columnIndex = cursor.getColumnIndex(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
    }
    return null
}


/**
 * Get a file path from a Uri. This will get the the path for Storage Access
 * Framework Documents, as well as the _data field for the MediaStore and
 * other file-based ContentProviders.
 *
 * [Android使用系统文件管理器选择文件，并将Uri转换为File](https://blog.csdn.net/weixin_40255793/article/details/79496076)
 *
 * @param context The context.
 * @author paulburke
 */
@Throws(IOException::class)
fun Uri.getPath(context: Context): String? {
    // DocumentProvider
    if (DocumentsContract.isDocumentUri(context, this)) {
        if (isExternalStorageDocument) {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex())
            val type = split[0]
            if ("primary".equals(type, true))
                return "${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}"
            else
                TODO("handle non-primary volumes")
        } else if (isDownloadsDocument) {
            val id = DocumentsContract.getDocumentId(this)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), id.toLong()
            )
            return contentUri.getDataColumn(context, null, null)
        } else if (isMediaDocument) {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex())
            val contentUri = when (split[0]) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            } ?: return null

            return contentUri.getDataColumn(context, "_id=?", arrayOf(split[1]))
        }
    } else if ("content".equals(scheme, true)) {
        return getDataColumn(context, null, null)
    } else if ("file".equals(scheme, true)) {
        return path
    }
    return null
}

/**
 * Android 10+ Uri to File
 *
 * [android10以上 uri转file uri转真实路径](https://blog.csdn.net/jingzz1/article/details/106188462)
 *
 */
@Throws(IOException::class)
fun Uri.copyToPrivateFile(
    ctx: Context,
    name: String
): File {
    var file: File? = null
    if (scheme == ContentResolver.SCHEME_CONTENT) {
        // 把文件复制到沙盒目录
        val contentResolver = ctx.contentResolver
        val cacheDir = (ctx.externalCacheDir ?: ctx.cacheDir)
        file = File(cacheDir, "${DIR_PATH_EXTERNAL_FILES}/${name}")
        contentResolver.openInputStream(this).use { inputStream ->
            if (inputStream == null) throw IOException("Cannot open ${this@copyToPrivateFile} using contentResolver.openInputStream.")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
    if (file == null)
        throw IOException("file is null.")
    return file
}

/**
 * 仅当有存储权限，且可以获取文件路径时返回原文件，否则返回临时文件
 *
 * @param context 上下文
 * @return 获取到的文件路径，或者是复制到的新文件路径
 */
fun Uri.getOrCopyFile(context: Context, name: String): File? {
    if (context.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
        try {
            val path = getPath(context)
            if (path != null) {
                File(path).also {
                    if (it.isFile && it.canRead()) return it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    try {
        return copyToPrivateFile(context, name)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun Uri.getFileName(context: Context): String? {
    return when (scheme) {
        ContentResolver.SCHEME_FILE -> path?.let { File(it).name }
        ContentResolver.SCHEME_CONTENT -> DocumentFile.fromSingleUri(context, this)?.name
        else -> throw RuntimeException("Uri.getFileName: Not support scheme: $this")
    }
}