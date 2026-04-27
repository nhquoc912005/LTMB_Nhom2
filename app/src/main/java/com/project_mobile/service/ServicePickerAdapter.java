package com.project_mobile.service;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project_mobile.R;
import com.project_mobile.network.ApiModels.CatalogItemDto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicePickerAdapter extends RecyclerView.Adapter<ServicePickerAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CatalogItemDto item);
    }

    private final List<CatalogItemDto> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public ServicePickerAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CatalogItemDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CatalogItemDto item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvPrice.setText(formatMoney(item.price != null ? item.price : 0));
        
        // Hide edit/delete buttons as this is a picker
        if (holder.btnEdit != null) holder.btnEdit.setVisibility(View.GONE);
        if (holder.btnDelete != null) holder.btnDelete.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatMoney(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(Math.round(value)) + "đ";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        View btnEdit, btnDelete;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivIcon = itemView.findViewById(R.id.ivServiceIcon);
        }
    }
}
