package org.eu.exodus_privacy.exodusprivacy.fragments.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.eu.exodus_privacy.exodusprivacy.R

class ThemeDialogFragment : DialogFragment() {
    private val TAG = ExodusDialogFragment::class.java.simpleName

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_theme))
            .setSingleChoiceItems(arrayOf(getString(R.string.dark_theme), getString(R.string.light_theme), getString(R.string.system_theme)), 2) { _, _ ->
                this.dismiss()
            }
            .create()
    }
}
