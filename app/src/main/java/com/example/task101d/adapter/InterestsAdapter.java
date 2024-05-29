package com.example.task101d.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.model.InterestItem;
import com.example.task101d.R;

import java.util.List;

public class InterestsAdapter extends RecyclerView.Adapter<InterestsAdapter.ViewHolder> {
    private final List<InterestItem> interests;
    private final Context context;
    private static ItemClickListener clickListener;

    public InterestsAdapter(List<InterestItem> interests, Context context) {
        this.interests = interests;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_interest, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterestItem item = interests.get(position);
        holder.tvTitle.setText(item.getTopic());
        if (item.getSelected()) {
            holder.itemView.setBackgroundResource(R.drawable.panel_accent);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.accent20));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.panel_primary);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primary90));
        }
    }

    @Override
    public int getItemCount() {
        return interests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onItemClick(v, getAdapterPosition());

                // Toggle selection state and notify item changed to update background color
                interests.get(getAdapterPosition()).getSelected();
                notifyItemChanged(getAdapterPosition());
            }
        }
    }

    // Method for getting item at click position
    public InterestItem getItem(int position) {
        return interests.get(position);
    }

    // Allow click events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    // Implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View v, int position);
    }
}
