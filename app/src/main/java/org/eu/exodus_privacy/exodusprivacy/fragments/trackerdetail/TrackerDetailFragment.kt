package org.eu.exodus_privacy.exodusprivacy.fragments.trackerdetail

import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentTrackerDetailBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.apps.model.AppsRVAdapter
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
            if (tracker.exodusApplications.isNotEmpty()) {
                viewModel.getApps(tracker.exodusApplications)
            }

            binding.apply {
                toolbarTD.apply {
                    menu.clear()
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
                chipGroup.removeAllViews()
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

                // Show presence if tracker is present on device
                if (args.percentage != 0) {
                    trackerPresenceTV.visibility = View.VISIBLE
                    trackerPresenceTV.text = getString(
                        R.string.trackers_presence,
                        args.percentage,
                        tracker.exodusApplications.size
                    )
                }

                // Tracker description and webURL
                if (tracker.description.isNotEmpty()) {
                    trackerDescTV.apply {
                        val markwon = Markwon.create(view.context)
                        markwon.setMarkdown(this, tracker.description)
                        movementMethod = LinkMovementMethod.getInstance()
                        isClickable = true
                        removeUnderlineFromLinks()
                    }
                } else {
                    trackerDescTV.visibility = View.GONE
                }
                trackerWebURLTV.text = tracker.website

                // Tracker code and network signatures
                codeSignTV.text = tracker.code_signature
                if (tracker.network_signature.isNotEmpty()) {
                    networkSignTV.text = tracker.network_signature
                } else {
                    networkSignTV.text = "NC"
                }
            }
        }

        viewModel.appsList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val appsRVAdapter = AppsRVAdapter(findNavController().currentDestination!!.id)
                binding.appsListTV.visibility = View.VISIBLE
                binding.appsListRV.apply {
                    visibility = View.VISIBLE
                    adapter = appsRVAdapter
                    layoutManager = object : LinearLayoutManager(view.context) {
                        override fun canScrollVertically(): Boolean {
                            return false
                        }
                    }
                }
                appsRVAdapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.toolbarTD.setOnMenuItemClickListener(null)
        _binding = null
    }

    private fun TextView.removeUnderlineFromLinks() {
        val spannable = SpannableString(text)
        for (urlSpan in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
            spannable.setSpan(
                object : URLSpan(urlSpan.url) {
                    override fun updateDrawState(textPaint: TextPaint) {
                        super.updateDrawState(textPaint)
                        textPaint.isUnderlineText = false
                    }
                },
                spannable.getSpanStart(urlSpan), spannable.getSpanEnd(urlSpan), 0
            )
        }
        text = spannable
    }
}
