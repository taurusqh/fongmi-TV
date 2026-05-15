package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Depot;
import com.fongmi.android.tv.databinding.DialogDepotBinding;
import com.fongmi.android.tv.service.DepotService;
import com.fongmi.android.tv.ui.adapter.DepotAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.fongmi.android.tv.utils.Notify;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// 多仓管理对话框，显示仓库列表，支持添加、切换和删除
public class DepotDialog implements DepotAdapter.OnClickListener {

    private DialogDepotBinding binding;
    private DepotAdapter adapter;
    private AlertDialog dialog;
    private OnDepotListener listener;

    public interface OnDepotListener {
        void onDepotSwitch(Depot item);
    }

    public static DepotDialog create(Activity activity) {
        return new DepotDialog(activity);
    }

    public static DepotDialog create(Fragment fragment) {
        return new DepotDialog(fragment);
    }

    public DepotDialog(Activity activity) {
        init(activity);
    }

    public DepotDialog(Fragment fragment) {
        init(fragment.requireActivity());
    }

    private void init(Activity activity) {
        this.binding = DialogDepotBinding.inflate(LayoutInflater.from(activity));
        this.dialog = new MaterialAlertDialogBuilder(activity).setView(binding.getRoot()).create();
        this.adapter = new DepotAdapter(this);
    }

    public DepotDialog setListener(OnDepotListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {
        setRecyclerView();
        setDialog();
        setEvent();
    }

    private void setRecyclerView() {
        binding.recycler.setAdapter(adapter);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 8));
        adapter.setItems(DepotService.get().getAll());
    }

    private void setDialog() {
        dialog.getWindow().setDimAmount(0);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setEvent() {
        binding.add.setOnClickListener(view -> onAdd());
    }

    private void onAdd() {
        String url = binding.url.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.depot_url_empty);
            return;
        }
        if (DepotService.get().add(url, url)) {
            Notify.show(R.string.depot_added);
            binding.url.setText("");
            adapter.setItems(DepotService.get().getAll());
        } else {
            Notify.show(R.string.depot_url_empty);
        }
    }

    @Override
    public void onItemClick(Depot item) {
        if (listener != null) {
            DepotService.get().setDefault(item.getId());
            listener.onDepotSwitch(item);
        }
        dialog.dismiss();
    }

    @Override
    public void onItemDelete(Depot item) {
        DepotService.get().delete(item.getId());
        adapter.setItems(DepotService.get().getAll());
    }
}
