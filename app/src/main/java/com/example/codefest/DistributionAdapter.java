package com.example.codefest;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DistributionAdapter extends RecyclerView.Adapter<DistributionAdapter.MyViewHolder> {

    private List<DistributionRecord> list;
    private final OnAction listener;

    public interface OnAction {
        void onDelete(DistributionRecord r);
    }

    public DistributionAdapter(List<DistributionRecord> list, OnAction listener) {
        this.list = list;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<DistributionRecord> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_distribution, parent, false);
        return new MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder h, int pos) {
        DistributionRecord r = list.get(pos);
        h.name.setText(r.getHouseholdName());

        String summaryText = r.getFamilySize() + " members • Rice: " + r.getRiceDistributed() + " kg, Canned Goods: " + r.getCansDistributed() + " units";
        h.info.setText(summaryText);

        if (r.isPriority()) {
            h.priority.setText("Priority");
            h.priority.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(), R.color.priority_bg));
            h.priority.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.priority_text));
        } else {
            h.priority.setText("Standard");
            h.priority.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(), R.color.normal_bg));
            h.priority.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.normal_text));
        }

        h.time.setText(r.getTimestamp());
        h.btnCancel.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, info, priority, time;
        ImageButton btnCancel;

        MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvHouseholdName);
            info = v.findViewById(R.id.tvSummary);
            priority = v.findViewById(R.id.tvPriority);
            time = v.findViewById(R.id.tvTimestamp);
            btnCancel = v.findViewById(R.id.btnCancel);
        }
    }
}
