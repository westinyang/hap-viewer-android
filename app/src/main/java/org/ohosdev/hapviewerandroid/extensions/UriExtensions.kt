package org.ohosdev.hapviewerandroid.extensions

import android.Manifest
import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.text.isDigitsOnly
import org.ohosdev.hapviewerandroid.app.DIR_PATH_EXTERNAL_FILES
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "UriExtensions"

val Uri.isExternalStorageDocument get() = authority.equals("com.android.externalstorage.documents")
val Uri.isDownloadsDocument get() = authority.equals("com.android.providers.downloads.documents")
val Uri.isMediaDocument get() = authority.equals("com.android.providers.media.documents")


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
fun Uri.getFilePath(context: Context): String? {
    // DocumentProvider
    if (DocumentsContract.isDocumentUri(context, this)) {
        val docId = DocumentsContract.getDocumentId(this)
        if (isExternalStorageDocument) {
            val split = docId.split(":")
            val type = split[0]
            val relativePath = split[1]
            if ("primary".equals(type, true))
                return "${Environment.getExternalStorageDirectory().absolutePath}/$relativePath}"
            else
                TODO("handle non-primary volumes")
        } else if (isDownloadsDocument) {
            if (docId.isEmpty() || !docId.isDigitsOnly()) return null
            val contentUri =
                ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), docId.toLong())
            return context.contentResolver.getDataColumn(contentUri)
        } else if (isMediaDocument) {
            val split = docId.split(":")
            val contentUri: Uri = when (split[0]) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> return null
            }
            return context.contentResolver.getDataColumn(contentUri, "_id=?", arrayOf(split[1]))
        }
    } else if (SCHEME_CONTENT.equals(scheme, true)) {
        return context.contentResolver.getDataColumn(this)
    } else if (SCHEME_FILE.equals(scheme, true)) {
        return path
    }
    return null
}

/**
 * 把文件复制到沙盒目录
 *
 * 注意：仅支持 scheme 为 content 的 uri
 *
 * [android10以上 uri转file uri转真实路径](https://blog.csdn.net/jingzz1/article/details/106188462)
 *
 * @param destFile 目标文件，默认为 `缓存目录/external_files/{name}`
 * @throws IOException 当文件无法获取时抛出异常
 * @throws IllegalArgumentException 当uri不合法时抛出异常
 */
@Throws(IOException::class, IllegalArgumentException::class)
fun Uri.copyFileToPrivateDir(
    ctx: Context,
    name: String,
    destFile: File = File(ctx.autoCacheDir, "${DIR_PATH_EXTERNAL_FILES}/${name}")
): File {
    if (scheme != SCHEME_CONTENT) {
        throw IllegalArgumentException("Scheme '$scheme' is not supported.")
    }
    destFile.parentFile?.mkdirs()
    ctx.contentResolver.openInputStream(this).use { inputStream ->
        if (inputStream == null) throw IOException("Cannot open ${this@copyFileToPrivateDir} using contentResolver.openInputStream.")
        FileOutputStream(destFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return destFile
}

/**
 * 仅当有存储权限，且可以获取文件路径时返回原文件，否则返回临时文件
 *
 * @param context 上下文
 * @return 获取到的文件路径，或者是复制到的新文件路径
 */
fun Uri.getOrCopyFile(context: Context, name: String): File? {
    if (context.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
        runCatching {
            getFilePath(context)?.also { path ->
                File(path).also {
                    if (it.isFile && it.canRead()) {
                        return it
                    }
                }
            }
        }.onFailure {
            Log.w(TAG, "getOrCopyFile: Failed to get file path: $this: ${it.localizedMessage}\n${it.stackTrace}")
        }
    }
    runCatching {
        return copyFileToPrivateDir(context, name)
    }.onFailure { it.printStackTrace() }
    return null
}

/**
 * 当 scheme 为 file 时，获取 path 中的文件名
 * 当 scheme 为 content 时，使用 `context.contentResolve` 查询文件名
 * @throws IllegalArgumentException 当 scheme 不为 file 或 content 时抛出异常
 */
fun Uri.getFileName(context: Context) = when (scheme) {
    SCHEME_FILE -> path?.let { File(it).name }
    SCHEME_CONTENT -> context.contentResolver.getDocumentName(this)
    else -> throw IllegalArgumentException("Not support scheme: $this")
}