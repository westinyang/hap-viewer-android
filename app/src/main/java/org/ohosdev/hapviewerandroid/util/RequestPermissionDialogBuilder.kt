package org.ohosdev.hapviewerandroid.util

import android.content.Context
import androidx.appcompat.app.AlertDialog

class RequestPermissionDialogBuilder(context: Context) :
    DialogBuilder<RequestPermissionDialogBuilder>(context) {
    private var permissionNames: Array<String> = arrayOf()
    private var functionNames: Array<String> = arrayOf()
    var onRequest: (() -> Unit)? = null

    init {
        setTitle("Permissions request")
        setPositiveButton(android.R.string.ok) { _, _ ->
            onRequest?.invoke()
        }
        setNegativeButton(android.R.string.no, null)
    }

    fun setPermissionNames(names: Array<String>): RequestPermissionDialogBuilder {
        this.permissionNames = names
        return this
    }

    fun setFunctionNames(names: Array<String>): RequestPermissionDialogBuilder {
        this.functionNames = names
        return this
    }

    fun setOnRequest(onRequest: () -> Unit): RequestPermissionDialogBuilder {
        this.onRequest = onRequest
        return this
    }

    override fun create(): AlertDialog {
        val permissionNamesText = permissionNames.joinToString {
            it.lowercase()
        }
        val functionNamesText = functionNames.joinToString {
            it.lowercase()
        }
        setMessage("We need $permissionNamesText to make sure $functionNamesText is working properly.")
        return super.create()
    }

}