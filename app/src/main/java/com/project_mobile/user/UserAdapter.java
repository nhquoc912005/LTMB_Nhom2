package com.project_mobile.user;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.project_mobile.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<UserModel> users;
    private final UserActionListener listener;

    public interface UserActionListener {
        void onToggleLock(UserModel user);
        void onEdit(UserModel user);
        void onDelete(UserModel user);
    }

    public UserAdapter(List<UserModel> users, UserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void submitList(List<UserModel> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = users.get(position);
        holder.tvDisplayName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone());
        holder.tvRole.setText(user.getRole());
        holder.tvCode.setText(user.getUserCode());
        holder.tvStatus.setText(user.getStatusLabel());
        holder.btnToggleLock.setText(user.getToggleLabel());

        int dotColor = Color.parseColor(user.isLocked() ? "#FF1010" : "#12C85C");
        GradientDrawable dot = (GradientDrawable) holder.statusDot.getBackground().mutate();
        dot.setColor(dotColor);
        holder.statusDot.setBackground(dot);

        if (user.isLocked()) {
            holder.btnToggleLock.setTextColor(Color.parseColor("#08B520"));
            holder.btnToggleLock.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDF8D4")));
        } else {
            holder.btnToggleLock.setTextColor(Color.parseColor("#C0410D"));
            holder.btnToggleLock.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBD2")));
        }

        holder.btnToggleLock.setOnClickListener(v -> listener.onToggleLock(user));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDisplayName;
        private final TextView tvEmail;
        private final TextView tvPhone;
        private final TextView tvRole;
        private final TextView tvCode;
        private final TextView tvStatus;
        private final View statusDot;
        private final MaterialButton btnToggleLock;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDisplayName = itemView.findViewById(R.id.tvUserDisplayName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPhone = itemView.findViewById(R.id.tvUserPhone);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvCode = itemView.findViewById(R.id.tvUserCode);
            tvStatus = itemView.findViewById(R.id.tvUserStatus);
            statusDot = itemView.findViewById(R.id.viewStatusDot);
            btnToggleLock = itemView.findViewById(R.id.btnToggleLock);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
