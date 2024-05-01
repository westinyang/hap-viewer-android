package org.ohosdev.hapviewerandroid.harmonystyle.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet

@SuppressLint("RestrictedApi")
class AlertDialogLayout : androidx.appcompat.widget.AlertDialogLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
}