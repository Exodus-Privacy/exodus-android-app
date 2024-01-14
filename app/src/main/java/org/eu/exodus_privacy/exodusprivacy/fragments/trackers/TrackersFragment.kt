package org.eu.exodus_privacy.exodusprivacy.fragments.trackers

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.ExodusUpdateService
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentTrackersBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model.TrackersRVAdapter

@AndroidEntryPoint
class TrackersFragment : Fragment(R.layout.fragment_trackers) {

    private var _binding: FragmentTrackersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrackersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrackersBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()

        val trackersRVAdapter =
            TrackersRVAdapter(false, findNavController().currentDestination!!.id)
        binding.trackersListRV.apply {
            adapter = trackersRVAdapter
            val column: Int =
                if (resources.configuration.smallestScreenWidthDp >= 600 && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    2
                } else {
                    1
                }
            layoutManager = GridLayoutManager(context, column)
        }

        // Setup Shimmer Layout
        for (num in 1..10) {
            val parent = binding.shimmerPlaceHolderLayout
            val shimmerLayout = LayoutInflater.from(view.context)
                .inflate(R.layout.shimmer_layout_tracker_item, parent, false)
            parent.addView(shimmerLayout)
        }

        viewModel.trackersList.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                val sortedList = list.sortedByDescending { it.exodusApplications.size }
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
                trackersRVAdapter.submitList(sortedList)
                binding.toolbarTrackers.title = trackersRVAdapter.itemCount.toString() + " " + getString(R.string.trackers)
            } else {
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.VISIBLE
                binding.toolbarTrackers.title = getString(R.string.trackers)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
