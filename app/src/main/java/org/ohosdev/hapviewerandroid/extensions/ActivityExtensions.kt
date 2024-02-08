package org.ohosdev.hapviewerandroid.extensions

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentResultListener
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.ui.common.BaseActivity

fun FragmentActivity.setFragmentResultListener(key: String, listener: (result: Bundle) -> Unit) {
    setFragmentResultListener(key) { _, result -> listener(result) }
}

fun FragmentActivity.setFragmentResultListener(key: String, listener: FragmentResultListener) {
    supportFragmentManager.setFragmentResultListener(key, this, listener)
}

fun BaseActivity.copyAndShowSnackBar(text: String?, name: String? = null): Boolean {
    if (text.isNullOrEmpty()) return false
    copyText(text)
    showSnackBar(getString(R.string.copied_withName, name ?: text))
    return true
}