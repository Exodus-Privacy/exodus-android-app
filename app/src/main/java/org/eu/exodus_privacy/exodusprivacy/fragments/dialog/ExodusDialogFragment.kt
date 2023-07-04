package org.eu.exodus_privacy.exodusprivacy.fragments.dialog

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.ExodusUpdateService
import org.eu.exodus_privacy.exodusprivacy.MainActivityViewModel
import org.eu.exodus_privacy.exodusprivacy.R

@AndroidEntryPoint
class ExodusDialogFragment : DialogFragment() {

    private val TAG = ExodusDialogFragment::class.java.simpleName
    private val exodusDialogViewModel: MainActivityViewModel by viewModels()
    private val permission = "android.permission.POST_NOTIFICATIONS"
    val version = Build.VERSION.SDK_INT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exodusDialogViewModel.config.observe(viewLifecycleOwner) { config ->
            if (config["privacy_policy"]?.enable!!) this.dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Initialize the config
        exodusDialogViewModel.config.observe(this) {}
        return MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.warning_title))
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.warning_desc),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            ).setPositiveButton(getString(R.string.accept)) { _, _ ->
                Log.d(TAG, "Permission to transmit data granted!")
                exodusDialogViewModel.savePolicyAgreement(true)
                Log.d(TAG, "Version is: $version")
                if (version < 33) {
                    startInitial()
                } else {
                    if (!isNotificationPermissionGranted()) {
                        requestPermission()
                    }
                }
            }.setNegativeButton(getString(R.string.reject)) { _, _ ->
                activity?.finish()
            }.create()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        exodusDialogViewModel.saveNotificationPermissionRequested(true)
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(permission)
    }

    private fun startInitial() {
        exodusDialogViewModel.config.observe(this) { config ->
            if (!config["app_setup"]?.enable!! && config["privacy_policy"]?.enable!! && !ExodusUpdateService.IS_SERVICE_RUNNING) {
                Log.d(TAG, "Populating database for the first time.")
                val intent = Intent(requireContext(), ExodusUpdateService::class.java)
                intent.apply {
                    action = ExodusUpdateService.FIRST_TIME_START_SERVICE
                    requireContext().startService(this)
                }
            }
        }
    }
}
