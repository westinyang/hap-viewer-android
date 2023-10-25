package org.ohosdev.hapviewerandroid.app

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType

class AppPreference(private val context: Context) {

    companion object {
        const val KEY_THEME_TYPE = "pref_theme_type"
    }

    private val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context)

    var themeType: ThemeType
        get() = sharedPreference.getString(KEY_THEME_TYPE, ThemeType.HARMONY.value)!!.toThemeType()
        set(value) {
            sharedPreference.edit { this.putString(KEY_THEME_TYPE,value.value) }
        }

    enum class ThemeType(val value: String) {
        MATERIAL1("material1"),
        MATERIAL2("material2"),
        MATERIAL3("material3"),
        HARMONY("harmony")
    }
}

fun String.toThemeType(): ThemeType {
    return when (this) {
        ThemeType.MATERIAL1.value -> ThemeType.MATERIAL1
        ThemeType.MATERIAL2.value -> ThemeType.MATERIAL2
        ThemeType.MATERIAL3.value -> ThemeType.MATERIAL3
        ThemeType.HARMONY.value -> ThemeType.HARMONY
        else -> ThemeType.MATERIAL2
    }
}