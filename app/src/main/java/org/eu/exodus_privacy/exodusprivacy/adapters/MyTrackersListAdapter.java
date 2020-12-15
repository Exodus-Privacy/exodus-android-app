package org.eu.exodus_privacy.exodusprivacy.adapters;
/*
 * Copyright (C) 2020  Thomas Schneider
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

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.MyTrackerItemBinding;
import org.eu.exodus_privacy.exodusprivacy.objects.MyTracker;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.ArrayList;
import java.util.List;


public class MyTrackersListAdapter extends RecyclerView.Adapter<MyTrackersListAdapter.TrackerListViewHolder> {

    private final TrackerClickListener trackerClickListener;
    private final List<MyTracker> myTrackers;
    private List<Tracker> trackersList;

    public MyTrackersListAdapter(List<MyTracker> mTrackers, TrackerClickListener listener) {
        myTrackers = mTrackers;
        setTrackers(mTrackers);
        trackerClickListener = listener;

    }

    @NonNull
    @Override
    public MyTrackersListAdapter.TrackerListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyTrackerItemBinding itemBinding = MyTrackerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TrackerListViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyTrackersListAdapter.TrackerListViewHolder holder, int position) {
        Tracker tracker = trackersList.get(position);
        if (tracker != null) {
            holder.viewDataBinding.trackerName.setText(tracker.name);
            holder.viewDataBinding.trackerCount.setText(String.valueOf(countOccurences(tracker.codeSignature)));
            holder.viewDataBinding.getRoot().setOnClickListener(v -> trackerClickListener.onTrackerClick(tracker.id));
        } else
            holder.viewDataBinding.trackerName.setText(R.string.no_trackers);
    }

    @Override
    public int getItemCount() {
        return trackersList.size();
    }

    public void setTrackers(List<MyTracker> myTrackers) {
        trackersList = new ArrayList<>();
        if (myTrackers != null) {
            for (MyTracker myTracker : myTrackers) {
                trackersList.add(myTracker.tracker);
            }
        }
    }

    private int countOccurences(String signature) {
        if (myTrackers == null) return 0;
        for (MyTracker myTracker : myTrackers) {
            if (myTracker.signature.compareTo(signature) == 0) return myTracker.number;
        }
        return 0;
    }

    public interface TrackerClickListener {
        void onTrackerClick(long trackerId);
    }

    static class TrackerListViewHolder extends RecyclerView.ViewHolder {

        MyTrackerItemBinding viewDataBinding;

        TrackerListViewHolder(MyTrackerItemBinding dataBinding) {
            super(dataBinding.getRoot());
            viewDataBinding = dataBinding;
        }

    }
}

