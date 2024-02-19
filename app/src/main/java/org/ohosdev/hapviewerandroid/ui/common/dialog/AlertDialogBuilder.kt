package org.ohosdev.hapviewerandroid.ui.common.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import org.ohosdev.hapviewerandroid.extensions.fixDialogGravityIfNeeded

/**
 * 整个应用使用的对话框，主要添加对话框重力修正。
 * */
open class AlertDialogBuilder<T : AlertDialogBuilder<T>> : MaterialAlertDialogBuilderBridge<T> {
    constructor(context: Context) : super(context)
    constructor(context: Context, overrideThemeResId: Int) : super(context, overrideThemeResId)

    override fun create(): AlertDialog {
        return super.create().apply {
            fixDialogGravityIfNeeded()
        }
    }
}