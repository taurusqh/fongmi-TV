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
import com.fongmi.android.tv.utils.Util;
import com.github.catvod.net.OkHttp;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;

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
        // fix: 仓库列表缓存更新后通知 UI 刷新
        void onWarehouseChanged();
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
        // 读取自定义名称，没有指定则用 URL 作为默认名称
        String name = binding.name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) name = url;
        long depotId = DepotService.get().add(url, name);
        if (depotId != -1) {
            Notify.show(R.string.depot_added);
            binding.url.setText("");
            binding.name.setText("");
            refreshList();
            // fix: 添加后自动解析仓库列表并缓存
            autoParseDepot(depotId, url);
        } else {
            Notify.show(R.string.depot_url_empty);
        }
    }

    // fix: 自动解析多仓 JSON，缓存子仓库列表 + 自动切换到第一个仓库
    private void autoParseDepot(long depotId, String url) {
        Notify.progress(dialog.getContext());
        Task.execute(() -> {
            try {
                Response res = OkHttp.newCall(url).execute();
                if (!res.isSuccessful()) {
                    App.post(() -> { Notify.dismiss(); Notify.show("HTTP " + res.code()); });
                    return;
                }
                String body = res.body().string();
                if (body != null && body.startsWith("{")) {
                    try {
                        JsonObject obj = App.gson().fromJson(body, JsonObject.class);
                        if (obj.has("urls")) body = obj.getAsJsonArray("urls").toString();
                    } catch (Exception ignored) { }
                }
                List<Depot> items = Depot.arrayFrom(body);
                for (Depot item : items) {
                    if (TextUtils.isEmpty(item.getUrl()) && !TextUtils.isEmpty(item.getApi())) {
                        item.setUrl(item.getApi());
                    }
                }
                if (!items.isEmpty()) {
                    // 缓存子仓库列表到 depot
                    DepotService.get().saveWarehouseList(depotId, body);
                    // 自动选取第一个仓库
                    Depot first = items.get(0);
                    DepotService.get().setActiveWarehouse(depotId, first.getName());
                    App.post(() -> {
                        Notify.dismiss();
                        refreshList();
                        if (listener != null) {
                            listener.onDepotSwitch(first);
                            listener.onWarehouseChanged();
                        }
                    });
                } else {
                    App.post(() -> { Notify.dismiss(); Notify.show(R.string.depot_empty); });
                }
            } catch (Exception e) {
                App.post(() -> { Notify.dismiss(); Notify.show(e.getMessage()); });
            }
        });
    }

    // fix: 确保添加/删除后列表刷新
    private void refreshList() {
        adapter.setItems(DepotService.get().getAll());
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
                // fix: 兼容 JSON 对象包 urls 数组的格式
                if (body != null && body.startsWith("{")) {
                    try {
                        JsonObject obj = App.gson().fromJson(body, JsonObject.class);
                        if (obj.has("urls")) body = obj.getAsJsonArray("urls").toString();
                    } catch (Exception ignored) {
                    }
                }
                final String warehouseJson = body;
                List<Depot> items = Depot.arrayFrom(body);
                // 子仓库的 URL 可能位于 api 字段而非 url 字段，做兼容映射
                for (Depot item : items) {
                    if (TextUtils.isEmpty(item.getUrl()) && !TextUtils.isEmpty(item.getApi())) {
                        item.setUrl(item.getApi());
                    }
                }
                App.post(() -> {
                    Notify.dismiss();
                    if (items.isEmpty()) {
                        Notify.show(R.string.depot_empty);
                    } else {
                        // fix: 手动解析后缓存子仓库列表，让仓库切换按钮能读取
                        DepotService.get().saveWarehouseList(depot.getId(), warehouseJson);
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

            @Override
            public void onItemLongClick(Depot item) {
                // 子仓库列表长按复制地址
                Util.copy(item.getUrl());
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
    public void onItemLongClick(Depot item) {
        // 长按复制仓库地址到剪贴板
        Util.copy(item.getUrl());
    }

    @Override
    public void onItemDelete(Depot item) {
        boolean isDefault = item.isDefault();
        DepotService.get().delete(item.getId());
        // fix: 删除默认仓库后，自动将第一个剩余仓库设为默认
        if (isDefault) DepotService.get().initDefault();
        refreshList();
    }
}
