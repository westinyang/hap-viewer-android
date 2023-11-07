package org.ohosdev.hapviewerandroid.app

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {

    abstract val rootView: CoordinatorLayout

    /**
     * 在屏幕上显示一个 SnackBar
     * */
    fun showSnackBar(@StringRes textId: Int): Snackbar {
        return showSnackBar(getString(textId))
    }

    open fun showSnackBar(text: String): Snackbar {
        return Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).apply { show() }
    }
}