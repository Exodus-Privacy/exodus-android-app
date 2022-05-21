package org.eu.exodus_privacy.exodusprivacy.fragments.trackerdetail

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentTrackerDetailBinding
import javax.inject.Inject

@AndroidEntryPoint
class TrackerDetailFragment : Fragment(R.layout.fragment_tracker_detail) {

    private var _binding: FragmentTrackerDetailBinding? = null
    private val binding get() = _binding!!

    private val TAG = TrackerDetailFragment::class.java.simpleName

    private val args: TrackerDetailFragmentArgs by navArgs()
    private val viewModel: TrackerDetailViewModel by viewModels()

    @Inject
    lateinit var customTabsIntent: CustomTabsIntent

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrackerDetailBinding.bind(view)

        viewModel.getTracker(args.trackerID)

        binding.toolbarTD.setOnClickListener {
            view.findNavController().navigateUp()
        }

        viewModel.tracker.observe(viewLifecycleOwner) { tracker ->
            binding.apply {
                toolbarTD.apply {
                    inflateMenu(R.menu.tracker_detail_menu)
                    setOnMenuItemClickListener {
                        if (it.itemId == R.id.openTrackerPage) {
                            customTabsIntent.launchUrl(
                                view.context,
                                Uri.parse(tracker.website)
                            )
                        }
                        true
                    }
                }
                trackerTitleTV.text = tracker.name

                // Setup categories
                tracker.categories.forEach {
                    val chip = Chip(context)
                    val chipStyle = ChipDrawable.createFromAttributes(
                        view.context,
                        null,
                        0,
                        R.style.Theme_Exodus_Chip
                    )
                    chip.text = it
                    chip.setChipDrawable(chipStyle)
                    chipGroup.addView(chip)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.toolbarTD.setOnMenuItemClickListener(null)
        _binding = null
    }
}
