package org.ohosdev.hapviewerandroid.ui.common.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import org.ohosdev.hapviewerandroid.extensions.contentSelectable
import org.ohosdev.hapviewerandroid.extensions.ensureArguments

class SimpleDialogFragment : DialogFragment() {

    private val onClickListener = DialogInterface.OnClickListener { dialog, which ->
        val requestKey = when (which) {
            Dialog.BUTTON_POSITIVE -> ARG_KEY_BUTTON_POSITIVE_KEY
            Dialog.BUTTON_NEGATIVE -> ARG_KEY_BUTTON_NEGATIVE_KEY
            Dialog.BUTTON_NEUTRAL -> ARG_KEY_BUTTON_NEUTRAL_KEY
            else -> return@OnClickListener
        }
        arguments?.getString(requestKey)?.let {
            setFragmentResult(it, Bundle())
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialogBuilder(requireContext()).apply {
            arguments?.also { args ->
                args.getCharSequence(ARG_KEY_TITLE)?.also { setTitle(it) }
                args.getCharSequence(ARG_KEY_MESSAGE)?.also { setMessage(it) }
                args.getInt(ARG_KEY_TITLE_ID, 0).also { if (it != 0) setTitle(it) }
                args.getInt(ARG_KEY_MESSAGE_ID, 0).also { if (it != 0) setMessage(it) }
                args.getInt(ARG_KEY_BUTTON_POSITIVE_ID, 0).also {
                    if (it != 0) setPositiveButton(it, onClickListener)
                }
                args.getInt(ARG_KEY_BUTTON_NEGATIVE_ID, 0).also {
                    if (it != 0) setNegativeButton(it, onClickListener)
                }
                args.getInt(ARG_KEY_BUTTON_NEUTRAL_ID, 0).also {
                    if (it != 0) setNeutralButton(it, onClickListener)
                }
            }
        }.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            arguments?.also {
                contentSelectable = it.getBoolean(ARG_KEY_SELECTABLE, false)
            }
        }
    }

    /**
     * @see AlertDialogBuilder.setTitle
     * */
    fun setTitle(title: CharSequence) = apply {
        ensureArguments()
        arguments?.putCharSequence(ARG_KEY_TITLE, title)
    }

    /**
     * @see AlertDialogBuilder.setTitle
     * */
    fun setTitle(@StringRes titleId: Int) = apply {
        ensureArguments()
        arguments?.putInt(ARG_KEY_TITLE_ID, titleId)
    }

    /**
     * @see AlertDialogBuilder.setMessage
     * */
    fun setMessage(message: CharSequence) = apply {
        ensureArguments()
        arguments?.putCharSequence(ARG_KEY_MESSAGE, message)
    }

    /**
     * @see AlertDialogBuilder.setMessage
     * */
    fun setMessage(@StringRes messageId: Int) = apply {
        ensureArguments()
        arguments?.putInt(ARG_KEY_MESSAGE_ID, messageId)
    }

    /**
     * @see Dialog.contentSelectable
     * */
    fun setSelectable(selectable: Boolean) = apply {
        ensureArguments()
        arguments?.putBoolean(ARG_KEY_SELECTABLE, selectable)
    }

    /**
     * @see AlertDialogBuilder.setPositiveButton
     * */
    fun setPositiveButton(@StringRes textId: Int, requestKey: String?) = apply {
        ensureArguments()
        arguments?.apply {
            putInt(ARG_KEY_BUTTON_POSITIVE_ID, textId)
            putString(ARG_KEY_BUTTON_POSITIVE_KEY, requestKey)
        }
    }

    /**
     * @see AlertDialogBuilder.setNegativeButton
     * */
    fun setNegativeButton(@StringRes textId: Int, requestKey: String?) = apply {
        ensureArguments()
        arguments?.apply {
            putInt(ARG_KEY_BUTTON_NEGATIVE_ID, textId)
            putString(ARG_KEY_BUTTON_NEGATIVE_KEY, requestKey)
        }
    }

    /**
     * @see AlertDialogBuilder.setNeutralButton
     * */
    fun setNeutralButton(@StringRes textId: Int, requestKey: String?) = apply {
        ensureArguments()
        arguments?.apply {
            putInt(ARG_KEY_BUTTON_NEUTRAL_ID, textId)
            putString(ARG_KEY_BUTTON_NEUTRAL_KEY, requestKey)
        }
    }

    companion object {
        const val ARG_KEY_TITLE = "title"
        const val ARG_KEY_TITLE_ID = "titleId"
        const val ARG_KEY_MESSAGE = "message"
        const val ARG_KEY_MESSAGE_ID = "messageId"
        const val ARG_KEY_SELECTABLE = "selectable"

        // 对话框按钮
        const val ARG_KEY_BUTTON_POSITIVE_KEY = "positiveButtonKey"
        const val ARG_KEY_BUTTON_POSITIVE_ID = "positiveButtonId"
        const val ARG_KEY_BUTTON_NEGATIVE_KEY = "negativeButtonKey"
        const val ARG_KEY_BUTTON_NEGATIVE_ID = "negativeButtonId"
        const val ARG_KEY_BUTTON_NEUTRAL_KEY = "neutralButtonKey"
        const val ARG_KEY_BUTTON_NEUTRAL_ID = "neutralButtonId"
    }
}