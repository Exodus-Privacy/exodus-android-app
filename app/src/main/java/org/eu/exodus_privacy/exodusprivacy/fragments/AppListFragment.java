package org.eu.exodus_privacy.exodusprivacy.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationListAdapter;
import org.eu.exodus_privacy.exodusprivacy.adapters.ApplicationViewModel;
import org.eu.exodus_privacy.exodusprivacy.databinding.ApplistBinding;

import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends Fragment {

    private ApplistBinding applistBinding;
    private List<ApplicationViewModel> applications;
    private ApplicationListAdapter adapter;
    private ApplicationListAdapter.OnAppClickListener onAppClickListener;
    private Type filterType = Type.NAME;
    private Object filterObject = "";
    private boolean scrollbarEnabled = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //create binding
        applistBinding = DataBindingUtil.inflate(inflater, R.layout.applist, container, false);
        //init variables
        if (applications == null)
            applications = new ArrayList<>();
        Context context = applistBinding.getRoot().getContext();
        //configure list
        applistBinding.appList.setLayoutManager(new LinearLayoutManager(context));
        applistBinding.appList.setVerticalScrollBarEnabled(scrollbarEnabled);
        adapter = new ApplicationListAdapter(onAppClickListener);
        adapter.displayAppList(applications);
        adapter.filter(filterType, filterObject);
        applistBinding.appList.setAdapter(adapter);
        return applistBinding.getRoot();
    }

    public void setOnAppClickListener(ApplicationListAdapter.OnAppClickListener listener) {
        onAppClickListener = listener;
    }

    public void setApplications(List<ApplicationViewModel> applicationList) {
        applications = applicationList;
        if (adapter != null)
            adapter.displayAppList(applications);
    }

    public void setFilter(Type type, Object filter) {
        filterType = type;
        filterObject = filter;
        if (adapter != null)
            adapter.filter(type, filterObject);
    }

    public void disableScrollBar() {
        scrollbarEnabled = false;
        if (applistBinding != null)
            applistBinding.appList.setVerticalScrollBarEnabled(false);
    }

    public void enableScrollBar() {
        scrollbarEnabled = true;
        if (applistBinding != null)
            applistBinding.appList.setVerticalScrollBarEnabled(true);
    }

    public int getTotalApps() {
        if (adapter == null)
            return 0;
        return adapter.getItemCount();
    }

    public int getDisplayedApps() {
        if (adapter == null)
            return 0;
        return adapter.getDisplayedApps();
    }

    public enum Type {
        NAME,
        TRACKER
    }

    public enum Order {
        NAME_ASC,
        NAME_DSC,
        TRACKER_ASC,
        TRACKER_DSC,
        PERMISSIONS_ASC,
        PERMISSIONS_DSC
    }
}
