package org.ohosdev.hapviewerandroid.extensions

import android.content.ClipDescription
import android.view.DragEvent

fun DragEvent.hasFileMime(): Boolean {
    for (index in 0 until clipDescription.mimeTypeCount) {
        if (clipDescription.getMimeType(index) != ClipDescription.MIMETYPE_TEXT_PLAIN) {
            return true
        }
    }
    return false
}