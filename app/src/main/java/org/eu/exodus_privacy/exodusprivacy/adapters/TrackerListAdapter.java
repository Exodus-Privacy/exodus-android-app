package org.eu.exodus_privacy.exodusprivacy.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.TrackerItemBinding;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class TrackerListAdapter extends RecyclerView.Adapter<TrackerListAdapter.TrackerListViewHolder>{

    private List<Tracker> trackersList;
    private OnTrackerClickListener trackerClickListener;
    private int layout;

    public TrackerListAdapter(Set<Tracker> trackerList, int resource, OnTrackerClickListener listener) {
        setTrackers(trackerList);
        layout = resource;
        trackerClickListener = listener;
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
            holder.setupData(trackersList.get(position));
    }

    @Override
    public int getItemCount() {
        if(trackersList == null || trackersList.size() == 0)
            return 1;
        else
            return trackersList.size();
    }

    private Comparator<Tracker> alphaTrackerComparator = (track1, track2) -> track1.name.compareToIgnoreCase(track2.name);

    public void setTrackers(Set<Tracker> trackers) {
        if(trackers != null) {
            trackersList = new ArrayList<>(trackers);
            Collections.sort(trackersList, alphaTrackerComparator);
        }
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
                if(tracker != null) {
                    binding.trackerName.setText(tracker.name + " ➤");
                    binding.getRoot().setOnClickListener(v -> {
                        trackerClickListener.onTrackerClick(tracker.id);
                    });
                }
                else
                    binding.trackerName.setText(R.string.no_trackers);
            }

        }
    }

    public interface OnTrackerClickListener{
        public void onTrackerClick(long trackerId);
    }
}

