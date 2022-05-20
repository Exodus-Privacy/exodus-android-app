package org.eu.exodus_privacy.exodusprivacy

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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
            if (it == false && viewModel.policyAgreement.value == true) {
                val intent = Intent(this, ExodusUpdateService::class.java)
                intent.apply {
                    action = ExodusUpdateService.START_SERVICE
                    startService(this)
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.appDetailFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}
