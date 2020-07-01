package org.eu.exodus_privacy.exodusprivacy.adapters;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import org.eu.exodus_privacy.exodusprivacy.objects.Report;
import org.eu.exodus_privacy.exodusprivacy.objects.Tracker;

import java.util.Set;

/**
 * This class holds the data needed to display an application cell in the RecyclerView
 */
public class ApplicationViewModel {
    public String packageName;
    public String versionName;
    public int versionCode;
    public String[] requestedPermissions;
    public @Nullable
    Report report;
    public Set<Tracker> trackers;
    public @Nullable
    Drawable icon;
    public CharSequence label;
    public String installerPackageName;
    public boolean isVisible;
    public String source;
}
