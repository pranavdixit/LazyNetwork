package com.db;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.lazynetwork.ExecutorCallback;
import com.lazynetwork.NetworkRecord;
import com.lazynetwork.RecordCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pranav.dixit on 22/05/17.
 */
public class DBCacheImpl implements DBCache, DbCallback {

    private Map<String, ArrayList<RecordPOJO>> cache = new ConcurrentHashMap<>();
    private Map<String, Class> typeMap = new HashMap<>();

    private static final String ADD_RECORD = "addRecord";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String UPDATE_RECORD_SENT = "update_record_sent";
    private static final String CLEAR_TYPE = "clear_type";
    private static final String TYPE_RECORDS = "type_records";

    private static DBCache instance = new DBCacheImpl();

    private DBCacheImpl() {

    }

    public static DBCache getInsDbCache() {
        return instance;
    }

    @Override
    public <E extends RecordCallback> void initType(String type, final Class<E> clazz, final ExecutorCallback<E> executorCallback, final NetworkRecord networkRecord) throws Exception {
        if (!typeMap.containsKey(type)) {
            typeMap.put(type, clazz);
            RecordTable.getAllRecordsData(type, new DbCallback() {
                @Override
                public void onQueryResult(Cursor cursor, String tag) {
                    new UpdateCacheTaskt<E>(cursor, clazz, executorCallback, networkRecord).execute();
                }

                @Override
                public void onResultInserted(long id, String tag) {

                }

                @Override
                public void onResultDeleted(long id, String tag) {

                }

                @Override
                public void onResultUpdated(int rows, String tag) {

                }
            }, TYPE_RECORDS);
        }else if (networkRecord.isAutoRetry()) {
            networkRecord.executeAllPendingRecords(type);
        }
    }

    public <E extends RecordCallback> void initType(String type, final Class<E> clazz) throws Exception {
        if (!typeMap.containsKey(type)) {
            typeMap.put(type, clazz);
            RecordTable.getAllRecordsData(type, new DbCallback() {
                @Override
                public void onQueryResult(Cursor cursor, String tag) {
                    new UpdateCacheTask<E>(cursor, clazz).execute();
                }

                @Override
                public void onResultInserted(long id, String tag) {

                }

                @Override
                public void onResultDeleted(long id, String tag) {

                }

                @Override
                public void onResultUpdated(int rows, String tag) {

                }
            }, TYPE_RECORDS);
        }
    }




    @Override
    public void addRecord(String type, RecordCallback data) throws Exception {
        RecordPOJO recordPOJO = new RecordPOJO(data, RecordTable.Status.PENDING, UUID.randomUUID().toString());
        if (addToCache(type, recordPOJO))
            new ParseToString(type, recordPOJO, ParseToString.ADD).execute();
    }

    @Override
    public void removeRecord(String type, RecordCallback data) throws Exception {
        RecordPOJO recordPOJO = removeFromCache(type, data);
        if (recordPOJO != null)
            new ParseToString(type, recordPOJO, ParseToString.REMOVE).execute();

    }

    @Override
    public ArrayList<RecordPOJO> getRecords(String type) {
        return cache.get(type);
    }

    @Override
    public void updateRecordStatus(String type, String status, RecordCallback data) throws Exception {
        ArrayList<RecordPOJO> records = cache.get(type);
        for (RecordPOJO record : records
                ) {
            if (record.getData().recordEqual(data)) {
                record.setStatus(status);
                new ParseToString(type, record, ParseToString.UPDATE);
                break;
            }
        }
    }

    @Override
    public void clearAllData() throws Exception {
        cache.clear();
        RecordTable.truncateTable();
    }

    @Override
    public void clearAllTypeRecords(String type) throws Exception {
        cache.clear();
        RecordTable.clearType(type, this, CLEAR_TYPE);
    }

    @Override
    public void onQueryResult(Cursor cursor, String tag) {

    }

    @Override
    public void onResultInserted(long id, String tag) {

    }

    @Override
    public void onResultDeleted(long id, String tag) {

    }

    @Override
    public void onResultUpdated(int rows, String tag) {

    }

    private boolean addToCache(String type, RecordPOJO recordPOJO) {
        ArrayList<RecordPOJO> recordList = cache.get(type);
        if (recordList == null) {
            recordList = new ArrayList<>();
            cache.put(type, recordList);
        }
        if (!recordList.contains(recordPOJO)) {
            recordList.add(recordPOJO);
            return true;
        }
        return false;
    }

    private <E> RecordPOJO removeFromCache(String type, E data) {
        ArrayList<RecordPOJO> recordList = cache.get(type);
        if (recordList != null) {
            Iterator<RecordPOJO> iterator = recordList.iterator();
            while (iterator.hasNext()) {
                RecordPOJO recordPOJO = iterator.next();
                if (recordPOJO.getData().recordEqual(data)) {
                    iterator.remove();
                    return recordPOJO;
                }
            }
        }
        return null;
    }

    private class ParseToString extends AsyncTask<String, String, String> {
        public static final int ADD = 1;
        public static final int REMOVE = 2;
        public static final int UPDATE = 3;

        private final RecordPOJO object;
        private int flag;
        private String type;

        public ParseToString(String type, RecordPOJO object, int flag) {
            this.object = object;
            this.flag = flag;
            this.type = type;
        }

        @Override
        protected String doInBackground(String... params) {
            String data;
            Gson gson = new Gson();
            data = gson.toJson(object.getData());
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                switch (flag) {
                    case ADD:
                        RecordTable.addRecord(type, s, object.getUid(), DBCacheImpl.this, ADD_RECORD);
                        break;
                    case REMOVE:
                        RecordTable.removeRecord(object.getUid(), DBCacheImpl.this, DELETE_RECORD);
                        break;

                    case UPDATE:
                        RecordTable.updateRecordStatus(RecordTable.Status.SENT, object.getUid(), DBCacheImpl.this, UPDATE_RECORD_SENT);
                        break;

                }
            } catch (Exception e) {
                Log.i(LazyNetwork.TAG, e.getMessage());
            }

        }
    }

    private class UpdateCacheTaskt<E extends RecordCallback> extends AsyncTask<String, String, String> {

        public static final int ADD_TO_CACHE = 1;

        private Cursor cursor;
        private Class<E> clazz;
        private ExecutorCallback<E> executorCallback;
        NetworkRecord networkRecord;

        public UpdateCacheTaskt(Cursor cursor, Class<E> clazz, ExecutorCallback<E> executorCallback, NetworkRecord networkRecord) {
            this.cursor = cursor;
            this.clazz = clazz;
            this.executorCallback = executorCallback;
            this.networkRecord = networkRecord;
        }


        @Override
        protected String doInBackground(String... params) {
            String type = "";
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String uid = cursor.getString(cursor.getColumnIndex(RecordTable.UID));
                type = cursor.getString(cursor.getColumnIndex(RecordTable.TYPE));
                String data = cursor.getString(cursor.getColumnIndex(RecordTable.DATA));
                String status = cursor.getString(cursor.getColumnIndex(RecordTable.STATUS));

                Gson gson = new Gson();
                E object = gson.fromJson(data, clazz);

                ArrayList<RecordPOJO> recordList = cache.get(type);
                if (recordList == null) {
                    recordList = new ArrayList<>();
                    cache.put(type, recordList);
                }
                if (object instanceof RecordCallback) {
                    recordList.add(new RecordPOJO((RecordCallback) object, status, uid));
                }

                cursor.moveToNext();
            }
            return type;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            executorCallback.onCacheUpdated();
            if (networkRecord.isAutoRetry())
                networkRecord.executeAllPendingRecords(s);
        }

    }

    private class UpdateCacheTask<E extends RecordCallback> extends AsyncTask<String, String, String> {

        public static final int ADD_TO_CACHE = 1;

        private Cursor cursor;
        private Class<E> clazz;

        public UpdateCacheTask(Cursor cursor, Class<E> clazz) {
            this.cursor = cursor;
            this.clazz = clazz;
        }


        @Override
        protected String doInBackground(String... params) {
            String type = "";
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String uid = cursor.getString(cursor.getColumnIndex(RecordTable.UID));
                type = cursor.getString(cursor.getColumnIndex(RecordTable.TYPE));
                String data = cursor.getString(cursor.getColumnIndex(RecordTable.DATA));
                String status = cursor.getString(cursor.getColumnIndex(RecordTable.STATUS));

                Gson gson = new Gson();
                E object = gson.fromJson(data, clazz);

                ArrayList<RecordPOJO> recordList = cache.get(type);
                if (recordList == null) {
                    recordList = new ArrayList<>();
                    cache.put(type, recordList);
                }
                if (object instanceof RecordCallback) {
                    recordList.add(new RecordPOJO(object, status, uid));
                }

                cursor.moveToNext();
            }
            return type;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

    }

}
