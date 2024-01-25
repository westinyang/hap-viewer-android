package org.ohosdev.hapviewerandroid.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.ohosdev.hapviewerandroid.dialog.AboutDialogBuilder

class AboutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AboutDialogBuilder(requireContext()).create()
    }

    companion object {
        const val TAG = "AboutDialogFragment"
    }
}