package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADTrackersBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailViewModel
import org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model.TrackersRVAdapter
import javax.inject.Inject

@AndroidEntryPoint
class ADTrackersFragment : Fragment(R.layout.fragment_a_d_trackers) {

    companion object {
        private const val trackersInfoPage =
            "https://reports.exodus-privacy.eu.org/en/info/trackers/"
    }

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
                        trackersStatusTV.text = getString(R.string.analysed)
                    }
                }
                trackersChip.apply {
                    val trackerNum = app.exodusTrackers.size
                    text = if (app.exodusVersionCode == 0L) "?" else trackerNum.toString()
                    setExodusColor(trackerNum)
                }
                trackersLearnTV.apply {
                    isClickable = true
                    setOnClickListener {
                        customTabsIntent.launchUrl(
                            view.context,
                            Uri.parse(trackersInfoPage)
                        )
                    }
                }
            }
        }

        viewModel.trackers.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val adTrackersRVAdapter = TrackersRVAdapter(true)
                binding.trackersRV.apply {
                    adapter = adTrackersRVAdapter
                    layoutManager = object : LinearLayoutManager(view.context) {
                        override fun canScrollVertically(): Boolean {
                            return false
                        }
                    }
                }
                adTrackersRVAdapter.submitList(it)
                binding.trackersStatusTV.text = getString(R.string.code_signature_found)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Chip.setExodusColor(size: Int) {
        val colorRed = ContextCompat.getColor(context, R.color.colorRedLight)
        val colorYellow = ContextCompat.getColor(context, R.color.colorYellow)
        val colorGreen = ContextCompat.getColor(context, R.color.colorGreen)

        val colorStateList = when (size) {
            0 -> ColorStateList.valueOf(colorGreen)
            in 1..4 -> ColorStateList.valueOf(colorYellow)
            else -> ColorStateList.valueOf(colorRed)
        }
        this.chipBackgroundColor = colorStateList
    }
}
