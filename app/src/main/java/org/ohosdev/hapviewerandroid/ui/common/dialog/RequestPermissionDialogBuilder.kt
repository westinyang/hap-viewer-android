package org.ohosdev.hapviewerandroid.ui.common.dialog

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.getQuantityString
import org.ohosdev.hapviewerandroid.extensions.localisedSeparator

class RequestPermissionDialogBuilder<T : RequestPermissionDialogBuilder<T>>(context: Context) :
    AlertDialogBuilder<T>(context) {
    private var permissionNames: Array<String> = arrayOf()
    private var functionNames: Array<String> = arrayOf()
    private var onAgree: (() -> Unit)? = null
    private var additional: String = ""

    init {
        setPositiveButton(android.R.string.ok) { _, _ ->
            onAgree?.invoke()
        }
        setNegativeButton(android.R.string.cancel, null)
    }

    fun setPermissionNames(names: Array<String>) = apply {
        this.permissionNames = names
    }

    fun setPermissionNames(names: IntArray) = apply {
        this.permissionNames = Array(names.size) { context.getString(names[it]) }
    }

    fun setFunctionNames(names: Array<String>) = apply {
        this.functionNames = names
    }

    fun setFunctionNames(names: IntArray) = apply {
        this.functionNames = Array(names.size) { context.getString(names[it]) }
    }

    fun setOnAgree(onAgree: () -> Unit) = apply {
        this.onAgree = onAgree
    }


    fun setAdditional(@StringRes additional: Int) = apply {
        this.additional = context.getString(additional)
    }


    override fun create(): AlertDialog {
        val separator = context.localisedSeparator
        val permissionNamesText = permissionNames.joinToString(separator = separator)
        val functionNamesText = functionNames.joinToString(separator = separator)
        setTitle(context.getQuantityString(R.plurals.permission_request, permissionNames.size))
        setMessage(
            context.getString(
                R.string.permission_request_message,
                permissionNamesText,
                functionNamesText,
                additional
            ).trim()
        )
        return super.create()
    }

}