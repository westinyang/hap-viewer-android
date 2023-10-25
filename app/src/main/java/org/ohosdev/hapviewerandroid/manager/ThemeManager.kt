package org.ohosdev.hapviewerandroid.manager

import android.content.Context
import android.util.Log
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.*
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

    private fun applyTheme(themeType: ThemeType) {
        this.themeType = themeType
        val themeId: Int = when (themeType) {
            MATERIAL1 -> R.style.Theme_HapViewerAndroid
            MATERIAL2 -> R.style.Theme_HapViewerAndroid_Material2
            MATERIAL3 -> R.style.Theme_HapViewerAndroid_Material3
            HARMONY -> R.style.Theme_HapViewerAndroid_Harmony
        }
        context.setTheme(themeId)
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