package org.ohosdev.hapviewerandroid.extensions

import android.app.Dialog
import android.text.method.MovementMethod
import android.view.Gravity
import android.widget.TextView
import org.ohosdev.hapviewerandroid.harmonystyle.R.attr.windowGravityBottom

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

/**
 * 鸿蒙风格将对话框的 Gravity 修正为底部
 * */
fun Dialog.fixDialogGravityIfNeeded() {
    context.theme.obtainStyledAttributes(intArrayOf(windowGravityBottom)).apply {
        if (getBoolean(0, false)) {
            window!!.setGravity(Gravity.BOTTOM)
        }
    }.recycle()
}