package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADSubBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailViewModel
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADInfoItem
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADInfoRVAdapter
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADStatusRVAdapter
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.ADStatusType
import org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model.TrackersRVAdapter
import org.eu.exodus_privacy.exodusprivacy.utils.openURL
import javax.inject.Inject

@AndroidEntryPoint
class ADTrackersFragment : Fragment(R.layout.fragment_a_d_sub) {

    companion object {
        private const val trackersInfoPage =
            "https://reports.exodus-privacy.eu.org/info/trackers/"
    }

    @Inject
    lateinit var customTabsIntent: CustomTabsIntent

    private var _binding: FragmentADSubBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppDetailViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentADSubBinding.bind(view)

        val adTrackersStatusRVAdapter = ADStatusRVAdapter(ADStatusType.TRACKERS)
        val adTrackersRVAdapter = TrackersRVAdapter(true, findNavController().currentDestination!!.id)
        val adTrackersInfoRVAdapter = ADInfoRVAdapter(
            listOf(
                ADInfoItem(
                    text = R.string.tracker_info,
                    onClick = {
                        openURL(
                            customTabsIntent,
                            view.context,
                            trackersInfoPage,
                        )
                    },
                ),
            ),
        )

        binding.root.apply {
            adapter = ConcatAdapter(adTrackersStatusRVAdapter, adTrackersRVAdapter, adTrackersInfoRVAdapter)
            layoutManager = LinearLayoutManager(view.context)
        }

        viewModel.app.observe(viewLifecycleOwner) { app ->
            adTrackersStatusRVAdapter.setApp(app)
        }

        viewModel.trackers.observe(viewLifecycleOwner) {
                adTrackersRVAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
