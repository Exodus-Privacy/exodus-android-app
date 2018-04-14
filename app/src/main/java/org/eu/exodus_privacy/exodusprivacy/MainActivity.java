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

import android.support.v4.app.FragmentManager;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.databinding.MainBinding;
import org.eu.exodus_privacy.exodusprivacy.fragments.AppListFragment;
import org.eu.exodus_privacy.exodusprivacy.fragments.ReportFragment;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;

public class MainActivity extends AppCompatActivity {

    AppListFragment appList;
    ReportFragment report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainBinding mainBinding = DataBindingUtil.setContentView(this,R.layout.main);

        NetworkListener networkListener = new NetworkListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    appList.updateComplete();
                    if(report != null)
                        report.updateComplete();

                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    appList.updateComplete();
                    Snackbar bar = Snackbar.make(mainBinding.fragmentContainer,error,Snackbar.LENGTH_LONG);
                    bar.show();
                });
            }

            @Override
            public void onProgress(int resourceId, int progress, int maxProgress) {
                //do nothing here
            }
        };

        ApplicationListAdapter.OnAppClickListener onAppClickListener = packageInfo -> {

            report = ReportFragment.newInstance(getPackageManager(),packageInfo);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.fragment_container,report)
                    .addToBackStack(null)
                    .commit();
        };

        appList = AppListFragment.newInstance(networkListener,onAppClickListener);


        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container,appList)
        .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finish();
        else
            getSupportFragmentManager().popBackStack();
    }
}
