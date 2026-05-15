package com.fongmi.android.tv.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.fongmi.android.tv.bean.Depot;

import java.util.List;

@Dao
public abstract class DepotDao extends BaseDao<Depot> {

    @Query("SELECT * FROM Depot ORDER BY sort ASC, createTime DESC")
    public abstract List<Depot> findAll();

    @Query("SELECT * FROM Depot WHERE isDefault = 1 LIMIT 1")
    public abstract Depot getDefault();

    @Query("UPDATE Depot SET isDefault = 0")
    public abstract void clearDefault();

    @Query("UPDATE Depot SET isDefault = 1 WHERE id = :id")
    public abstract void setDefault(long id);

    @Query("DELETE FROM Depot WHERE id = :id")
    public abstract void delete(long id);
}