package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADPermissionsBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailViewModel
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADPermissionsRVAdapter
import org.eu.exodus_privacy.exodusprivacy.utils.setExodusColor
import javax.inject.Inject

@AndroidEntryPoint
class ADPermissionsFragment : Fragment(R.layout.fragment_a_d_permissions) {

    companion object {
        private const val googleInfoPage =
            "https://developer.android.com/guide/topics/permissions/overview"
        private const val permissionsInfoPage =
            "https://reports.exodus-privacy.eu.org/info/permissions/"
    }

    @Inject
    lateinit var customTabsIntent: CustomTabsIntent

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
                    val adPermissionsRVAdapter = ADPermissionsRVAdapter()
                    permissionsRV.apply {
                        adapter = adPermissionsRVAdapter
                        layoutManager = object : LinearLayoutManager(view.context) {
                            override fun canScrollVertically(): Boolean {
                                return false
                            }
                        }
                    }
                    adPermissionsRVAdapter.submitList(app.permissions)
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
                        customTabsIntent.launchUrl(
                            view.context,
                            Uri.parse(googleInfoPage),
                        )
                    }
                }
                permissionsLearnExodusTV.apply {
                    isClickable = true
                    setOnClickListener {
                        customTabsIntent.launchUrl(
                            view.context,
                            Uri.parse(permissionsInfoPage),
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
}
