package org.eu.exodus_privacy.exodusprivacy.fragments.apps

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.MainActivityViewModel
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAppsBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.apps.model.AppsRVAdapter
import org.eu.exodus_privacy.exodusprivacy.objects.Status

@AndroidEntryPoint
class AppsFragment : Fragment(R.layout.fragment_apps) {

    private val TAG = AppsFragment::class.java.simpleName

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppsBinding.bind(view)

        // Setup menu actions
        val toolbar = binding.toolbarApps
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
        val appsRVAdapter = AppsRVAdapter()
        binding.appListRV.apply {
            adapter = appsRVAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

        mainActivityViewModel.dbStatus.observe(viewLifecycleOwner) {
            when (it) {
                Status.RUNNING_TRACKER -> {
                    binding.setupTV.text = view.context.getString(R.string.fetching_trackers)
                    binding.setupPB.visibility = View.VISIBLE
                    binding.setupTV.visibility = View.VISIBLE
                }
                Status.RUNNING_APPS -> {
                    binding.setupTV.text = view.context.getString(R.string.fetching_apps)
                }
                Status.COMPLETED_APPS -> {
                    binding.setupPB.visibility = View.GONE
                    binding.setupTV.visibility = View.GONE
                }
                else -> Log.d(TAG, "Got an unhandled status: $it")
            }
        }

        viewModel.appList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.appListRV.visibility = View.VISIBLE
                appsRVAdapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
