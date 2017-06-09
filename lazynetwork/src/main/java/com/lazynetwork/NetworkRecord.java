package com.lazynetwork;

import android.util.Log;

import com.db.DBCache;
import com.db.DBCacheImpl;
import com.db.LazyNetwork;
import com.db.RecordPOJO;
import com.db.RecordTable;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by pranav.dixit on 19/05/17.
 */

public class NetworkRecord<E extends RecordCallback> {

    private String type;
    private ExecutorCallback executor;
    private DBCache cache;
    private boolean autoRetry = true;

    public NetworkRecord(ExecutorCallback executor, String uniqueType, Class<E> clazz) throws Exception {
        this.type = uniqueType;
        this.executor = executor;
        cache = DBCacheImpl.getInsDbCache();
        cache.initType(type, clazz, executor, this);
    }

    public void deregister() {
        executor = null;
    }

    public void register(ExecutorCallback executor) {
        this.executor = executor;
    }

    public void createRecord(E object) {
        try {
            cache.addRecord(type, object);
        } catch (Exception e) {
            Log.i(LazyNetwork.TAG, e.getMessage());
        }
        executor.execute(object);
        try {
            cache.updateRecordStatus(type, RecordTable.Status.SENT, object);
        } catch (Exception e) {
            Log.i(LazyNetwork.TAG, e.getMessage());
        }
    }

    public void removeRecord(E object) {
        try {
            cache.removeRecord(type, object);
        } catch (Exception e) {
            Log.i(LazyNetwork.TAG, e.getMessage());
        }
    }


    public boolean isRecorded(E object) {
        ArrayList<RecordPOJO> records = cache.getRecords(type);
        if (records == null)
            return false;
        for (RecordPOJO recordPOJO : records
                ) {
            E object2 = (E) recordPOJO.getData();
            if (object.recordEqual(object2)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<E> getRecords() {
        ArrayList<RecordPOJO> list = cache.getRecords(type);
        ArrayList<E> result = new ArrayList<>();
        if (list != null) {
            for (RecordPOJO pojo : list
                    ) {
                try {
                    result.add((E) pojo.getData());
                }catch (ClassCastException e){
                    Log.i(LazyNetwork.TAG, ""+e.getMessage());
                }
            }
        }
        return result;
    }

    public void clear() {
        try {
            cache.clearAllTypeRecords(type);
        } catch (Exception e) {
            Log.i(LazyNetwork.TAG, e.getMessage());
        }
    }

    public void executeAllPendingRecords(String type) {
        ArrayList<RecordPOJO> records = cache.getRecords(type);
        if (records == null)
            return;
        for (RecordPOJO recordPOJO : records
                ) {
            if (!recordPOJO.getStatus().equals(RecordTable.Status.DONE)) {
                executor.execute(recordPOJO.getData());
            }
        }

    }

    /**
     * if true all pending requests will be retried on object creation
     *
     * @param autoRetry
     */
    public void setAutoRetry(boolean autoRetry) {
        this.autoRetry = autoRetry;
    }

    public boolean isAutoRetry() {
        return autoRetry;
    }
}
