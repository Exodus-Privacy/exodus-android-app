package org.eu.exodus_privacy.exodusprivacy

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private val permission = "android.permission.POST_NOTIFICATIONS"
    private val REQUEST_CODE_POST_NOTIFICATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {

        val config = viewModel.config
        // Handle the splash screen transition
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "ContentView was set.")

        val bottomNavigationView = binding.bottomNavView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        // Show or hide the connection message depending on the network
        viewModel.networkConnection.observe(this) { connected ->
            Log.d(TAG, "Observing Network Connection.")
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

        if (config["privacy_policy"]?.enable!!) {
            Log.d(TAG, "Policy Agreement was: ${viewModel.config["privacy_policy"]?.enable!!}")
            ExodusDialogFragment().apply {
                this.isCancelable = false
                this.show(supportFragmentManager, TAG)
            }
        }

        if (!isNotificationPermissionGranted() &&
            config["notification_perm"]?.enable!!) {
            requestNotificationPermission()
        }

        // Populate trackers in database
        if (config["app_setup"]?.enable!! &&
            config["privacy_policy"]?.enable!! &&
            !ExodusUpdateService.IS_SERVICE_RUNNING) {
            val intent = Intent(this, ExodusUpdateService::class.java)
            intent.apply {
                action = ExodusUpdateService.FIRST_TIME_START_SERVICE
                startService(this)
            }
            viewModel.saveAppSetup(true)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_POST_NOTIFICATION -> {
                viewModel.saveNotificationPermission(true)
            }
            else -> {
                viewModel.saveNotificationPermission(false)
            }
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        // Check if permission is granted
        return ContextCompat.checkSelfPermission(this,permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        Log.d("MainActivity", "Requesting Notification Permission.")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            REQUEST_CODE_POST_NOTIFICATION
        )
    }
}
