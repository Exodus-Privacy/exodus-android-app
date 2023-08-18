package org.eu.exodus_privacy.exodusprivacy.fragments.dialog

import android.app.Dialog
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import org.eu.exodus_privacy.exodusprivacy.R

class ThemeDialogFragment : DialogFragment() {
    private val TAG = ExodusDialogFragment::class.java.simpleName
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = mutableListOf(getString(R.string.light_theme), getString(R.string.dark_theme))
        val oledSwitch = MaterialSwitch(requireContext())
        // Show "Follow system" only on SDK >= 29
        if (Build.VERSION.SDK_INT >= 29) {
            items.add(
                2, getString(R.string.system_theme)
            )
        }
        // If preference initial value is 0, set initialCheckedItem to 0 or 2 based on SDK
        // TODO:
        //  Get preferences initial value and set it below instead of 0
        val preferenceInitialValue = 0
        val initialCheckedItem =
            if (preferenceInitialValue != 0) {
                // TODO:
                //  Get checked item value from preferences and set it below instead of 0
                0
            } else {
                when {
                    Build.VERSION.SDK_INT >= 29 -> 2
                    else -> 0
                }
            }
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_theme))
            .setSingleChoiceItems(items.toTypedArray(), initialCheckedItem) { _, newCheckedItem ->
                oledSwitch.isEnabled = when (newCheckedItem) {
                    1 -> true
                    2 -> isNightMode()
                    else -> false
                }
            }
            .setPositiveButton(getString(R.string.ok)) { _, newCheckedItem ->
                // TODO:
                //  store newCheckedItem in preferences
                when (newCheckedItem) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                dismiss()
            }
        oledSwitch.text = getString(R.string.black_theme)
        // TODO:
        //  Get isChecked value from preferences and set it below instead of false
        oledSwitch.isChecked = false
        oledSwitch.isEnabled = isNightMode()
        oledSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO:
            //  store isChecked in preferences
        }
        val switchLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val dpToPixels = requireContext().resources.displayMetrics.density
        switchLayoutParams.marginStart = (35 * dpToPixels).toInt()
        switchLayoutParams.marginEnd = (20 * dpToPixels).toInt()
        oledSwitch.layoutParams = switchLayoutParams
        val dialogContainer = LinearLayout(requireContext())
        dialogContainer.orientation = LinearLayout.VERTICAL
        dialogContainer.addView(oledSwitch)
        dialogBuilder.setView(dialogContainer)
        return dialogBuilder.create()
    }
    private fun isNightMode(): Boolean {
        return requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}
