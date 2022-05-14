package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAppDetailBinding
import org.eu.exodus_privacy.exodusprivacy.objects.Source

@AndroidEntryPoint
class AppDetailFragment : Fragment(R.layout.fragment_app_detail) {

    private var _binding: FragmentAppDetailBinding? = null
    private val binding get() = _binding!!

    private val TAG = AppDetailFragment::class.java.simpleName

    private val args: AppDetailFragmentArgs by navArgs()
    private val viewModel: AppDetailViewModel by viewModels()

    companion object {
        private const val exodusReportPage = "https://reports.exodus-privacy.eu.org/en/reports/"
        private const val exodusSubmitPage =
            "https://reports.exodus-privacy.eu.org/en/analysis/submit/#"
        private const val fDroidPackagePage = "https://f-droid.org/en/packages/"
        private const val playPackagePage = "https://play.google.com/store/apps/details?id="
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppDetailBinding.bind(view)

        viewModel.getApp(args.packageName)

        binding.toolbar.setNavigationOnClickListener {
            view.findNavController().navigateUp()
        }

        viewModel.app.observe(viewLifecycleOwner) { app ->
            binding.apply {
                toolbar.apply {
                    inflateMenu(R.menu.app_detail_menu)
                    if (app.exodusVersionCode == 0L) {
                        menu.findItem(R.id.openExodusPage)?.isVisible = false
                    } else {
                        menu.findItem(R.id.submitApp)?.isVisible = false
                    }
                    setOnMenuItemClickListener {
                        // Set appropriate CustomTabsIntent for respective menu items
                        val customTabsIntent = CustomTabsIntent.Builder().build()
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
                                when (app.source) {
                                    Source.FDROID -> {
                                        customTabsIntent.launchUrl(
                                            view.context,
                                            Uri.parse(fDroidPackagePage + app.packageName)
                                        )
                                    }
                                    else -> {
                                        customTabsIntent.launchUrl(
                                            view.context,
                                            Uri.parse(playPackagePage + app.packageName)
                                        )
                                    }
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
                appVersionTV.text = app.versionName
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
                versionChip.chipIcon = when (app.exodusVersionCode) {
                    app.versionCode -> ContextCompat.getDrawable(view.context, R.drawable.ic_match)
                    0L -> ContextCompat.getDrawable(view.context, R.drawable.ic_unavailable)
                    else -> ContextCompat.getDrawable(view.context, R.drawable.ic_mismatch)
                }
                sourceChip.text = app.source.name.lowercase().replaceFirstChar { it.uppercase() }
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
