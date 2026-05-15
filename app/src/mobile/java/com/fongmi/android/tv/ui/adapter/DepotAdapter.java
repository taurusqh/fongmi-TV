package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Depot;
import com.fongmi.android.tv.databinding.DepotItemBinding;

public class DepotAdapter extends BaseDiffAdapter<Depot, DepotAdapter.ViewHolder> {

    private final OnClickListener listener;

    public DepotAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onItemClick(Depot item);
        void onItemDelete(Depot item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DepotItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Depot item = getItem(position);
        holder.binding.name.setText(item.getName());
        holder.binding.url.setText(item.getUrl());
        holder.binding.defaultIcon.setVisibility(item.isDefault() ? View.VISIBLE : View.GONE);
        holder.binding.delete.setVisibility(item.isDefault() ? View.GONE : View.VISIBLE);
        holder.binding.getRoot().setOnClickListener(view -> listener.onItemClick(item));
        holder.binding.delete.setOnClickListener(view -> listener.onItemDelete(item));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final DepotItemBinding binding;

        ViewHolder(@NonNull DepotItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}