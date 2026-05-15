package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Depot;
import com.fongmi.android.tv.databinding.DialogDepotBinding;
import com.fongmi.android.tv.service.DepotService;
import com.fongmi.android.tv.ui.adapter.DepotAdapter;
import com.fongmi.android.tv.ui.custom.CustomRecyclerView;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.net.OkHttp;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;

// 多仓管理对话框，支持添加仓库地址、查看仓库列表、切换配置源
public class DepotDialog implements DepotAdapter.OnClickListener {

    private DialogDepotBinding binding;
    private DepotAdapter adapter;
    private AlertDialog dialog;
    private AlertDialog subDialog;
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
        fetchSubDepots(item);
    }

    private void fetchSubDepots(Depot depot) {
        Notify.progress(dialog.getContext());
        Task.execute(() -> {
            try {
                Response res = OkHttp.newCall(depot.getUrl()).execute();
                if (!res.isSuccessful()) {
                    App.post(() -> {
                        Notify.dismiss();
                        Notify.show("HTTP " + res.code());
                    });
                    return;
                }
                String body = res.body().string();
                List<Depot> items = Depot.arrayFrom(body);
                App.post(() -> {
                    Notify.dismiss();
                    if (items.isEmpty()) {
                        Notify.show(R.string.depot_empty);
                    } else {
                        showSubDialog(items);
                    }
                });
            } catch (IOException e) {
                App.post(() -> {
                    Notify.dismiss();
                    Notify.show(e.getMessage());
                });
            }
        });
    }

    private void showSubDialog(List<Depot> items) {
        CustomRecyclerView recycler = new CustomRecyclerView(dialog.getContext());
        recycler.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
        recycler.addItemDecoration(new SpaceItemDecoration(1, 8));
        recycler.setPadding(24, 24, 24, 24);

        DepotAdapter subAdapter = new DepotAdapter(new DepotAdapter.OnClickListener() {
            @Override
            public void onItemClick(Depot item) {
                subDialog.dismiss();
                if (listener != null) {
                    listener.onDepotSwitch(item);
                }
            }

            @Override
            public void onItemDelete(Depot item) {
            }
        });
        subAdapter.setItems(items);
        recycler.setAdapter(subAdapter);

        subDialog = new MaterialAlertDialogBuilder(dialog.getContext())
                .setView(recycler)
                .create();
        subDialog.getWindow().setDimAmount(0);
        subDialog.show();
        if (subDialog.getWindow() != null) {
            subDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onItemDelete(Depot item) {
        DepotService.get().delete(item.getId());
        adapter.setItems(DepotService.get().getAll());
    }
}
