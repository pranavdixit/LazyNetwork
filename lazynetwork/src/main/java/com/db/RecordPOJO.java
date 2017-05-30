package com.db;

import com.lazynetwork.RecordCallback;

/**
 * Created by pranav.dixit on 22/05/17.
 */

public class RecordPOJO {
    private final String uid;
    private RecordCallback data;
    private String status;
    private int id;

    public RecordPOJO(RecordCallback data, String status, String uid) {
        this.data = data;
        this.status = status;
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RecordCallback getData() {
        return data;
    }

    public void setData(RecordCallback data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecordPOJO) {
            RecordPOJO obj2 = (RecordPOJO) obj;
            return data.recordEqual(obj2.getData());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
