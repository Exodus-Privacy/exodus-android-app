package org.eu.exodus_privacy.exodusprivacy.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.TrackerItemBinding;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.Set;

public class TrackerListAdapter extends RecyclerView.Adapter<TrackerListAdapter.TrackerListViewHolder>{

    private Set<Tracker> trackersList;
    private int layout;

    public TrackerListAdapter(Set<Tracker> trackerList, int resource) {
        setTrackers(trackerList);
        layout = resource;
    }

    @NonNull
    @Override
    public TrackerListAdapter.TrackerListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),layout,parent,false);
        return new TrackerListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackerListAdapter.TrackerListViewHolder holder, int position) {
        if(trackersList == null || trackersList.size() == 0)
            holder.setupData(null);
        else
            holder.setupData((Tracker) trackersList.toArray()[position]);
    }

    @Override
    public int getItemCount() {
        if(trackersList == null || trackersList.size() == 0)
            return 1;
        else
            return trackersList.size();
    }

    public void setTrackers(Set<Tracker> trackers) {
        trackersList = trackers;
    }

    class TrackerListViewHolder extends RecyclerView.ViewHolder {

        ViewDataBinding viewDataBinding;

        TrackerListViewHolder(ViewDataBinding dataBinding) {
            super(dataBinding.getRoot());
            viewDataBinding = dataBinding;
        }

        void setupData(Tracker tracker) {
            if(viewDataBinding instanceof TrackerItemBinding) {
                TrackerItemBinding binding = (TrackerItemBinding) viewDataBinding;
                if(tracker != null)
                    binding.trackerName.setText(tracker.name);
                else
                    binding.trackerName.setText(R.string.no_trackers);
            }

        }
    }
}

