package org.eu.exodus_privacy.exodusprivacy.fragments.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.BuildConfig
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAboutBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.dialog.ThemeDialogFragment
import org.eu.exodus_privacy.exodusprivacy.utils.getLanguage
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : PreferenceFragmentCompat() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var customTabsIntent: CustomTabsIntent

    companion object {
        private const val analyzeURL = "https://reports.exodus-privacy.eu.org/analysis/submit/"
        private const val alternativesURL = "https://reports.exodus-privacy.eu.org/info/next/"
        private const val privacyPolicyURL = "https://exodus-privacy.eu.org/en/page/privacy-policy/"
        private const val sourceCodeURL = "https://github.com/Exodus-Privacy/exodus-android-app"
        private const val websiteURL = "https://exodus-privacy.eu.org/"
        private const val twitterURL = "https://twitter.com/ExodusPrivacy"
        private const val mastodonURL = "https://framapiaf.org/@exodus"
        private const val emailID = "contact@exodus-privacy.eu.org"
    }

    private fun getLocaleWebsiteURL(): String {
        return if (getLanguage() != "fr") {
            websiteURL + "en"
        } else {
            websiteURL + "fr"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()

        val toolbar = binding.toolbar
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.about_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.chooseTheme -> {
                    ThemeDialogFragment().show(childFragmentManager, tag)
                }
            }
            true
        }

        val type = BuildConfig.BUILD_TYPE.replaceFirstChar { it.uppercase() }
        binding.appVersionTV.text =
            getString(
                R.string.version_info,
                type,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preference, rootKey)

        findPreference<Preference>("analyze")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(analyzeURL))
            true
        }

        findPreference<Preference>("alternatives")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(alternativesURL))
            true
        }

        findPreference<Preference>("website")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(getLocaleWebsiteURL()))
            true
        }
        findPreference<Preference>("privPolicy")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(privacyPolicyURL))
            true
        }

        findPreference<Preference>("srcCode")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(sourceCodeURL))
            true
        }

        findPreference<Preference>("twitter")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(twitterURL))
            true
        }

        findPreference<Preference>("mastodon")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(mastodonURL))
            true
        }

        // Open default email app for support
        findPreference<Preference>("email")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("mailto:$emailID")
            try {
                startActivity(intent)
                true
            } catch (e: ActivityNotFoundException) {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
