package org.ohosdev.hapviewerandroid.util.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.getQuantityString
import org.ohosdev.hapviewerandroid.extensions.localisedSeparator

class RequestPermissionDialogBuilder(context: Context) :
    DialogBuilder<RequestPermissionDialogBuilder>(context) {
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

    fun setPermissionNames(names: Array<Int>) = apply {
        this.permissionNames = Array(names.size) { context.getString(names[it]) }
    }

    fun setFunctionNames(names: Array<String>) = apply {
        this.functionNames = names
    }

    fun setFunctionNames(names: Array<Int>) = apply {
        this.functionNames = Array(names.size) { context.getString(names[it]) }
    }

    fun setOnAgree(onAgree: () -> Unit) = apply {
        this.onAgree = onAgree
    }


    fun setAdditional(additional: Int) = apply {
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