package org.ohosdev.hapviewerandroid.extensions

import android.content.ClipDescription
import android.view.DragEvent

/**
 * 判断是否有非文字的 MIME，也就是非 [ClipDescription.MIMETYPE_TEXT_PLAIN] 的 MIME。
 * */
fun DragEvent.hasFileMime(): Boolean {
    for (index in 0 until clipDescription.mimeTypeCount) {
        if (clipDescription.getMimeType(index) != ClipDescription.MIMETYPE_TEXT_PLAIN) {
            return true
        }
    }
    return false
}