package com.project_mobile.checkout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.text.DecimalFormat;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private Context context;
    private List<CheckoutBill> billList;
    private OnCheckoutClickListener listener;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    public interface OnCheckoutClickListener {
        void onCheckoutClick(CheckoutBill bill);
    }

    public CheckoutAdapter(Context context, List<CheckoutBill> billList, OnCheckoutClickListener listener) {
        this.context = context;
        this.billList = billList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_card, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CheckoutBill bill = billList.get(position);

        holder.tvCustomerName.setText(bill.getRoomModel().getCustomerName());
        holder.tvRoomInfo.setText("Phòng " + bill.getRoomModel().getRoomNumber());
        holder.tvPhone.setText(bill.getRoomModel().getCustomerPhone());
        holder.tvEmail.setText(bill.getCustomerEmail());
        holder.tvDate.setText(bill.getCheckInDate() + " - " + bill.getCheckOutDate());

        holder.tvTotalGuests.setText("Tổng số người: " + bill.getTotalGuests());
        holder.tvAdults.setText("Người lớn: " + bill.getAdults());
        holder.tvChildren.setText("Trẻ em: " + bill.getChildren());

        holder.tvRoomFee.setText(bill.getRoomModel().getPrice());
        holder.tvServiceFee.setText(formatter.format(bill.getServiceFee()));
        holder.tvTotalFee.setText(formatter.format(bill.getTotalFee()));

        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onCheckoutClick(bill);
        });
    }

    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvRoomInfo, tvPhone, tvEmail, tvDate, tvRoomFee, tvServiceFee, tvTotalFee;
        TextView tvTotalGuests, tvAdults, tvChildren;
        Button btnPay;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvRoomInfo = itemView.findViewById(R.id.tvRoomNumber); // Đã sửa từ tvRoomInfo -> tvRoomNumber cho khớp XML
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvDate = itemView.findViewById(R.id.tvDateRange); // Đã sửa từ tvDate -> tvDateRange cho khớp XML

            tvTotalGuests = itemView.findViewById(R.id.tvTotalGuests);
            tvAdults = itemView.findViewById(R.id.tvAdultCount); // Đã sửa từ tvAdults -> tvAdultCount cho khớp XML
            tvChildren = itemView.findViewById(R.id.tvChildCount); // Đã sửa từ tvChildren -> tvChildCount cho khớp XML

            tvRoomFee = itemView.findViewById(R.id.tvRoomFee);
            tvServiceFee = itemView.findViewById(R.id.tvServiceFee);
            tvTotalFee = itemView.findViewById(R.id.tvTotalFee);
            btnPay = itemView.findViewById(R.id.btnPayAndCheckout);
        }
    }
}
