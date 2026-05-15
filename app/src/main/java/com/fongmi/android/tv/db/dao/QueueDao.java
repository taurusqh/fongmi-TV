package com.fongmi.android.tv.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.fongmi.android.tv.bean.Queue;

import java.util.List;

@Dao
public abstract class QueueDao extends BaseDao<Queue> {

    @Query("SELECT * FROM Queue ORDER BY createTime ASC")
    public abstract List<Queue> findAll();

    @Query("SELECT * FROM Queue ORDER BY createTime ASC LIMIT 1")
    public abstract Queue getTop();

    @Query("DELETE FROM Queue WHERE id = :id")
    public abstract void delete(long id);

    @Query("DELETE FROM Queue")
    public abstract void clear();
}