package com.lazynetwork;

import android.content.Context;
import android.os.AsyncTask;
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
    private boolean autoRetry = true;

    public NetworkRecord(ExecutorCallback executor, String uniqueType) throws Exception {
        this.type = uniqueType;
        this.executor = executor;
        cache = DBCacheImpl.getInsDbCache();
        if (autoRetry)
            executeAllPendingRecords(type);
    }

    public void deregister() {
        executor = null;
    }

    public void register(ExecutorCallback executor) {
        this.executor = executor;
    }

//    public void createRecord(E object) {
//        Gson gson = new Gson();
//        String data = gson.toJson(object);
//        try {
//            cache.addRecord(type, data);
//        } catch (Exception e) {
//            Log.i("lazyNetwork", e.getMessage());
//        }
//        executor.execute(data);
//        try {
//            cache.updateRecordStatus(type, RecordTable.Status.SENT, data);
//        } catch (Exception e) {
//            Log.i("lazyNetwork", e.getMessage());
//        }
//    }
//
//    public void removeRecord(E object) {
//        Gson gson = new Gson();
//        String data = gson.toJson(object);
//        try {
//            cache.removeRecord(type, data);
//        } catch (Exception e) {
//            Log.i("lazyNetwork", e.getMessage());
//        }
//    }

    public void createRecord(E object) {
        new ParseObject(object, ParseObject.ADD).execute();
    }

    public void removeRecord(E object) {
        new ParseObject(object, ParseObject.REMOVE).execute();

    }

    public <T extends RecordCallback> boolean isRecorded(T object, Class<T> clazz) {
        ArrayList<RecordPOJO> records = cache.getRecords(type);
        if (records == null)
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

    public void clear() {
        try {
            cache.clearAllTypeRecords(type);
        } catch (Exception e) {
            Log.i("lazyNetwork", e.getMessage());
        }
    }

    private void executeAllPendingRecords(String type) {
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

    private class ParseObject extends AsyncTask<String, String, String> {
        public static final int ADD = 1;
        public static final int REMOVE = 2;

        private final E object;
        private int flag;

        public ParseObject(E object, int flag) {
            this.object = object;
            this.flag = flag;
        }

        @Override
        protected String doInBackground(String... params) {
            String data;
            Gson gson = new Gson();
            data = gson.toJson(object);
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            switch (flag) {
                case ADD:
                    try {
                        cache.addRecord(type, s);
                    } catch (Exception e) {
                        Log.i("lazyNetwork", e.getMessage());
                    }
                    executor.execute(s);
                    executor.recordAdded(object);
                    try {
                        cache.updateRecordStatus(type, RecordTable.Status.SENT, s);
                    } catch (Exception e) {
                        Log.i("lazyNetwork", e.getMessage());
                    }
                    break;
                case REMOVE:
                    try {
                        cache.removeRecord(type, s);
                    } catch (Exception e) {
                        Log.i("lazyNetwork", e.getMessage());
                    }
                    executor.recordRemoved(object);
                    break;

            }

        }
    }
}
