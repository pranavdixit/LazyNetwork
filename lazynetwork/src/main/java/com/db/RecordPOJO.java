package com.db;

import com.lazynetwork.RecordCallback;

/**
 * Created by pranav.dixit on 22/05/17.
 */

public class RecordPOJO {
    private RecordCallback data;
    private String status;

    public RecordPOJO(RecordCallback data, String status) {
        this.data = data;
        this.status = status;
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
