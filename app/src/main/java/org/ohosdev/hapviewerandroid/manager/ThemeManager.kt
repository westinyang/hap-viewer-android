package org.ohosdev.hapviewerandroid.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.view.WindowCompat
import com.google.android.material.resources.MaterialAttributes.resolveBoolean
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.HARMONY
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL1
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL2
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL3
import org.ohosdev.hapviewerandroid.extensions.thisApp

/**
 * @author Jesse205
 * */
class ThemeManager(val context: Context) {

    private var themeType: ThemeType? = null
    private val preferenceThemeType get() = context.thisApp.appPreference.themeType

    fun applyTheme() {
        applyTheme(preferenceThemeType)
    }

    @SuppressLint("RestrictedApi")
    private fun applyTheme(themeType: ThemeType) {
        this.themeType = themeType
        val themeId: Int = when (themeType) {
            MATERIAL1 -> R.style.Theme_HapViewerAndroid
            MATERIAL2 -> R.style.Theme_HapViewerAndroid_Material2
            MATERIAL3 -> R.style.Theme_HapViewerAndroid_Material3
            HARMONY -> R.style.Theme_HapViewerAndroid_Harmony
        }
        context.setTheme(themeId)
        if (context is Activity) {
            context.window.also {
                WindowCompat.getInsetsController(it, it.decorView).apply {
                    isAppearanceLightNavigationBars =
                        resolveBoolean(context, R.attr.windowLightNavigationBar, false)
                    isAppearanceLightStatusBars =
                        resolveBoolean(context, R.attr.windowLightStatusBar, false)
                }
            }
        }
    }

    fun isThemeChanged(): Boolean {
        return isThemeChanged(preferenceThemeType)
    }

    fun isThemeChanged(themeType: ThemeType): Boolean {
        return (themeType != this.themeType).also {
            Log.i(TAG, "isThemeChanged: $it")
        }
    }

    companion object {
        private const val TAG = "ThemeManager"
    }

}