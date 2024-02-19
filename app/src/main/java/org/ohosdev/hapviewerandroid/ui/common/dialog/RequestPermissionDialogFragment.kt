package org.ohosdev.hapviewerandroid.ui.common.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.setFragmentResult
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.extensions.ensureArguments
import org.ohosdev.hapviewerandroid.extensions.openUrl

class RequestPermissionDialogFragment : AlertDialogFragment() {

    override fun onCreateAlertDialogBuilder(): RequestPermissionDialogBuilder<*> =
        RequestPermissionDialogBuilder(requireContext())

    override fun onAttachAlertDialogBuilder(builder: AlertDialogBuilder<*>) {
        super.onAttachAlertDialogBuilder(builder)
        builder as RequestPermissionDialogBuilder<*>
        arguments?.also { args ->
            args.getString(ARG_KEY_ON_AGREE)?.also {
                builder.setOnAgree {
                    setFragmentResult(it, Bundle())
                }
            }
            args.getStringArray(ARG_KEY_PERMISSION_NAMES)?.also { builder.setPermissionNames(it) }
            args.getIntArray(ARG_KEY_PERMISSION_NAME_IDS)?.also { builder.setPermissionNames(it) }
            args.getStringArray(ARG_KEY_FUNCTION_NAMES)?.also { builder.setFunctionNames(it) }
            args.getIntArray(ARG_KEY_FUNCTION_NAME_IDS)?.also { builder.setFunctionNames(it) }
            args.getInt(ARG_KEY_ADDITIONAL_ID, 0).also { if (it != 0) builder.setAdditional(it) }
            args.getString(ARG_KEY_GUIDE_URL)
                ?.also { builder.setNeutralButton(R.string.guide, null) }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            this as AlertDialog
            arguments?.getString(ARG_KEY_GUIDE_URL)?.also { url ->
                getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener {
                    requireContext().openUrl(url)
                }
            }
        }
    }

    fun setPermissionNames(names: Array<String>) = apply {
        ensureArguments().putStringArray(ARG_KEY_PERMISSION_NAMES, names)
    }

    fun setPermissionNames(names: IntArray) = apply {
        ensureArguments().putIntArray(ARG_KEY_PERMISSION_NAME_IDS, names)
    }

    fun setFunctionNames(names: Array<String>) = apply {
        ensureArguments().putStringArray(ARG_KEY_FUNCTION_NAMES, names)
    }

    fun setFunctionNames(names: IntArray) = apply {
        ensureArguments().putIntArray(ARG_KEY_FUNCTION_NAME_IDS, names)
    }

    fun setOnAgreeKey(key: String) = apply {
        ensureArguments().putString(ARG_KEY_ON_AGREE, key)
    }

    fun setAdditional(@StringRes additional: Int) = apply {
        ensureArguments().putInt(ARG_KEY_ADDITIONAL_ID, additional)
    }

    fun setGuideUrl(url: String) = apply {
        ensureArguments().putString(ARG_KEY_GUIDE_URL, url)
    }

    companion object {
        const val ARG_KEY_PERMISSION_NAMES = "permissionNames"
        const val ARG_KEY_PERMISSION_NAME_IDS = "permissionNameIds"
        const val ARG_KEY_FUNCTION_NAMES = "functionNames"
        const val ARG_KEY_FUNCTION_NAME_IDS = "functionNameIds"
        const val ARG_KEY_ON_AGREE = "agreeKey"
        const val ARG_KEY_ADDITIONAL_ID = "additionalId"
        const val ARG_KEY_GUIDE_URL = "guideUrl"
    }

}