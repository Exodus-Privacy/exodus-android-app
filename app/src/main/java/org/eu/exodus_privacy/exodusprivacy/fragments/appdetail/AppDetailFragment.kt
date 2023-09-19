package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAppDetailBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.model.AppDetailVPAdapter
import org.eu.exodus_privacy.exodusprivacy.utils.setExodusColor
import org.eu.exodus_privacy.exodusprivacy.utils.setVersionReport
import javax.inject.Inject

@AndroidEntryPoint
class AppDetailFragment : Fragment(R.layout.fragment_app_detail) {

    private var _binding: FragmentAppDetailBinding? = null
    private val binding get() = _binding!!

    private val TAG = AppDetailFragment::class.java.simpleName

    private val args: AppDetailFragmentArgs by navArgs()
    private val viewModel: AppDetailViewModel by viewModels()

    @Inject
    lateinit var customTabsIntent: CustomTabsIntent

    companion object {
        private const val exodusReportPage = "https://reports.exodus-privacy.eu.org/reports/"
        private const val exodusSubmitPage =
            "https://reports.exodus-privacy.eu.org/analysis/submit/#"
        private const val storePage = "market://details?id="
        private const val GPPage = "https://play.google.com/store/apps/details?id="
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppDetailBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()

        viewModel.getApp(args.packageName)

        binding.toolbarAD.setNavigationOnClickListener {
            view.findNavController().navigateUp()
        }

        viewModel.app.observe(viewLifecycleOwner) { app ->
            binding.apply {
                toolbarAD.apply {
                    menu.clear()
                    inflateMenu(R.menu.app_detail_menu)
                    if (app.exodusVersionCode == 0L) {
                        menu.findItem(R.id.openExodusPage)?.isVisible = false
                    } else menu.findItem(R.id.submitApp)?.isVisible = app.exodusVersionCode != app.versionCode
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.openExodusPage -> {
                                customTabsIntent.launchUrl(
                                    view.context,
                                    Uri.parse(exodusReportPage + app.report)
                                )
                            }
                            R.id.submitApp -> {
                                customTabsIntent.launchUrl(
                                    view.context,
                                    Uri.parse(exodusSubmitPage + app.packageName)
                                )
                            }
                            R.id.openStore -> {
                                try {
                                    customTabsIntent.launchUrl(
                                        view.context,
                                        Uri.parse(storePage + app.packageName)
                                    )
                                } catch (e: ActivityNotFoundException) {
                                    customTabsIntent.launchUrl(
                                        view.context,
                                        Uri.parse(GPPage + app.packageName)
                                    )
                                }
                            }
                            R.id.openAppInfo -> {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:" + app.packageName)
                                    }
                                startActivity(intent)
                            }
                            else -> {
                                Log.d(TAG, "Unexpected itemId: ${it.itemId}")
                            }
                        }
                        true
                    }
                }
                appIconIV.background = app.icon.toDrawable(view.resources)
                appNameTV.text = app.name
                when (app.exodusVersionCode) {
                    app.versionCode, 0L -> {
                        appVersionTV.text = app.versionName
                    }
                    else -> {
                        if (app.versionName != app.exodusVersionName) {
                            appIVTV.visibility = View.VISIBLE
                            appInstalledVersionTV.apply {
                                text = app.versionName
                                visibility = View.VISIBLE
                            }
                            appAVTV.visibility = View.VISIBLE
                            appAnalyzedVersionTV.apply {
                                text = app.exodusVersionName
                                visibility = View.VISIBLE
                            }
                            appVTV.visibility = View.GONE
                            appVersionTV.visibility = View.GONE
                        } else {
                            appIVTV.visibility = View.GONE
                            appInstalledVersionTV.visibility = View.GONE
                            appAVTV.visibility = View.GONE
                            appAnalyzedVersionTV.visibility = View.GONE
                            appVTV.visibility = View.VISIBLE
                            appVersionTV.apply {
                                text = app.versionName
                                visibility = View.VISIBLE
                            }
                            appSameVersionTV.visibility = View.VISIBLE
                        }
                    }
                }
                if (app.created.isNotBlank()) {
                    val dateCreated = viewModel.getFormattedReportDate(app.created, view.context)
                    val dateUpdated = viewModel.getFormattedReportDate(app.updated, view.context)
                    if (dateCreated != dateUpdated) {
                        appReportTV.text = getString(
                            R.string.report_date,
                            dateCreated
                        ) + " " + getString(
                            R.string.updated,
                            dateUpdated
                        )
                    } else {
                        appReportTV.text = getString(
                            R.string.report_date,
                            dateCreated
                        )
                    }
                } else {
                    appReportTV.visibility = View.GONE
                }
                trackersChip.apply {
                    val trackerNum = app.exodusTrackers.size
                    text = if (app.exodusVersionCode == 0L) "?" else trackerNum.toString()
                    setExodusColor(trackerNum)
                }
                permsChip.apply {
                    val permsNum = app.permissions.size
                    text = permsNum.toString()
                    setExodusColor(permsNum)
                }
                versionChip.setVersionReport(app)

                sourceChip.text = app.source.name.lowercase().replaceFirstChar { it.uppercase() }

                // Setup ViewPager for trackers and permissions fragment
                viewPager.apply {
                    adapter = AppDetailVPAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
                    isUserInputEnabled = false
                }
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    when (position) {
                        0 -> {
                            tab.apply {
                                text = getString(R.string.trackers)
                                icon =
                                    ContextCompat.getDrawable(view.context, R.drawable.ic_tracker)
                            }
                        }
                        1 -> {
                            tab.apply {
                                text = getString(R.string.permissions)
                                icon = ContextCompat.getDrawable(
                                    view.context,
                                    R.drawable.ic_permission
                                )
                            }
                        }
                    }
                }.attach()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.toolbarAD.setOnMenuItemClickListener(null)
        _binding = null
    }
}
