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

/**
 * 销毁 HapInfo，**删除文件**，回收图片。
 *
 * 注：仅当文件路径为缓存路径时才会删除文件。
 * */
fun HapInfo.destroy(context: Context) {
    icon?.recycle()
    if (hapFilePath != null) File(hapFilePath).deleteIfCache(context)
}

@Throws(RemoteException::class)
fun HapInfo.installToSelf(helper: ShizukuServiceHelper): ExecuteResult {
    if (!helper.isServiceBound) {
        throw RuntimeException("Shizuku not bound.")
    }
    val result =
        helper.service!!.execute(listOf("bm", "install", "-r", this.hapFilePath), null, null)
    Log.i(TAG, "installing hap: $result")
    return result
}

fun HapInfo.getTechDesc(context: Context): String? {
    // techList可能为空，所以应该加?。
    return if (techList?.isNotEmpty() == true) techList?.joinToString(context.getString(R.string.separator))
    else null
}
