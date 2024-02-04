package org.ohosdev.hapviewerandroid.extensions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentResultListener

fun AppCompatActivity.setFragmentResultListener(key: String, listener: (result: Bundle) -> Unit) {
    setFragmentResultListener(key) { _, result -> listener(result) }
}

fun AppCompatActivity.setFragmentResultListener(key: String, listener: FragmentResultListener) {
    supportFragmentManager.setFragmentResultListener(key, this, listener)
}