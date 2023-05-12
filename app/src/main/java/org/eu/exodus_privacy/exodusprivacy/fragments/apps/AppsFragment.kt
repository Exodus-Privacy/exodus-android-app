package org.eu.exodus_privacy.exodusprivacy.fragments.apps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.ExodusUpdateService
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAppsBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.apps.model.AppsRVAdapter

@AndroidEntryPoint
class AppsFragment : Fragment(R.layout.fragment_apps) {

    private val TAG = AppsFragment::class.java.simpleName

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppsBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()

        // Setup menu actions
        val toolbar = binding.toolbarApps
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.apps_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_search -> {}
                R.id.action_filter -> {
                    val menuItemView = view.findViewById<View>(R.id.action_filter)
                    val popupMenu = PopupMenu(context, menuItemView)
                    popupMenu.inflate(R.menu.popup_menu_filter)
                    popupMenu.show()
                }
                else -> {
                    Log.d(TAG, "Unexpected itemId: ${it.itemId}")
                }
            }
            true
        }

        // Setup RecyclerView
        val appsRVAdapter = AppsRVAdapter(findNavController().currentDestination!!.id)
        binding.appListRV.apply {
            adapter = appsRVAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        // Setup Shimmer Layout
        for (num in 1..10) {
            val parent = binding.shimmerPlaceHolderLayout
            val shimmerLayout = LayoutInflater.from(view.context)
                .inflate(R.layout.shimmer_layout_app_item, parent, false)
            parent.addView(shimmerLayout)
        }

        viewModel.appList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
                appsRVAdapter.submitList(it)
            } else {
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.VISIBLE
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            if (!ExodusUpdateService.IS_SERVICE_RUNNING) {
                val intent = Intent(view.context, ExodusUpdateService::class.java)
                intent.apply {
                    action = ExodusUpdateService.START_SERVICE
                    activity?.startService(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.startShimmer()
    }

    override fun onPause() {
        binding.shimmerLayout.stopShimmer()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.toolbarApps.setOnMenuItemClickListener(null)
        _binding = null
    }
}
