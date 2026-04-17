package com.project_mobile.external_data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class ExternalDataAdapter extends RecyclerView.Adapter<ExternalDataAdapter.ViewHolder> {

    private List<ExternalData> dataList;

    public ExternalDataAdapter(List<ExternalData> dataList) {
        this.dataList = dataList;
    }

    public void setDataList(List<ExternalData> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_external_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExternalData data = dataList.get(position);
        holder.tvName.setText(data.getName());
        holder.tvDescription.setText(data.getDescription());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExternalName);
            tvDescription = itemView.findViewById(R.id.tvExternalDescription);
        }
    }
}
