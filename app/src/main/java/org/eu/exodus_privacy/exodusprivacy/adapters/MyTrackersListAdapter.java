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
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.MyTrackerItemBinding;
import org.eu.exodus_privacy.exodusprivacy.objects.MyTracker;

import java.util.List;


public class MyTrackersListAdapter extends RecyclerView.Adapter<MyTrackersListAdapter.TrackerListViewHolder> {

    private final TrackerClickListener trackerClickListener;
    private final List<MyTracker> myTrackers;
    private final int max, installedApps;
    private int viewWidth = 0;

    public MyTrackersListAdapter(List<MyTracker> mTrackers, TrackerClickListener listener, int maxValue, int appInstalled) {
        myTrackers = mTrackers;
        trackerClickListener = listener;
        max = maxValue;
        installedApps = appInstalled;
    }

    @NonNull
    @Override
    public MyTrackersListAdapter.TrackerListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyTrackerItemBinding itemBinding = MyTrackerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TrackerListViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyTrackersListAdapter.TrackerListViewHolder holder, int position) {

        if (myTrackers != null) {
            MyTracker myTracker = myTrackers.get(position);
            holder.viewDataBinding.trackerName.setText(myTracker.tracker.name);
            holder.viewDataBinding.trackerCount.setText(holder.viewDataBinding.trackerCount.getContext().getString(R.string.apps, String.valueOf(myTracker.number)));
            holder.viewDataBinding.getRoot().setOnClickListener(v -> trackerClickListener.onTrackerClick(myTracker.tracker.id));
            float percent = (float) myTracker.number / (float) max;
            int percentApp = myTracker.number * 100 / installedApps;
            holder.viewDataBinding.percent.getLayoutParams().width = (int) (viewWidth * percent);
            holder.viewDataBinding.percentVal.setText(String.format("%s %%", percentApp));
            if (percentApp >= 50)
                holder.viewDataBinding.trackerCount.setBackgroundResource(R.drawable.square_red);
            else if (percentApp >= 33)
                holder.viewDataBinding.trackerCount.setBackgroundResource(R.drawable.square_dark_orange);
            else if (percentApp >= 20)
                holder.viewDataBinding.trackerCount.setBackgroundResource(R.drawable.square_yellow);
            else
                holder.viewDataBinding.trackerCount.setBackgroundResource(R.drawable.square_light_blue);
            holder.viewDataBinding.percent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    holder.viewDataBinding.percent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (viewWidth == 0) {
                        viewWidth = holder.viewDataBinding.percent.getWidth();
                        notifyDataSetChanged();
                    }
                }
            });
        } else
            holder.viewDataBinding.trackerName.setText(R.string.no_trackers);
    }

    @Override
    public int getItemCount() {
        return myTrackers.size();
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

