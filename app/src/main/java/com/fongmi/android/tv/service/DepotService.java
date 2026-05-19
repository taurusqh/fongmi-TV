package com.fongmi.android.tv.service;

import com.fongmi.android.tv.bean.Depot;
import com.fongmi.android.tv.db.AppDatabase;

import java.util.ArrayList;
import java.util.List;

// 多仓管理服务，支持配置仓库地址的增删和默认仓切换
public class DepotService {

    private static DepotService instance;

    public static DepotService get() {
        if (instance == null) instance = new DepotService();
        return instance;
    }

    public List<Depot> getAll() {
        return Depot.getAll();
    }

    public Depot getDefault() {
        return Depot.getDefault();
    }

    // fix: 改成返回 depot id，便于后续操作
    public long add(String url, String name) {
        if (url == null || url.isEmpty()) return -1;
        Depot depot = new Depot();
        depot.setUrl(url);
        depot.setName(name);
        if (Depot.getAll().isEmpty()) depot.setDefault(true);
        depot.save();
        return depot.getId();
    }

    public void setDefault(long id) {
        Depot.setDefault(id);
    }

    public void delete(long id) {
        Depot.delete(id);
    }

    // fix: 删除默认仓库后，自动将第一个剩余仓库设为默认
    public void initDefault() {
        if (getDefault() == null) {
            List<Depot> all = getAll();
            if (!all.isEmpty()) setDefault(all.get(0).getId());
        }
    }

    // fix: 缓存子仓库列表到 DB
    public void saveWarehouseList(long depotId, String warehouses) {
        AppDatabase.get().getDepotDao().setWarehouses(depotId, warehouses);
    }

    // fix: 设置当前激活的子仓库
    public void setActiveWarehouse(long depotId, String name) {
        AppDatabase.get().getDepotDao().setActiveWarehouse(depotId, name);
    }

    // fix: 获取当前默认仓库的激活子仓库名
    public String getActiveWarehouseName() {
        Depot d = getDefault();
        if (d == null) return "";
        if (d.getActiveWarehouse() != null) return d.getActiveWarehouse();
        return "";
    }

    // fix: 获取当前默认仓库缓存的所有子仓库（反序列化）
    public List<Depot> getCachedWarehouses(long depotId) {
        Depot depot = AppDatabase.get().getDepotDao().findById(depotId);
        if (depot == null || depot.getWarehouses() == null) return new ArrayList<>();
        return Depot.arrayFrom(depot.getWarehouses());
    }

    // fix: 获取默认 depot 的 id
    public long getDefaultId() {
        Depot d = getDefault();
        return d == null ? -1 : d.getId();
    }
}