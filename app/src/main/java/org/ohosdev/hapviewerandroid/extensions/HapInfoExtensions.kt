package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import org.ohosdev.hapviewerandroid.model.HapInfo
import java.io.File

/**
 * 销毁 HapInfo，**删除文件**，回收图片。
 *
 * 仅当文件路径为缓存路径时才会删除文件。
 * */
fun HapInfo.destroy(context: Context) {
    icon?.recycle()
    if (hapFilePath != null)
        File(hapFilePath).deleteIfCache(context)
}