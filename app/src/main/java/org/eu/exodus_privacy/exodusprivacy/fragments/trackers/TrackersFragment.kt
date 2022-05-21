package org.eu.exodus_privacy.exodusprivacy.fragments.trackers

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

        val trackersRVAdapter =
            TrackersRVAdapter(false, findNavController().currentDestination!!.id)
        binding.trackersListRV.apply {
            adapter = trackersRVAdapter
            layoutManager = LinearLayoutManager(view.context)
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
            } else {
                binding.shimmerLayout.visibility = View.VISIBLE
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            if (!ExodusUpdateService.IS_SERVICE_RUNNING) {
                Toast.makeText(view.context, getString(R.string.fetching_apps), Toast.LENGTH_SHORT)
                    .show()
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
