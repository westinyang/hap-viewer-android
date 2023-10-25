package org.ohosdev.hapviewerandroid.extensions

import android.app.Dialog
import android.text.method.MovementMethod
import android.widget.TextView

val Dialog.messageView: TextView get() = findViewById(android.R.id.message)

var Dialog.contentSelectable
    get() = messageView.isTextSelectable
    set(value) {
        messageView.setTextIsSelectable(value)
    }

var Dialog.contentMovementMethod: MovementMethod
    get() = messageView.movementMethod
    set(value) {
        messageView.movementMethod = value
    }


fun Dialog.setContentAutoLinkMask(mask: Int) {
    messageView.apply {
        linksClickable = true
        autoLinkMask = mask
    }
}
