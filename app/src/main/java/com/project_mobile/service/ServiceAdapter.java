package com.project_mobile.service;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceModel> serviceList;
    private OnServiceClickListener listener;
    private boolean isServiceType = true;

    public interface OnServiceClickListener {
        void onEditClick(ServiceModel service);
        void onDeleteClick(ServiceModel service);
    }

    public ServiceAdapter(List<ServiceModel> serviceList, OnServiceClickListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    public void submitList(List<ServiceModel> items) {
        this.serviceList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void setServiceType(boolean isServiceType) {
        this.isServiceType = isServiceType;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = serviceList.get(position);
        holder.tvName.setText(service.getName());
        holder.tvPrice.setText(service.getFormattedPrice());

        // Thay đổi icon dựa trên loại (Dịch vụ hoặc Bồi thường)
        if (isServiceType) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_agenda);
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_manage);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(service));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(service));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageButton btnEdit, btnDelete;
        ImageView ivIcon;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivIcon = itemView.findViewById(R.id.ivServiceIcon);
        }
    }
}
