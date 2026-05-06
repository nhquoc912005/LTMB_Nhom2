// Module trang chủ Android.
// File này bind hoạt động gần đây lên RecyclerView trong HomeFragment.
// Dữ liệu chính là RecentActivityModel đã được map từ API dashboard activities.
package com.project_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecentActivityAdapter hiển thị các hoạt động booking/check-out mới nhất.
 * Adapter nhận sẵn màu chữ/nền trạng thái từ HomeFragment để không tự quyết định nghiệp vụ.
 */
public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private final List<RecentActivityModel> activityList;

    public RecentActivityAdapter(List<RecentActivityModel> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivityModel activity = activityList.get(position);
        holder.tvRoomNumber.setText(activity.getRoomNumber());
        holder.tvCustomerName.setText(activity.getCustomerName());
        holder.tvStatus.setText(activity.getStatus());
        holder.tvTime.setText(activity.getTime());

        holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), activity.getStatusColorRes()));
        holder.tvStatus.setBackgroundResource(activity.getStatusBgRes());
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber, tvCustomerName, tvStatus, tvTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tvRecentRoomNumber);
            tvCustomerName = itemView.findViewById(R.id.tvRecentCustomerName);
            tvStatus = itemView.findViewById(R.id.tvRecentStatus);
            tvTime = itemView.findViewById(R.id.tvRecentTime);
        }
    }
}
