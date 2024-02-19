package org.ohosdev.hapviewerandroid.ui.main

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.contentSelectable
import org.ohosdev.hapviewerandroid.extensions.ensureArguments
import org.ohosdev.hapviewerandroid.ui.common.dialog.AlertDialogBuilder
import org.ohosdev.hapviewerandroid.util.highlight.JSONHighlighter

class MoreInfoDialogFragment : DialogFragment() {
    private lateinit var htmlSpanned: CharSequence

    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val stringColor: String
        val numberColor: String
        val keywordColor: String
        requireContext().obtainStyledAttributes(
            intArrayOf(
                R.attr.codeColorString,
                R.attr.codeColorNumber,
                R.attr.codeColorKeyword
            )
        ).also {
            val noAlphaBlack = 0xff000000.toInt()
            fun getCodeColor(index: Int) =
                it.getColor(index, Color.RED).run { "#${(this - noAlphaBlack).toHexString()}" }
            stringColor = getCodeColor(0)
            numberColor = getCodeColor(1)
            keywordColor = getCodeColor(2)
        }.recycle()

        htmlSpanned = HtmlCompat.fromHtml(
            JSONHighlighter.highlight(
                arguments?.getString("info") ?: "",
                stringColor = stringColor,
                numberColor = numberColor,
                keywordColor = keywordColor
            ),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        return AlertDialogBuilder(requireContext())
            .setTitle(R.string.more_info)
            .setMessage(htmlSpanned)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            contentSelectable = true
        }
    }

    fun setInfoJson(info: CharSequence) = apply {
        ensureArguments()
        arguments?.putCharSequence("info", info)
    }

    companion object {
        const val TAG = "MoreInfoDialogFragment"
    }
}