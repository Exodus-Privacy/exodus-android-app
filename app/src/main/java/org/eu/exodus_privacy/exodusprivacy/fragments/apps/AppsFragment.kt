package org.eu.exodus_privacy.exodusprivacy.fragments.apps

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
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

        // Setup menu actions
        val toolbar = binding.toolbar
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

        viewModel.appList.observe(viewLifecycleOwner) {
            appsRVAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
