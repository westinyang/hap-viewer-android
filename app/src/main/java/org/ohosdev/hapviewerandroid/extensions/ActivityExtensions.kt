package org.ohosdev.hapviewerandroid.extensions

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentResultListener

fun FragmentActivity.setFragmentResultListener(key: String, listener: (result: Bundle) -> Unit) {
    setFragmentResultListener(key) { _, result -> listener(result) }
}

fun FragmentActivity.setFragmentResultListener(key: String, listener: FragmentResultListener) {
    supportFragmentManager.setFragmentResultListener(key, this, listener)
}