package com.fongmi.android.tv.service;

import com.fongmi.android.tv.bean.Depot;

import java.util.List;

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

    public void add(String url, String name) {
        Depot depot = new Depot();
        depot.setUrl(url);
        depot.setName(name);
        if (Depot.getAll().isEmpty()) depot.setDefault(true);
        depot.save();
    }

    public void setDefault(long id) {
        Depot.setDefault(id);
    }

    public void delete(long id) {
        Depot.delete(id);
    }
}