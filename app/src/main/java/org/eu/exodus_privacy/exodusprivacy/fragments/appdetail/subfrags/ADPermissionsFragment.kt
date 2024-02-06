package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADPermissionsBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailViewModel
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADPermissionsInfoRVAdapter
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADPermissionsRVAdapter
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADPermissionsStatusRVAdapter
import org.eu.exodus_privacy.exodusprivacy.utils.openURL
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

        val adPermissionsStatusRVAdapter = ADPermissionsStatusRVAdapter()
        val adPermissionsRVAdapter = ADPermissionsRVAdapter()
        val adPermissionsInfoRVAdapter = ADPermissionsInfoRVAdapter(
            googleInfoClicked = {
                openURL(
                    customTabsIntent,
                    view.context,
                    googleInfoPage,
                )
            },
            permissionInfoClicked = {
                openURL(
                    customTabsIntent,
                    view.context,
                    permissionsInfoPage,
                )
            },
        )

        binding.permissionsRV.apply {
            adapter = ConcatAdapter(adPermissionsStatusRVAdapter, adPermissionsRVAdapter, adPermissionsInfoRVAdapter)
            layoutManager = LinearLayoutManager(view.context)
        }

        viewModel.app.observe(viewLifecycleOwner) { app ->
            adPermissionsStatusRVAdapter.setPermissionCount(app.permissions.size)
            adPermissionsRVAdapter.submitList(app.permissions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
