package org.eu.exodus_privacy.exodusprivacy

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.databinding.ActivityMainBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.dialog.ExodusDialogFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.simpleName

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        // Show or hide the connection message depending on the network
        viewModel.networkConnection.observe(this) { connected ->
            if (!connected) {
                Snackbar
                    .make(
                        binding.fragmentCoordinator,
                        R.string.not_connected,
                        Snackbar.LENGTH_LONG
                    )
                    .setAction(R.string.settings) {
                        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                    .show()
            }
        }

        viewModel.policyAgreement.observe(this) {
            if (it == false) {
                ExodusDialogFragment().apply {
                    this.isCancelable = false
                    this.show(supportFragmentManager, TAG)
                }
            }
        }

        // Populate trackers in database
        viewModel.appSetup.observe(this) {
            if (it == false && viewModel.policyAgreement.value == true && !ExodusUpdateService.IS_SERVICE_RUNNING) {
                val intent = Intent(this, ExodusUpdateService::class.java)
                intent.apply {
                    action = ExodusUpdateService.FIRST_TIME_START_SERVICE
                    startService(this)
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.appDetailFragment,
                R.id.trackerDetailFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}
