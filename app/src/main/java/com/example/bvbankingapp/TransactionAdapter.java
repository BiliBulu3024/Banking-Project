package com.example.bvbankingapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bvbankingapp.R;

import java.text.DecimalFormat;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionItem> list;
    private DecimalFormat moneyFormat = new DecimalFormat("#,###.00");

    public TransactionAdapter(List<TransactionItem> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TransactionItem t = list.get(position);

        // 🟦 Format type
        String type = t.getType().toUpperCase();
        if (type.equals("DEPOSIT")) {
            holder.tvType.setText("Nạp tiền");
        } else if (type.equals("WITHDRAW")) {
            holder.tvType.setText("Rút tiền");
        } else {
            holder.tvType.setText(type); // fallback
        }

        // 🟦 Format amount
        double amount = t.getAmount();
        String formatted = moneyFormat.format(amount);

        if (type.equals("DEPOSIT")) {
            holder.tvAmount.setText("+ ₫ " + formatted);
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32")); // xanh
        } else {
            holder.tvAmount.setText("- ₫ " + formatted);
            holder.tvAmount.setTextColor(Color.parseColor("#C62828")); // đỏ
        }

        // 🟦 Other fields
        holder.tvDesc.setText(t.getDescription());
        holder.tvTime.setText(t.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvDesc, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDesc = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
