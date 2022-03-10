package org.eu.exodus_privacy.exodusprivacy

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        val viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

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
                Log.d(TAG, "Refreshing trackers database")
                viewModel.apply {
                    fetchAndSaveTrackers()
                    saveAppSetup(true)
                }
            }
        }
    }
}
