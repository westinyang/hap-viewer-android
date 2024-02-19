package org.ohosdev.hapviewerandroid.ui.common

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import org.ohosdev.hapviewerandroid.manager.ThemeManager
import rikka.insets.WindowInsetsHelper
import rikka.layoutinflater.view.LayoutInflaterFactory

abstract class BaseActivity : AppCompatActivity() {
    protected val themeManager: ThemeManager = ThemeManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.factory2 = LayoutInflaterFactory(delegate)
            .addOnViewCreatedListener(WindowInsetsHelper.LISTENER)
        super.onCreate(savedInstanceState)
        themeManager.applyTheme()
    }

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