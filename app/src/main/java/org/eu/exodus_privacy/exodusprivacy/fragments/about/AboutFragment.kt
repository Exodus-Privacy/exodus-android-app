package org.eu.exodus_privacy.exodusprivacy.fragments.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.BuildConfig
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentAboutBinding

@AndroidEntryPoint
class AboutFragment : PreferenceFragmentCompat() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val privacyPolicyURL = "https://exodus-privacy.eu.org/en/page/privacy-policy"
        private const val sourceCodeURL = "https://github.com/Exodus-Privacy/exodus-android-app"
        private const val websiteURL = "https://exodus-privacy.eu.org"
        private const val twitterURL = "https://twitter.com/ExodusPrivacy"
        private const val emailID = "contact@exodus-privacy.eu.org"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)

        val type = BuildConfig.BUILD_TYPE.replaceFirstChar { it.uppercase() }
        binding.appVersionTV.text =
            getString(R.string.version_info, type, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preference, rootKey)

        // Set appropriate CustomTabsIntent for respective preferences
        val customTabsIntent = CustomTabsIntent.Builder().build()

        findPreference<Preference>("website")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(websiteURL))
            true
        }

        findPreference<Preference>("srcCode")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(sourceCodeURL))
            true
        }

        findPreference<Preference>("privPolicy")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(privacyPolicyURL))
            true
        }

        findPreference<Preference>("twitter")?.setOnPreferenceClickListener {
            customTabsIntent.launchUrl(it.context, Uri.parse(twitterURL))
            true
        }

        // Open default email app for support
        findPreference<Preference>("email")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("mailto:$emailID")
            startActivity(intent)
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
