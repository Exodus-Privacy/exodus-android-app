package org.eu.exodus_privacy.exodusprivacy.fragments.apps

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.ExodusUpdateService
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAppsBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.apps.model.AppsRVAdapter

@AndroidEntryPoint
class AppsFragment : Fragment(R.layout.fragment_apps), Toolbar.OnMenuItemClickListener {

    private val TAG = AppsFragment::class.java.simpleName

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by viewModels()

    private lateinit var sortPopupMenu: PopupMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppsBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        setupToolbar()
        setupSearchView()
        setupSortPopupMenu(view)
        setupRecyclerView()
        setupShimmerLayout(view)
        setupObservers()
        setupSwipeRefreshLayout(view)
        setupUpdateReportsFab(view)
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbarApps
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.apps_menu)
        toolbar.setOnMenuItemClickListener(this)
    }

    private fun setupSearchView() {
        val searchMenu =
            binding.toolbarApps.menu.findItem(R.id.action_search).actionView as SearchView
        searchMenu.maxWidth = Integer.MAX_VALUE // Expand the search view to fill the toolbar
        searchMenu.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchApp(newText.orEmpty())
                return false
            }
        })
    }

    private fun setupSortPopupMenu(view: View) {
        val sortMenuView = view.findViewById<View>(R.id.action_filter)
        sortPopupMenu = PopupMenu(context, sortMenuView)
        sortPopupMenu.inflate(R.menu.popup_menu_filter)
        sortPopupMenu.setOnMenuItemClickListener { sortType ->
            when (sortType.itemId) {
                R.id.sort_by_name -> {
                    viewModel.sortApps(SortType.Name)
                }

                R.id.sort_by_trackers -> {
                    viewModel.sortApps(SortType.Trackers)
                }

                R.id.sort_by_permissions -> {
                    viewModel.sortApps(SortType.Permissions)
                }

                R.id.sort_by_creation_date -> {
                    viewModel.sortApps(SortType.CreatedAt)
                }

                else -> {
                    Log.d(TAG, "Unexpected SortType: ${sortType.itemId}")
                }
            }
            true
        }
    }

    private fun setupRecyclerView() {
        val appsRVAdapter = AppsRVAdapter(findNavController().currentDestination!!.id)
        binding.appListRV.apply {
            adapter = appsRVAdapter
            val column: Int =
                if (resources.configuration.smallestScreenWidthDp >= 600 && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    2
                } else {
                    1
                }
            layoutManager = StaggeredGridLayoutManager(column, 1)
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0 && binding.updateReportsFAB.isVisible) {
                            binding.updateReportsFAB.hide()
                        } else if (dy < 0 && !binding.updateReportsFAB.isVisible) {
                            binding.updateReportsFAB.show()
                        }
                    }
                },
            )
            setItemViewCacheSize(10)
        }
    }

    private fun setupShimmerLayout(view: View) {
        for (num in 1..10) {
            val parent = binding.shimmerPlaceHolderLayout
            val shimmerLayout = LayoutInflater.from(view.context)
                .inflate(R.layout.shimmer_layout_app_item, parent, false)
            parent.addView(shimmerLayout)
        }
    }

    private fun setupObservers() {
        viewModel.sortedAppList.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                // If the list is not empty, show the RecyclerView with data
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.appListRV.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
                binding.progress.visibility = View.GONE
                binding.noAppsFound.visibility = View.GONE
                (binding.appListRV.adapter as AppsRVAdapter).submitList(list)
            } else {
                // If the list is empty, check if the search query is empty
                if (viewModel.currentSearchQuery.value.isNullOrEmpty()) {
                    // If both list and search query are empty, show the shimmer layout
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.appListRV.visibility = View.VISIBLE
                    binding.noAppsFound.visibility = View.GONE
                } else {
                    // If search query is not empty, show the no apps found layout
                    binding.appListRV.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.GONE
                    binding.noAppsFound.visibility = View.VISIBLE
                }
            }
            binding.swipeRefreshLayout.visibility = View.VISIBLE
        }

        // Preserve the state of the sort menu on orientation change
        viewModel.currentSortType.observe(viewLifecycleOwner) { sortType ->
            if (sortType != null) {
                updateSortPopupMenu(sortType)
            }
        }
    }

    private fun updateSortPopupMenu(sortType: SortType) {
        sortPopupMenu.menu.findItem(R.id.sort_by_name).isChecked = false
        sortPopupMenu.menu.findItem(R.id.sort_by_trackers).isChecked = false
        sortPopupMenu.menu.findItem(R.id.sort_by_permissions).isChecked = false
        sortPopupMenu.menu.findItem(R.id.sort_by_creation_date).isChecked = false
        when (sortType) {
            SortType.Name -> {
                sortPopupMenu.menu.findItem(R.id.sort_by_name).isChecked = true
            }

            SortType.Trackers -> {
                sortPopupMenu.menu.findItem(R.id.sort_by_trackers).isChecked = true
            }

            SortType.Permissions -> {
                sortPopupMenu.menu.findItem(R.id.sort_by_permissions).isChecked = true
            }

            SortType.CreatedAt -> {
                sortPopupMenu.menu.findItem(R.id.sort_by_creation_date).isChecked = true
            }
        }
    }

    private fun setupSwipeRefreshLayout(view: View) {
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            updateReports(view.context)
            binding.progress.visibility = View.VISIBLE
        }
    }

    private fun setupUpdateReportsFab(view: View) {
        binding.updateReportsFAB.setOnClickListener {
            updateReports(view.context)
            binding.progress.visibility = View.VISIBLE
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {}
            R.id.action_filter -> {
                sortPopupMenu.show()
            }

            else -> {
                Log.d(TAG, "Unexpected itemId: ${item.itemId}")
            }
        }
        return true
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

    private fun updateReports(context: Context) {
        if (!ExodusUpdateService.IS_SERVICE_RUNNING) {
            val intent = Intent(context, ExodusUpdateService::class.java)
            intent.apply {
                action = ExodusUpdateService.START_SERVICE
                activity?.startService(this)
            }
        }
    }
}
