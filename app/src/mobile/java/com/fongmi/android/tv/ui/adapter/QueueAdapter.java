package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Queue;
import com.fongmi.android.tv.databinding.QueueItemBinding;
import com.fongmi.android.tv.utils.ImgUtil;

public class QueueAdapter extends BaseDiffAdapter<Queue, QueueAdapter.ViewHolder> {

    private final OnClickListener listener;

    public QueueAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onItemClick(Queue item);
        void onItemDelete(Queue item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(QueueItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Queue item = getItem(position);
        holder.binding.name.setText(item.getVodName());
        holder.binding.episode.setText(item.getEpisodeName());
        ImgUtil.load(item.getVodName(), item.getVodPic(), holder.binding.image);
        holder.binding.getRoot().setOnClickListener(view -> listener.onItemClick(item));
        holder.binding.delete.setOnClickListener(view -> listener.onItemDelete(item));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final QueueItemBinding binding;

        ViewHolder(@NonNull QueueItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}