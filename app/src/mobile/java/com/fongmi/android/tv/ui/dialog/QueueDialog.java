package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fongmi.android.tv.bean.Queue;
import com.fongmi.android.tv.databinding.DialogSiteBinding;
import com.fongmi.android.tv.service.QueueService;
import com.fongmi.android.tv.ui.adapter.QueueAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class QueueDialog implements QueueAdapter.OnClickListener {

    private DialogSiteBinding binding;
    private QueueAdapter adapter;
    private AlertDialog dialog;
    private OnQueueListener listener;

    public interface OnQueueListener {
        void onQueuePlay(Queue item);
    }

    public static QueueDialog create(Activity activity) {
        return new QueueDialog(activity);
    }

    public static QueueDialog create(Fragment fragment) {
        return new QueueDialog(fragment);
    }

    public QueueDialog(Activity activity) {
        init(activity);
    }

    public QueueDialog(Fragment fragment) {
        init(fragment.requireActivity());
    }

    private void init(Activity activity) {
        this.binding = DialogSiteBinding.inflate(LayoutInflater.from(activity));
        this.dialog = new MaterialAlertDialogBuilder(activity).setView(binding.getRoot()).create();
        this.adapter = new QueueAdapter(this);
    }

    public QueueDialog setListener(OnQueueListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {
        setRecyclerView();
        setDialog();
    }

    private void setRecyclerView() {
        binding.recycler.setAdapter(adapter);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 8));
        adapter.setItems(QueueService.get().getAll());
    }

    private void setDialog() {
        if (adapter.getItemCount() == 0) return;
        dialog.getWindow().setDimAmount(0);
        dialog.show();
    }

    @Override
    public void onItemClick(Queue item) {
        if (listener != null) listener.onQueuePlay(item);
        dialog.dismiss();
    }

    @Override
    public void onItemDelete(Queue item) {
        QueueService.get().remove(item.getId());
        adapter.setItems(QueueService.get().getAll());
    }
}