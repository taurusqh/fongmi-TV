package com.fongmi.android.tv.bean;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.impl.Diffable;

import java.util.Collections;
import java.util.List;

@Entity
public class Queue implements Diffable<Queue> {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String siteKey;
    private String vodId;
    private String vodName;
    private String vodPic;
    private String episodeUrl;
    private String episodeName;
    private long createTime;

    public Queue() {
        this.createTime = System.currentTimeMillis();
    }

    public static Queue create(String siteKey, String vodId, String vodName, String vodPic, String episodeUrl, String episodeName) {
        Queue queue = new Queue();
        queue.setSiteKey(siteKey);
        queue.setVodId(vodId);
        queue.setVodName(vodName);
        queue.setVodPic(vodPic);
        queue.setEpisodeUrl(episodeUrl);
        queue.setEpisodeName(episodeName);
        return queue;
    }

    public static List<Queue> getAll() {
        return AppDatabase.get().getQueueDao().findAll();
    }

    public static Queue getTop() {
        return AppDatabase.get().getQueueDao().getTop();
    }

    public static void delete(long id) {
        AppDatabase.get().getQueueDao().delete(id);
    }

    public static void clear() {
        AppDatabase.get().getQueueDao().clear();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getVodId() {
        return vodId;
    }

    public void setVodId(String vodId) {
        this.vodId = vodId;
    }

    public String getVodName() {
        return vodName;
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getVodPic() {
        return vodPic;
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public String getEpisodeUrl() {
        return episodeUrl;
    }

    public void setEpisodeUrl(String episodeUrl) {
        this.episodeUrl = episodeUrl;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getKey() {
        return siteKey + AppDatabase.SYMBOL + vodId;
    }

    public void save() {
        AppDatabase.get().getQueueDao().insert(this);
    }

    public void delete() {
        AppDatabase.get().getQueueDao().delete(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Queue it)) return false;
        return getId() == it.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    @Override
    public boolean isSameItem(Queue other) {
        return getId() == other.getId();
    }

    @Override
    public boolean isSameContent(Queue other) {
        return getSiteKey().equals(other.getSiteKey()) && getVodId().equals(other.getVodId()) && getEpisodeUrl().equals(other.getEpisodeUrl());
    }
}