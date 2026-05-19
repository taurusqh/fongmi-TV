package com.fongmi.android.tv.bean;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.impl.Diffable;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

@Entity
public class Depot implements Diffable<Depot> {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String url;
    private String name;
    private boolean isDefault;
    private int sort;
    private long createTime;

    @SerializedName("api")
    private String api;
    @SerializedName("ext")
    private String ext;
    @SerializedName("jar")
    private String jar;
    @SerializedName("proxy")
    private String proxy;

    // fix: 持久化缓存子仓库列表和当前选中仓库
    private String warehouses;
    private String activeWarehouse;

    public Depot() {
        this.createTime = System.currentTimeMillis();
    }

    public static List<Depot> arrayFrom(String str) {
        List<Depot> items = App.gson().fromJson(str, new com.google.gson.reflect.TypeToken<List<Depot>>() {}.getType());
        return items == null ? Collections.emptyList() : items;
    }

    public static List<Depot> getAll() {
        return AppDatabase.get().getDepotDao().findAll();
    }

    public static Depot getDefault() {
        return AppDatabase.get().getDepotDao().getDefault();
    }

    public static void setDefault(long id) {
        AppDatabase.get().getDepotDao().clearDefault();
        AppDatabase.get().getDepotDao().setDefault(id);
    }

    public static void delete(long id) {
        AppDatabase.get().getDepotDao().delete(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? getUrl() : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(String warehouses) {
        this.warehouses = warehouses;
    }

    public String getActiveWarehouse() {
        return activeWarehouse;
    }

    public void setActiveWarehouse(String activeWarehouse) {
        this.activeWarehouse = activeWarehouse;
    }

    public void save() {
        AppDatabase.get().getDepotDao().insert(this);
    }

    public void delete() {
        AppDatabase.get().getDepotDao().delete(getId());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Depot it)) return false;
        return getId() == it.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    @NonNull
    @Override
    public String toString() {
        return App.gson().toJson(this);
    }

    @Override
    public boolean isSameItem(Depot other) {
        return getId() == other.getId();
    }

    @Override
    public boolean isSameContent(Depot other) {
        return getUrl().equals(other.getUrl());
    }
}