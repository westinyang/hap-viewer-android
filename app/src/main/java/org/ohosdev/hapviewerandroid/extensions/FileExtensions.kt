package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.os.Build
import android.os.FileUtils
import androidx.annotation.ChecksSdkIntAtLeast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.FileChannel
import kotlin.io.copyTo as ktCopyTo

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val isSystemFileUtilsSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun FileChannel.copyTo(out: FileChannel) {
    val size = this.size()
    var left = size
    while (left > 0) {
        left -= this.transferTo(size - left, left, out)
    }
}

/**
 * 复制文件。
 *
 * Android 10 以上使用系统自带的方法，速度会有所提升。
 * */
fun FileInputStream.copyTo(out: FileOutputStream) {
    if (isSystemFileUtilsSupported) {
        FileUtils.copy(this.fd, out.fd)
    } else {
        channel.use { inputChannel ->
            out.channel.use { outputChannel ->
                inputChannel.copyTo(outputChannel)
            }
        }
    }
}

fun InputStream.copyTo(out: OutputStream) {
    if (isSystemFileUtilsSupported) {
        FileUtils.copy(this, out)
    } else if (this is FileInputStream && out is FileOutputStream) {
        copyTo(out)
    } else {
        ktCopyTo(out)
    }
}

fun File.copyTo(out: File) {
    FileInputStream(this).use { inputStream ->
        FileOutputStream(out).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}

/**
 * 判断文件是否位于缓存路径 [Context.getCacheDir]、[Context.getExternalCacheDirs] 下。
 * */
fun File.isInCache(context: Context) =
    startsWith(context.cacheDir) || context.externalCacheDirs.find { startsWith(it) } != null


fun File.deleteIfCache(context: Context) {
    if (isInCache(context)) {
        if (!delete()) {
            deleteOnExit()
        }
    }
}