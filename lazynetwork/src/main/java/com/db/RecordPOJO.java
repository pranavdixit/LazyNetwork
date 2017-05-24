package com.db;

import com.lazynetwork.RecordCallback;

/**
 * Created by pranav.dixit on 22/05/17.
 */

public class RecordPOJO {
    private String data;
    private String status;

    public RecordPOJO(String data, String status){
        this.data = data;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
