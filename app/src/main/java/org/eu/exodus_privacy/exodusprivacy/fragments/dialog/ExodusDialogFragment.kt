package org.eu.exodus_privacy.exodusprivacy.fragments.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R

@AndroidEntryPoint
class ExodusDialogFragment : DialogFragment() {

    private val TAG = ExodusDialogFragment::class.java.simpleName
    private val exodusDialogViewModel: ExodusDialogViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exodusDialogViewModel.policyAgreement.observe(viewLifecycleOwner) {
            this.dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.warning_title))
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.warning_desc),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )
            .setPositiveButton(getString(R.string.accept)) { _, _ ->
                Log.d(TAG, "Permission to transmit data granted!")
                exodusDialogViewModel.savePolicyAgreement(true)
            }
            .setNegativeButton(getString(R.string.reject)) { _, _ ->
                activity?.finish()
            }
            .create()
    }
}
