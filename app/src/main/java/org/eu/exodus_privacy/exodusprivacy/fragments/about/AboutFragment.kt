package org.eu.exodus_privacy.exodusprivacy.fragments.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
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
import org.eu.exodus_privacy.exodusprivacy.utils.openURL
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
        private const val mastodonURL = "https://mastodon.social/@exodus@framapiaf.org"
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (toolbar.menu.findItem(R.id.chooseLanguage)).isVisible = true
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.chooseLanguage -> {
                    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                        data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    }
                    try {
                        startActivity(intent)
                        true
                    } catch (e: ActivityNotFoundException) {
                        false
                    }
                }

                R.id.chooseTheme -> {
                    ThemeDialogFragment().show(childFragmentManager, tag)
                }
            }
            true
        }

        binding.appVersionTV.text =
            getString(
                R.string.version_info,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
            )
        binding.appVersionTV.setOnClickListener {
            Toast.makeText(context, "Thanks for support ❤", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preference, rootKey)

        findPreference<Preference>("alternatives")?.setOnPreferenceClickListener {
            openURL(customTabsIntent, it.context, alternativesURL)
            true
        }

        findPreference<Preference>("website")?.setOnPreferenceClickListener {
            openURL(customTabsIntent, it.context, getLocaleWebsiteURL())
            true
        }

        findPreference<Preference>("mastodon")?.setOnPreferenceClickListener {
            openURL(customTabsIntent, it.context, mastodonURL)
            true
        }

        // Open default email app for support
        findPreference<Preference>("email")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$emailID"))
            try {
                startActivity(intent)
                true
            } catch (e: ActivityNotFoundException) {
                false
            }
        }

        findPreference<Preference>("analyze")?.setOnPreferenceClickListener {
            openURL(customTabsIntent, it.context, analyzeURL)
            true
        }

        findPreference<Preference>("privPolicy")?.setOnPreferenceClickListener {
            openURL(customTabsIntent, it.context, privacyPolicyURL)
            true
        }

        findPreference<Preference>("srcCode")?.setOnPreferenceClickListener {
            openURL(customTabsIntent, it.context, sourceCodeURL)
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
