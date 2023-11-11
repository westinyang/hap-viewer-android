package org.ohosdev.hapviewerandroid.extensions

import android.app.Activity
import com.google.android.material.dialog.MaterialAlertDialogBuilder


fun Activity.newRequestPermissionDialogBuilder(
    permissionNames: Array<String>,
    functionNames: Array<String>,
    onRequest: () -> Unit
): MaterialAlertDialogBuilder {
    val permissionNamesText = permissionNames.joinToString {
        "${it.lowercase()} permission"
    }
    val functionNamesText = functionNames.joinToString {
        it.lowercase()
    }
    return MaterialAlertDialogBuilder(this)
        .setTitle("Permissions request")
        .setMessage("We need $permissionNamesText to make sure we can $functionNamesText.")
        .setPositiveButton(android.R.string.ok) { _, _ ->
            onRequest.invoke()
        }
        .setNegativeButton(android.R.string.no, null)
}

fun Activity.newShizukuRequestPermissionDialogBuilder(
    functionNames: Array<String>,
    onRequest: () -> Unit
): MaterialAlertDialogBuilder {
    return newRequestPermissionDialogBuilder(arrayOf("Shizuku"), functionNames, onRequest)
}