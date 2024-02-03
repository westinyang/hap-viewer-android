package org.ohosdev.hapviewerandroid.fragment.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.ohosdev.hapviewerandroid.extensions.contentSelectable
import org.ohosdev.hapviewerandroid.extensions.ensureArguments
import org.ohosdev.hapviewerandroid.extensions.fixDialogGravityIfNeeded

class SimpleDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            arguments?.also { args ->
                val onClickListener = DialogInterface.OnClickListener { dialog, which ->
                    val requestKey = when (which) {
                        Dialog.BUTTON_POSITIVE -> "positiveButtonKey"
                        Dialog.BUTTON_NEGATIVE -> "negativeButtonKey"
                        Dialog.BUTTON_NEUTRAL -> "neutralButtonKey"
                        else -> return@OnClickListener
                    }
                    args.getString(requestKey)?.let {
                        setFragmentResult(it, Bundle())
                    }
                }

                args.getCharSequence("title")?.also { setTitle(it) }
                args.getCharSequence("message")?.also { setMessage(it) }
                args.getInt("titleId", 0).also { if (it != 0) setTitle(it) }
                args.getInt("messageId", 0).also { if (it != 0) setMessage(it) }
                args.getInt("positiveButtonId", 0).also {
                    if (it != 0) setPositiveButton(it, onClickListener)
                }
                args.getInt("negativeButtonId", 0).also {
                    if (it != 0) setNegativeButton(it, onClickListener)
                }
                args.getInt("neutralButtonId", 0).also {
                    if (it != 0) setNeutralButton(it, onClickListener)
                }
            }


        }.create().apply {
            fixDialogGravityIfNeeded()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            arguments?.also {
                contentSelectable = it.getBoolean("selectable", false)
            }
        }
    }

    fun setTitle(title: CharSequence) = apply {
        ensureArguments()
        arguments?.putCharSequence("title", title)
    }

    fun setTitle(@StringRes titleId: Int) = apply {
        ensureArguments()
        arguments?.putInt("titleId", titleId)
    }

    fun setMessage(message: CharSequence) = apply {
        ensureArguments()
        arguments?.putCharSequence("message", message)
    }

    fun setMessage(@StringRes messageId: Int) = apply {
        ensureArguments()
        arguments?.putInt("messageId", messageId)
    }

    fun setSelectable(selectable: Boolean) = apply {
        ensureArguments()
        arguments?.putBoolean("selectable", selectable)
    }

    fun setPositiveButton(@StringRes textId: Int, requestKey: String?) = apply {
        ensureArguments()
        arguments?.putInt("positiveButtonId", textId)
        arguments?.putString("positiveButtonKey", requestKey)
    }

    fun setNegativeButton(@StringRes textId: Int, requestKey: String?) = apply {
        ensureArguments()
        arguments?.putInt("negativeButtonId", textId)
        arguments?.putString("negativeButtonKey", requestKey)
    }

    fun setNeutralButton(@StringRes textId: Int, requestKey: String?) = apply {
        ensureArguments()
        arguments?.putInt("neutralButtonId", textId)
        arguments?.putString("neutralButtonKey", requestKey)
    }

}