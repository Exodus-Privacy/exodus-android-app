package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADPermissionsBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailViewModel

@AndroidEntryPoint
class ADPermissionsFragment : Fragment(R.layout.fragment_a_d_permissions) {

    companion object {
        private const val googleInfoPage =
            "https://developer.android.com/guide/topics/permissions/overview"
        private const val permissionsInfoPage =
            "https://reports.exodus-privacy.eu.org/en/info/permissions/"
    }

    private var _binding: FragmentADPermissionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppDetailViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentADPermissionsBinding.bind(view)

        viewModel.app.observe(viewLifecycleOwner) { app ->
            binding.apply {
                if (app.permissions.isEmpty()) {
                    permissionsRV.visibility = View.GONE
                } else {
                    permissionsStatusTV.text = getString(R.string.code_permission_found)
                }
                permissionsChip.apply {
                    val permsNum = app.permissions.size
                    text = permsNum.toString()
                    setExodusColor(permsNum)
                }
                permissionsLearnGoogleTV.apply {
                    isClickable = true
                    setOnClickListener {
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(
                            view.context,
                            Uri.parse(googleInfoPage)
                        )
                    }
                }
                permissionsLearnExodusTV.apply {
                    isClickable = true
                    setOnClickListener {
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(
                            view.context,
                            Uri.parse(permissionsInfoPage)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Chip.setExodusColor(size: Int) {
        val colorRed = ContextCompat.getColor(context, R.color.colorRedLight)
        val colorYellow = ContextCompat.getColor(context, R.color.colorYellow)
        val colorGreen = ContextCompat.getColor(context, R.color.colorGreen)

        val colorStateList = when (size) {
            0 -> ColorStateList.valueOf(colorGreen)
            in 1..4 -> ColorStateList.valueOf(colorYellow)
            else -> ColorStateList.valueOf(colorRed)
        }
        this.chipBackgroundColor = colorStateList
    }
}
