package com.db;

import android.database.Cursor;

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
 public class DBCacheImpl implements DBCache,DbCallback{

    private Map<String,ArrayList<RecordPOJO>> cache = new ConcurrentHashMap<>();
    private Gson gson = new Gson();

    private static final String ALL_RECORDS = "allRecords";
    private static final String ADD_RECORD = "addRecord";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String UPDATE_RECORD_SENT = "update_record_sent";
    private static final String CLEAR_TYPE = "clear_type";

    private static DBCache instance = new DBCacheImpl();

    private DBCacheImpl(){

    };

    public static DBCache getInsDbCache(){
        return instance;
    }

    @Override
    public void initCache() throws Exception {
        RecordTable.getAllRecords(this,ALL_RECORDS);
    }

    @Override
    public <E>void addRecord(String type, E object) throws Exception {
        String data = gson.toJson(object);
        addToCache(type,new RecordPOJO(data,RecordTable.Status.PENDING));
        RecordTable.addRecord(type,data,this,ADD_RECORD);
    }

    @Override
    public <E>void removeRecord(String type, E object) throws Exception {
        String data = gson.toJson(object);
        removeFromCache(type,data);
        RecordTable.removeRecord(data,this,DELETE_RECORD);
    }

    @Override
    public  ArrayList<RecordPOJO> getRecords(String type) {
        return cache.get(type);
    }

    @Override
    public void updateRecordStatus(String type, String status, String data) throws Exception {
        ArrayList<RecordPOJO> records = cache.get(type);
        for (RecordPOJO record:records
             ) {
            if(record.getData().equals(data)){
                record.setStatus(status);
                RecordTable.updateRecordStatus(RecordTable.Status.SENT,data,this,UPDATE_RECORD_SENT);
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
        RecordTable.clearType(type,this,CLEAR_TYPE);
    }

    @Override
    public void onQueryResult(Cursor cursor, String tag) {
        if(cursor == null)
            return;
        switch (tag){
            case ALL_RECORDS:
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){

                    String type = cursor.getString(cursor.getColumnIndex(RecordTable.TYPE));
                    String data = cursor.getString(cursor.getColumnIndex(RecordTable.DATA));
                    String status = cursor.getString(cursor.getColumnIndex(RecordTable.STATUS));

                    addToCache(type,new RecordPOJO(data,status));

                    cursor.moveToNext();
                }
                break;
            default:
//                TODO: do nothing
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

    private void addToCache(String type, RecordPOJO recordPOJO){
        ArrayList<RecordPOJO> recordList = cache.get(type);
        if(recordList == null){
            recordList = new ArrayList<>();
            cache.put(type,recordList);
        }

        recordList.add(recordPOJO);
    }

    private void removeFromCache(String type, String data){
        ArrayList<RecordPOJO> recordList = cache.get(type);
        if(recordList != null){
            Iterator<RecordPOJO> iterator = recordList.iterator();
            while (iterator.hasNext()){
                RecordPOJO recordPOJO = iterator.next();
                if(recordPOJO.getData().equals(data)){
                    iterator.remove();
                    break;
                }
            }
        }
    }
}
