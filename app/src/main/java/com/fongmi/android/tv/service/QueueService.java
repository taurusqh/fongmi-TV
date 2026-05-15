package com.fongmi.android.tv.service;

import com.fongmi.android.tv.bean.Queue;

import java.util.List;

public class QueueService {

    private static QueueService instance;

    public static QueueService get() {
        if (instance == null) instance = new QueueService();
        return instance;
    }

    public void add(String siteKey, String vodId, String vodName, String vodPic, String episodeUrl, String episodeName) {
        Queue.create(siteKey, vodId, vodName, vodPic, episodeUrl, episodeName).save();
    }

    public void remove(long id) {
        Queue.delete(id);
    }

    public void clear() {
        Queue.clear();
    }

    public List<Queue> getAll() {
        return Queue.getAll();
    }

    public Queue getNext() {
        return Queue.getTop();
    }

    public Queue pollNext() {
        Queue item = Queue.getTop();
        if (item != null) item.delete();
        return item;
    }

    public boolean hasNext() {
        return Queue.getTop() != null;
    }
}