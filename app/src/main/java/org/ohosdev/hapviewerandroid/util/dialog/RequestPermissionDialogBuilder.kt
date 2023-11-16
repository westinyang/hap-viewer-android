package org.ohosdev.hapviewerandroid.util.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.getQuantityString

class RequestPermissionDialogBuilder(context: Context) :
    DialogBuilder<RequestPermissionDialogBuilder>(context) {
    private var permissionNames: Array<String> = arrayOf()
    private var functionNames: Array<String> = arrayOf()
    private var onRequest: (() -> Unit)? = null

    init {
        setPositiveButton(android.R.string.ok) { _, _ ->
            onRequest?.invoke()
        }
        setNegativeButton(android.R.string.cancel, null)
    }

    fun setPermissionNames(names: Array<String>): RequestPermissionDialogBuilder {
        this.permissionNames = names
        return this
    }

    fun setPermissionNames(names: Array<Int>): RequestPermissionDialogBuilder {
        this.permissionNames = Array(names.size) { context.getString(names[it]) }
        return this
    }

    fun setFunctionNames(names: Array<String>): RequestPermissionDialogBuilder {
        this.functionNames = names
        return this
    }

    fun setFunctionNames(names: Array<Int>): RequestPermissionDialogBuilder {
        this.functionNames = Array(names.size) { context.getString(names[it]) }
        return this
    }

    fun setOnRequest(onRequest: () -> Unit): RequestPermissionDialogBuilder {
        this.onRequest = onRequest
        return this
    }

    override fun create(): AlertDialog {
        val separator = context.getText(R.string.separator)
        val permissionNamesText = permissionNames.joinToString()
        val functionNamesText = functionNames.joinToString(separator = separator)
        setTitle(context.getQuantityString(R.plurals.permission_request, permissionNames.size))
        setMessage(
            context.getQuantityString(
                R.plurals.permission_request_message, functionNames.size,
                permissionNamesText, functionNamesText
            )
        )
        return super.create()
    }

}