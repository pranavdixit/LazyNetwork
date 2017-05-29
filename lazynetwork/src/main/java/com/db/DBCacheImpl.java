package com.db;

import android.database.Cursor;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.lazynetwork.RecordCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pranav.dixit on 22/05/17.
 */
public class DBCacheImpl implements DBCache, DbCallback {

    private Map<String, ArrayList<RecordPOJO>> cache = new ConcurrentHashMap<>();
    private Map<String, Class> typeMap = new HashMap<>();

    private static final String ALL_RECORDS = "allRecords";
    private static final String ADD_RECORD = "addRecord";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String UPDATE_RECORD_SENT = "update_record_sent";
    private static final String CLEAR_TYPE = "clear_type";
    private static final String TYPE_RECORDS = "type_records";

    private static DBCache instance = new DBCacheImpl();

    private DBCacheImpl() {

    }

    ;

    public static DBCache getInsDbCache() {
        return instance;
    }

    @Override
    public void initCache() throws Exception {
        // Cache is initialized for as per Record Type demand and not at once.
    }

    @Override
    public <E> void initType(String type, final Class<E> clazz) throws Exception {
        if (!typeMap.containsKey(type)) {
            typeMap.put(type, clazz);
            RecordTable.getAllRecordsData(type, new DbCallback() {
                @Override
                public void onQueryResult(Cursor cursor, String tag) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {

                        String type = cursor.getString(cursor.getColumnIndex(RecordTable.TYPE));
                        String data = cursor.getString(cursor.getColumnIndex(RecordTable.DATA));
                        String status = cursor.getString(cursor.getColumnIndex(RecordTable.STATUS));

                        ParseToObject<E> parseToObject = new ParseToObject(type, data, ParseToObject.ADD_TO_CACHE, clazz);
                        parseToObject.setStatus(status);
                        parseToObject.execute();

                        cursor.moveToNext();
                    }
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
        addToCache(type, new RecordPOJO(data, RecordTable.Status.PENDING));
        new ParseToString(type, data, ParseToString.ADD).execute();

    }

    @Override
    public void removeRecord(String type, RecordCallback data) throws Exception {
        removeFromCache(type, data);
        new ParseToString(type, data, ParseToString.REMOVE).execute();

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
                new ParseToString(type, data, ParseToString.UPDATE);
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

    private void addToCache(String type, RecordPOJO recordPOJO) {
        ArrayList<RecordPOJO> recordList = cache.get(type);
        if (recordList == null) {
            recordList = new ArrayList<>();
            cache.put(type, recordList);
        }
        if (!recordList.contains(recordPOJO))
            recordList.add(recordPOJO);
    }

    private <E> void removeFromCache(String type, E data) {
        ArrayList<RecordPOJO> recordList = cache.get(type);
        if (recordList != null) {
            Iterator<RecordPOJO> iterator = recordList.iterator();
            while (iterator.hasNext()) {
                RecordPOJO recordPOJO = iterator.next();
                if (recordPOJO.getData().recordEqual(data)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private class ParseToString extends AsyncTask<String, String, String> {
        public static final int ADD = 1;
        public static final int REMOVE = 2;
        public static final int UPDATE = 3;

        private final RecordCallback object;
        private int flag;
        private String type;

        public ParseToString(String type, RecordCallback object, int flag) {
            this.object = object;
            this.flag = flag;
            this.type = type;
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
            try {
                switch (flag) {
                    case ADD:
                        RecordTable.addRecord(type, s, DBCacheImpl.this, ADD_RECORD);
                        break;
                    case REMOVE:
                        RecordTable.removeRecord(s, DBCacheImpl.this, DELETE_RECORD);
                        break;

                    case UPDATE:
                        RecordTable.updateRecordStatus(RecordTable.Status.SENT, s, DBCacheImpl.this, UPDATE_RECORD_SENT);
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class ParseToObject<E> extends AsyncTask<String, String, E> {

        public static final int ADD_TO_CACHE = 1;

        private final String data;
        private int flag;
        private String type;
        private String recordStatus;
        private Class<E> clazz;

        public ParseToObject(String type, String data, int flag, Class<E> clazz) {
            this.data = data;
            this.flag = flag;
            this.type = type;
            this.clazz = clazz;
        }

        public String getRecordStatus() {
            return recordStatus;
        }

        public void setStatus(String status) {
            this.recordStatus = status;
        }

        @Override
        protected E doInBackground(String... params) {
            Gson gson = new Gson();
            E object = gson.fromJson(data, clazz);
            return object;
        }

        @Override
        protected void onPostExecute(E s) {
            super.onPostExecute(s);
            try {
                switch (flag) {
                    case ADD_TO_CACHE:
                        ArrayList<RecordPOJO> recordList = cache.get(type);
                        if (recordList == null) {
                            recordList = new ArrayList<>();
                            cache.put(type, recordList);
                        }
                        if (s instanceof RecordCallback) {
                            recordList.add(new RecordPOJO((RecordCallback) s, recordStatus));
                        }
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
