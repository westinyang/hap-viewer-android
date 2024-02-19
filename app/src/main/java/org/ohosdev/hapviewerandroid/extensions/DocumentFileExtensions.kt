package org.ohosdev.hapviewerandroid.extensions

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import cn.hutool.core.util.RandomUtil
import org.ohosdev.hapviewerandroid.app.DIR_PATH_EXTERNAL_FILES
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "DocumentFileExtensions"

/**
 * 获取文件的真实路径，可能会获取失败。
 *
 * - 如果 `uri` 的 `scheme` 为 `content`，则使用 [getDataColumn] 获取文件路径。
 * - 如果 `uri` 的 `scheme` 为 `file`，则使用 [Uri.getPath] 获取文件路径。
 *
 * [Android使用系统文件管理器选择文件，并将Uri转换为File](https://blog.csdn.net/weixin_40255793/article/details/79496076)
 *
 * @author paulburke
 * */
fun DocumentFile.getFilePath(context: Context): String? {
    val uri = uri
    val scheme = uri.scheme
    if (DocumentsContract.isDocumentUri(context, uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        if (uri.isExternalStorageDocument) {
            val split = docId.split(":")
            val (type, relativePath) = split
            if ("primary".equals(type, true))
                return "${Environment.getExternalStorageDirectory().absolutePath}/$relativePath"
            else {
                TODO("handle non-primary volumes")
            }
        } else if (uri.isDownloadsDocument) {
            if (docId.isEmpty() || !docId.isDigitsOnly()) return null
            val contentUri =
                ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), docId.toLong())
            return context.contentResolver.getDataColumn(contentUri)
        } else if (uri.isMediaDocument) {
            val (type, id) = docId.split(":")
            val contentUri: Uri = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> return null
            }
            return context.contentResolver.getDataColumn(contentUri, "_id=?", arrayOf(id))
        }
    } else if (ContentResolver.SCHEME_CONTENT == scheme) {
        return context.contentResolver.getDataColumn(uri)
    } else if (ContentResolver.SCHEME_FILE == scheme) {
        return uri.path
    }
    return null
}


/**
 * 仅当有存储权限，且可以获取文件路径时返回原文件，否则返回临时文件
 *
 * @param context 上下文
 * @return 获取到的文件路径，或者是复制到的新文件路径
 */
fun DocumentFile.getOrCopyFile(context: Context, name: String): File? {
    if (context.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
        runCatching {
            val path = getFilePath(context) ?: return@runCatching
            val file = File(path)
            if (file.isFile && file.canRead()) {
                return file
            }
            Log.i(TAG, "getOrCopyFile: got path, but cannot read: $file")
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
 * 把文件复制到沙盒目录
 *
 * **注意**：仅支持 scheme 为 content 的 uri，否则将抛出异常
 *
 * [android10以上 uri转file uri转真实路径](https://blog.csdn.net/jingzz1/article/details/106188462)
 *
 * @throws IOException 当文件无法获取时抛出异常
 */
@Throws(IOException::class)
fun DocumentFile.copyFileToPrivateDir(
    context: Context,
    name: String = getName() ?: RandomUtil.randomString(10)
): File {
    val destFile = File(context.autoCacheDir, "$DIR_PATH_EXTERNAL_FILES/${name}")
    destFile.parentFile?.mkdirs()
    context.contentResolver.openInputStream(uri).use { inputStream ->
        if (inputStream == null) {
            throw IOException("Cannot open ${this@copyFileToPrivateDir} using contentResolver.openInputStream.")
        }
        FileOutputStream(destFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return destFile
}