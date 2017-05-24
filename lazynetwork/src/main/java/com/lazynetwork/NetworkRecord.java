package com.lazynetwork;

import android.content.Context;
import android.util.Log;

import com.db.DBCache;
import com.db.DBCacheImpl;
import com.db.RecordDB;
import com.db.RecordDbImpl;
import com.db.RecordPOJO;
import com.db.RecordTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by pranav.dixit on 19/05/17.
 */

public class NetworkRecord<E extends RecordCallback> {

    private String type;
    private ExecutorCallback executor;
    private DBCache cache;

    public NetworkRecord(ExecutorCallback executor, String uniqueType) throws Exception {
        this.type = uniqueType;
        this.executor = executor;
        cache = DBCacheImpl.getInsDbCache();
        executeAllPendingRecords(type);
    }

    public void deregister() {
        executor = null;
    }

    public void register(ExecutorCallback executor) {
        this.executor = executor;
    }

    public void createRecord(E object){
        try {
            cache.addRecord(type, object);
        } catch (Exception e) {
            Log.i("lazyNetwork", e.getMessage());
        }
        Gson gson = new Gson();
        String  data = gson.toJson(object);
        executor.execute(data);
        try {
            cache.updateRecordStatus(type,RecordTable.Status.SENT,data);
        } catch (Exception e) {
            Log.i("lazyNetwork", e.getMessage());
        }
    }

    public void removeRecord(E object) {
        try {
            cache.removeRecord(type, object);
        } catch (Exception e) {
            Log.i("lazyNetwork", e.getMessage());
        }
    }

    public <T extends RecordCallback>boolean isRecorded(T object, Class<T> clazz) {
        ArrayList<RecordPOJO> records = cache.getRecords(type);
        if(records == null)
            return false;
        for (RecordPOJO recordPOJO : records
                ) {
            String json = recordPOJO.getData();
            T object2 = new Gson().fromJson(json, clazz);
            if (object.recordEqual(object, object2)) {
                return true;
            }
        }
        return false;
    }

    public void executeAllPendingRecords(String type) {
        ArrayList<RecordPOJO> records = cache.getRecords(type);
        if(records == null)
            return;
        for (RecordPOJO recordPOJO : records
                ) {
            if (recordPOJO.getStatus().equals(RecordTable.Status.PENDING)) {
                executor.execute(recordPOJO.getData());
            }
        }

    }

}
