package org.ohosdev.hapviewerandroid.ui.about

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.ohosdev.hapviewerandroid.ui.about.AboutDialogBuilder

class AboutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AboutDialogBuilder(requireContext()).create()
    }

    companion object {
        const val TAG = "AboutDialogFragment"
    }
}