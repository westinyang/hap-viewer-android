package org.ohosdev.hapviewerandroid.ui.about

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.HtmlCompat
import com.onegravity.rteditor.RTEditorMovementMethod
import org.ohosdev.hapviewerandroid.BuildConfig
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.LICENSE_APP
import org.ohosdev.hapviewerandroid.app.URL_HOME_JESSE205
import org.ohosdev.hapviewerandroid.app.URL_HOME_WESTINYANG
import org.ohosdev.hapviewerandroid.app.URL_OPEN_SOURCE_LICENSES
import org.ohosdev.hapviewerandroid.app.URL_PRIVACY_POLICY
import org.ohosdev.hapviewerandroid.app.URL_REPOSITORY
import org.ohosdev.hapviewerandroid.extensions.contentMovementMethod
import org.ohosdev.hapviewerandroid.extensions.contentSelectable
import org.ohosdev.hapviewerandroid.extensions.localisedColon
import org.ohosdev.hapviewerandroid.extensions.localisedSeparator
import org.ohosdev.hapviewerandroid.extensions.openUrl
import org.ohosdev.hapviewerandroid.ui.common.dialog.AlertDialogBuilder

class AboutDialogBuilder(context: Context) : AlertDialogBuilder<AboutDialogBuilder>(context) {
    init {
        setTitle(R.string.about)
        val messageHtml = context.run {
            resources.openRawResource(R.raw.about).use { it.readBytes().decodeToString() }
                .replace(REGEX_KEY) {
                    when (it.groupValues[1]) {
                        KEY_NAME -> getString(R.string.app_name_full)
                        KEY_DESC -> getString(R.string.app_description)
                        KEY_INFO -> getInformationHtml()
                        else -> ""
                    }
                }
        }
        setMessage(HtmlCompat.fromHtml(messageHtml, HtmlCompat.FROM_HTML_MODE_LEGACY))
        setPositiveButton(android.R.string.ok, null)
        setNeutralButton(R.string.legal_submenu, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun create(): AlertDialog = super.create().apply {
        setOnShowListener {
            contentSelectable = true
            contentMovementMethod = RTEditorMovementMethod.getInstance()
            getButton(AlertDialog.BUTTON_NEUTRAL).let { button ->
                PopupMenu(context, button).apply {
                    inflate(R.menu.menu_legal)
                    setOnMenuItemClickListener {
                        val link = when (it.itemId) {
                            R.id.action_privacy_policy -> URL_PRIVACY_POLICY
                            R.id.action_open_source_licenses -> URL_OPEN_SOURCE_LICENSES
                            else -> throw NoSuchElementException("Unknown itemId: $it")
                        }
                        context.openUrl(link)
                        true
                    }
                    button.setOnTouchListener(dragToOpenListener)
                    button.setOnClickListener { show() }
                }
            }
        }
    }

    /**
     * 获取应用信息字符串
     *
     * ```txt
     * 开源许可：xxx
     * 应用版本：xxx
     * ...
     * ```
     * */
    private fun getInformationHtml() = context.run {
        val colon = localisedColon
        val builder = StringBuilder()
        // 版本号
        builder.append(getString(R.string.about_version))
        builder.append(colon)
        builder.append("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        builder.append(ELEMENT_BR)
        // 源代码
        builder.append(getString(R.string.about_source))
        builder.append(colon)
        builder.append(URL_REPOSITORY.let { "<a href=\"${it}\">${it}</a>" })
        builder.append(ELEMENT_BR)
        // 许可证
        builder.append(getString(R.string.about_license))
        builder.append(colon)
        builder.append(LICENSE_APP)
        builder.append(ELEMENT_BR)
        // 贡献者
        builder.append(getString(R.string.about_contributors))
        builder.append(colon)
        builder.append(getContributorsHtml())
        builder.toString()
    }


    private fun getContributorsHtml() =
        contributors.joinToString(context.localisedSeparator) { "<a href=\"${it.url}\">${it.name}</a>" }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_DESC = "description"
        const val KEY_INFO = "information"
        const val ELEMENT_BR = "<br/>"

        /**
         * 匹配{{xxx}}，用于替换字符串
         * */
        val REGEX_KEY = Regex("\\{\\{\\s*(\\S*)\\s*\\}\\}")
        val contributors = arrayOf(
            Person("westinyang", URL_HOME_WESTINYANG),
            Person("Jesse205", URL_HOME_JESSE205),
        )
    }

    data class Person(val name: String, val url: String)
}