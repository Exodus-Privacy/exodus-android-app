package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADTrackersBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailViewModel
import org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model.TrackersRVAdapter
import org.eu.exodus_privacy.exodusprivacy.utils.openURL
import org.eu.exodus_privacy.exodusprivacy.utils.setExodusColor
import javax.inject.Inject

@AndroidEntryPoint
class ADTrackersFragment : Fragment(R.layout.fragment_a_d_trackers) {

    companion object {
        private const val trackersInfoPage =
            "https://reports.exodus-privacy.eu.org/info/trackers/"
    }

    private val TAG = ADTrackersFragment::class.java.simpleName

    @Inject
    lateinit var customTabsIntent: CustomTabsIntent

    private var _binding: FragmentADTrackersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppDetailViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentADTrackersBinding.bind(view)

        viewModel.app.observe(viewLifecycleOwner) { app ->
            binding.apply {
                if (app.exodusTrackers.isEmpty()) {
                    trackersRV.visibility = View.GONE
                    if (app.exodusVersionCode == 0L) {
                        trackersStatusTV.text = getString(R.string.analyzed)
                    }
                }
                trackersChip.apply {
                    val trackerNum =
                        if (app.exodusVersionCode == 0L) "?" else app.exodusTrackers.size.toString()
                    text = trackerNum
                    setExodusColor(trackerNum)
                }
                trackersLearnTV.apply {
                    isClickable = true
                    setOnClickListener {
                        openURL(
                            customTabsIntent,
                            view.context,
                            trackersInfoPage,
                        )
                    }
                }
            }
        }

        viewModel.trackers.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val adTrackersRVAdapter =
                    TrackersRVAdapter(true, findNavController().currentDestination!!.id)
                binding.trackersRV.apply {
                    adapter = adTrackersRVAdapter
                    layoutManager = object : LinearLayoutManager(view.context) {
                        override fun canScrollVertically(): Boolean {
                            return false
                        }
                    }
                    setItemViewCacheSize(10)
                }
                val trackersList = arrayListOf<String>()
                it.forEach { item -> trackersList.add(item.name) }
                Log.d(TAG, "Submitting following trackers for app ${viewModel.app.value?.name}:")
                Log.d(TAG, "$trackersList.")
                adTrackersRVAdapter.submitList(it)
                binding.trackersStatusTV.text = getString(R.string.code_signature_found)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
