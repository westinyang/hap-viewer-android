package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.ExecuteResult
import org.ohosdev.hapviewerandroid.util.helper.ShizukuServiceHelper
import java.io.File

private const val TAG = "HapInfoExtensions"

/**
 * 销毁 HapInfo，**删除文件**，回收图片。
 *
 * 注：仅当文件路径为缓存路径时才会删除文件。
 * */
fun HapInfo.destroy(context: Context) {
    icon?.also {
        if (!it.isRecycled){
            it.recycle()
        }
    }
    hapFilePath?.also {
        File(it).deleteIfCache(context)
    }
}

@Throws(RemoteException::class)
suspend fun HapInfo.installToSelf(helper: ShizukuServiceHelper) = withContext(Dispatchers.Default) {
    if (!helper.isServiceBound) {
        throw RuntimeException("Shizuku not bound.")
    }
    isInstalling = true
    val result: ExecuteResult = runCatching {
        helper.service!!.execute(listOf("bm", "install", "-r", this@installToSelf.hapFilePath), null, null).also {
            Log.i(TAG, "installed hap: $it")
        }
    }.getOrElse { ExecuteResult(1, it.localizedMessage, null) }
    isInstalling = false
    result
}

fun HapInfo.getTechDesc(context: Context) = techList.let {
    // techList可能为空
    if (!it.isNullOrEmpty()) it.joinToString(context.localisedSeparator) else null
}

/**
 * 获取版本名和版本号，格式为 `版本名 (版本号)`。
 * */
fun HapInfo.getVersionNameAndCode(unknownString: String) =
    if (versionCode != null || versionCode != null)
        "%s (%s)".format(versionName ?: unknownString, versionCode ?: unknownString)
    else null