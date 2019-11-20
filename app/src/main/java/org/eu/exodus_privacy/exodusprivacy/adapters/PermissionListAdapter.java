package org.eu.exodus_privacy.exodusprivacy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.exodus_privacy.exodusprivacy.R;
import org.eu.exodus_privacy.exodusprivacy.databinding.PermissionItemBinding;
import org.eu.exodus_privacy.exodusprivacy.objects.Permission;

import java.util.List;

public class PermissionListAdapter extends RecyclerView.Adapter<PermissionListAdapter.TrackerListViewHolder>{

    private List<Permission> permissionList;

    public PermissionListAdapter(List<Permission> permissions) {
        setPermisions(permissions);
    }

    @NonNull
    @Override
    public PermissionListAdapter.TrackerListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PermissionItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),R.layout.permission_item,parent,false);
        return new TrackerListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionListAdapter.TrackerListViewHolder holder, int position) {
        if(permissionList == null || permissionList.size() == 0)
            holder.setupData(null);
        else
            holder.setupData(permissionList.get(position));
    }

    @Override
    public int getItemCount() {
        if(permissionList == null || permissionList.size() == 0)
            return 0;
        else
            return permissionList.size();
    }

    public void setPermisions(List<Permission> permisions) {
        permissionList = permisions;
    }


    class TrackerListViewHolder extends RecyclerView.ViewHolder {

        PermissionItemBinding permissionItemBinding;

        TrackerListViewHolder(PermissionItemBinding dataBinding) {
            super(dataBinding.getRoot());
            permissionItemBinding = dataBinding;
        }

        void setupData(Permission permission) {
            if(permission != null) {
                if(permission.name != null) {
                    permissionItemBinding.permissionShort.setText(permission.name);
                    permissionItemBinding.permissionShort.setVisibility(View.VISIBLE);
                }
                else
                    permissionItemBinding.permissionShort.setVisibility(View.GONE);

                permissionItemBinding.permissionName.setText(permission.fullName.substring(permission.fullName.lastIndexOf(".")+1));
                permissionItemBinding.permissionDescription.setText(permission.description);
                if(permission.icon != null)
                    permissionItemBinding.icon.setImageDrawable(permission.icon);
                if(!permission.dangerous)
                    permissionItemBinding.dangerous.setVisibility(View.GONE);
                else
                    permissionItemBinding.dangerous.setVisibility(View.VISIBLE);
                manageExpanded(permission);
                permissionItemBinding.mainLayout.setOnClickListener((View.OnClickListener) v -> {
                    if( permission.description != null && permission.description.trim().length() > 0) {
                        permission.expanded = !permission.expanded;
                        manageExpanded(permission);
                    }
                });

            }
            else {
                permissionItemBinding.permissionName.setText(R.string.no_permissions);
                permissionItemBinding.arrow.setText(" ");
                permissionItemBinding.permissionShort.setVisibility(View.GONE);
                permissionItemBinding.dangerous.setVisibility(View.GONE);
                permissionItemBinding.permissionDescription.setVisibility(View.GONE);
            }

        }

        void manageExpanded(Permission permission) {
            if(permission.expanded) {
                permissionItemBinding.arrow.setText("▼");
                permissionItemBinding.permissionDescription.setVisibility(View.VISIBLE);
            } else {
                if( permission.description != null && permission.description.trim().length() > 0 )
                    permissionItemBinding.arrow.setText("▶");
                else
                    permissionItemBinding.arrow.setText("");
                permissionItemBinding.permissionDescription.setVisibility(View.GONE);

            }
        }
    }
}

