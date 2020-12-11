package org.eu.exodus_privacy.exodusprivacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.databinding.AppCheckActivityBinding;
import org.eu.exodus_privacy.exodusprivacy.fragments.Updatable;
import org.eu.exodus_privacy.exodusprivacy.listener.NetworkListener;
import org.eu.exodus_privacy.exodusprivacy.manager.DatabaseManager;
import org.eu.exodus_privacy.exodusprivacy.manager.NetworkManager;
import org.eu.exodus_privacy.exodusprivacy.objects.Application;
import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.ReportDisplay;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckAppActivity extends AppCompatActivity implements NetworkListener {

    private static final Pattern fdroidRegex = Pattern.compile("https?://f-droid\\.org/[\\w-]+/packages/([\\w.-]+)");
    private static final Pattern googleRegex = Pattern.compile("https?://play\\.google\\.com/store/apps/details\\?id=([\\w.-]+)");

    ArrayList<Updatable> fragments;
    AppCheckActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (extraText != null) {
            Matcher matcher = fdroidRegex.matcher(extraText);
            String app_id = null;
            while (matcher.find()) {
                app_id = matcher.group(1);
            }
            if (app_id == null) {
                matcher = googleRegex.matcher(extraText);
                while (matcher.find()) {
                    app_id = matcher.group(1);
                }
            }

            fragments = new ArrayList<>();
            NetworkManager.getInstance().getSingleReport(CheckAppActivity.this, this, app_id);

        }


    }

    @Override
    public void onSuccess(Application application) {
        runOnUiThread(() -> {
            ApplicationViewModel applicationViewModel = new ApplicationViewModel();

            applicationViewModel.packageName = application.packageName;
            applicationViewModel.label = application.name;

            Report reportToKeep = null;
            long versionCode = -1;
            for (Report report : application.reports) {
                if (versionCode == -1) {
                    reportToKeep = report;
                    versionCode = report.versionCode;
                } else if (report.versionCode > versionCode) {
                    reportToKeep = report;
                    versionCode = report.versionCode;
                }
            }
            applicationViewModel.report = reportToKeep;
            applicationViewModel.source = reportToKeep.source;
            applicationViewModel.trackers = DatabaseManager.getInstance(CheckAppActivity.this).getTrackers(reportToKeep.trackers);

            ReportDisplay reportDisplay = ReportDisplay.buildReportDisplay(CheckAppActivity.this, applicationViewModel, null, null);
            ReportViewModel viewModel = new ReportViewModel();
            viewModel.setReportDisplay(reportDisplay);

            binding = AppCheckActivityBinding.inflate(getLayoutInflater());
            binding.setReportInfo(viewModel);
            View viewRoot = binding.getRoot();
            setContentView(viewRoot);
        });
    }

    @Override
    public void onError(String error) {
    }

    @Override
    public void onProgress(int resourceId, int progress, int maxProgress) {
    }
}
