/*
 * Copyright (C) 2018  Anthony Chomienne, anthony@mob-dev.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.eu.exodus_privacy.exodusprivacy;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.databinding.MainBinding;
import org.eu.exodus_privacy.exodusprivacy.fragments.HomeFragment;
import org.eu.exodus_privacy.exodusprivacy.fragments.ReportFragment;
import org.eu.exodus_privacy.exodusprivacy.fragments.Updatable;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ReportFragment report;
    private List<Updatable> fragments;
    private SearchView searchView;
    private Menu toolbarMenu;
    private MenuItem settingsMenuItem;
    private String packageName;
    private MainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.main);
        final MainBinding mainBinding = binding;
        fragments = new ArrayList<>();

        NetworkListener networkListener = new NetworkListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    for(Updatable updatable : fragments){

                        updatable.onUpdateComplete();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    for(Updatable updatable : fragments){
                        updatable.onUpdateComplete();
                    }
                    Snackbar bar = Snackbar.make(mainBinding.fragmentContainer,error,Snackbar.LENGTH_LONG);
                    bar.show();
                });
            }

            @Override
            public void onProgress(int resourceId, int progress, int maxProgress) {
                //do nothing here
            }
        };

        ApplicationListAdapter.OnAppClickListener onAppClickListener = vm -> {
            try {
                PackageManager pm = getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(vm.packageName, PackageManager.GET_PERMISSIONS);

                report = ReportFragment.newInstance(pm,packageInfo);
                fragments.add(report);
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_left)
                        .replace(R.id.fragment_container,report)
                        .addToBackStack(null)
                        .commit();

                packageName = packageInfo.packageName;

                searchView.clearFocus();
                if (toolbarMenu != null)
                    (toolbarMenu.findItem(R.id.action_filter)).collapseActionView();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(mainBinding.fragmentContainer.getWindowToken(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        };

        HomeFragment home = new HomeFragment();
        fragments.add(home);
        home.setNetworkListener(networkListener);
        home.setOnAppClickListener(onAppClickListener);


        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container,home)
        .commit();
        home.startRefresh();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finish();
        else {
            getSupportFragmentManager().popBackStack();
            fragments.remove(fragments.size()-1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbarMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        searchView = (SearchView) menu.findItem(R.id.action_filter).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                HomeFragment home = (HomeFragment) fragments.get(0);
                home.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                HomeFragment home = (HomeFragment) fragments.get(0);
                home.filter(newText);
                return true;
            }
        });

        settingsMenuItem = menu.findItem(R.id.action_settings);
        Updatable fragment = fragments.get(fragments.size()-1);
        if (fragment instanceof ReportFragment)
            settingsMenuItem.setVisible(true);
        else
            settingsMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package",packageName,null));
            try {
                startActivity(intent);
            } catch(android.content.ActivityNotFoundException e) {
                Snackbar bar = Snackbar.make(binding.fragmentContainer,R.string.no_settings,Snackbar.LENGTH_LONG);
                bar.show();
            }
            return true;
        }
        return false;
    }
}
