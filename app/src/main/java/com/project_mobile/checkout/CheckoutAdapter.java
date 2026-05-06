// Module trả phòng Android.
// File này bind danh sách CheckoutBill lên RecyclerView màn Trả phòng.
// Dữ liệu chính là thông tin khách, phòng, phí và số tiền cần thanh toán/hoàn.
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

/**
 * CheckoutAdapter hiển thị mỗi phòng đang lưu trú như một card thanh toán.
 * Adapter chỉ phát sự kiện bấm thanh toán về CheckoutFragment.
 */
public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private final Context context;
    private final List<CheckoutBill> billList;
    private final OnCheckoutClickListener listener;
    private final DecimalFormat formatter = new DecimalFormat("###,###,###");

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
    /** Bind thông tin hóa đơn tạm tính và quyết định nhãn cần thanh toán hay hoàn lại. */
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

        holder.tvRoomFee.setText(formatMoney(bill.getRoomFee()));
        holder.tvServiceFee.setText(formatMoney(bill.getServiceFee()));
        holder.tvDamageFee.setText(formatMoney(bill.getDamageFee()));
        holder.tvTotalFee.setText(formatMoney(bill.getGrossTotal()));
        holder.tvDeposit.setText(formatMoney(bill.getDeposit()));
        // Nếu cọc vượt tổng phí, card chuyển sang trạng thái hoàn lại thay vì cần thanh toán.
        if (bill.getRefundAmount() > 0) {
            holder.tvAmountDueLabel.setText("Hoàn lại:");
            holder.tvAmountDue.setText(formatMoney(bill.getRefundAmount()));
        } else {
            holder.tvAmountDueLabel.setText("Cần thanh toán:");
            holder.tvAmountDue.setText(formatMoney(bill.getAmountDue()));
        }

        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onCheckoutClick(bill);
        });
    }

    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    private String formatMoney(double value) {
        return formatter.format(value) + "đ";
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvRoomInfo, tvPhone, tvEmail, tvDate, tvRoomFee, tvServiceFee, tvDamageFee, tvTotalFee;
        TextView tvDeposit, tvAmountDueLabel, tvAmountDue;
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
            tvDamageFee = itemView.findViewById(R.id.tvDamageFee);
            tvTotalFee = itemView.findViewById(R.id.tvTotalFee);
            tvDeposit = itemView.findViewById(R.id.tvDeposit);
            tvAmountDueLabel = itemView.findViewById(R.id.tvAmountDueLabel);
            tvAmountDue = itemView.findViewById(R.id.tvAmountDue);
            btnPay = itemView.findViewById(R.id.btnPayAndCheckout);
        }
    }
}
