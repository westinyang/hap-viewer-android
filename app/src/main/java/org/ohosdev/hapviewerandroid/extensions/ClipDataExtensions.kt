package org.ohosdev.hapviewerandroid.extensions

import android.content.ClipData
import android.net.Uri

/**
 * 获取 ClipData 内第一个 Uri
 * */
fun ClipData.getFirstUri(): Uri? {
    for (index in 0 until itemCount) {
        val item = getItemAt(index)
        if (item.uri != null) {
            return item.uri
        }
    }
    return null
}