package org.ohosdev.hapviewerandroid.extensions

import android.util.Log
import java.io.File

fun String.isFileInSystemPath(): Boolean {
    paths.let {
        for (folder in it) {
            if (File(folder, this@isFileInSystemPath).isFile) {
                return true
            }
        }
    }
    return false
}

val paths by lazy {
    val pathRegex = Regex("[;:]")
    val pathEnv = System.getenv("PATH")
    pathEnv!!.split(pathRegex).dropLastWhile { it.isEmpty() }.toTypedArray()
}