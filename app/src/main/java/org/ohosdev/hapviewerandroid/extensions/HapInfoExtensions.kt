package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.os.RemoteException
import android.util.Log
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.ExecuteResult
import org.ohosdev.hapviewerandroid.util.helper.ShizukuServiceHelper
import java.io.File

private const val TAG = "HapInfoExtensions"

val HapInfo.init get() = fileStateFlags and HapInfo.FLAG_FILE_STATE_INIT != 0
var HapInfo.installing
    get() = fileStateFlags and HapInfo.FLAG_FILE_STATE_INSTALLING != 0
    set(value) {
        fileStateFlags = if (value)
            fileStateFlags or HapInfo.FLAG_FILE_STATE_INSTALLING
        else
            fileStateFlags and HapInfo.FLAG_FILE_STATE_INSTALLING.inv()
    }

/**
 * 销毁 HapInfo，**删除文件**，回收图片。
 *
 * 注：仅当文件路径为缓存路径时才会删除文件。
 * */
fun HapInfo.destroy(context: Context) {
    icon?.recycle()
    hapFilePath?.also {
        File(it).deleteIfCache(context)
    }
}

@Throws(RemoteException::class)
fun HapInfo.installToSelf(helper: ShizukuServiceHelper): ExecuteResult {
    if (!helper.isServiceBound) {
        throw RuntimeException("Shizuku not bound.")
    }
    installing = true
    val result = runCatching {
        return@runCatching helper.service!!.execute(
            listOf("bm", "install", "-r", this.hapFilePath), null, null
        ).also {
            Log.i(TAG, "installed hap: $it")
        }
    }.getOrElse { ExecuteResult(1, it.localizedMessage, null) }
    installing = false
    return result
}

fun HapInfo.getTechDesc(context: Context) = techList.let {
    // techList可能为空
    if (!it.isNullOrEmpty()) it.joinToString(context.localisedSeparator) else null
}

