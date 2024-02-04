package org.ohosdev.hapviewerandroid.ui.about

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class AboutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AboutDialogBuilder(requireContext()).create()
    }

    companion object {
        const val TAG = "AboutDialogFragment"
    }
}