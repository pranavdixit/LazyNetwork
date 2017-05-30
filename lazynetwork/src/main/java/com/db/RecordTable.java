package com.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

import static android.R.attr.tag;

/**
 * Created by pranav.dixit on 19/05/17.
 */

public class RecordTable {

    private RecordTable() {

    }

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + RecordTable.TABLE_NAME + "(" +
            RecordTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RecordTable.TYPE + " TEXT, " +
            RecordTable.DATA + " TEXT, " +
            RecordTable.STATUS + " INTEGER, " +
            RecordTable.UID + " TEXT)";


    public static final String TABLE_NAME = "records";
    public static final String ID = "_id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String STATUS = "status";
    public static final String UID = "uid";

    public interface Status {
        String PENDING = "pending";
        String SENT = "sent";
        String DONE = "done";
    }

    private static RecordDB db;

    public static void getAllRecordsData(String type, DbCallback dbCallback, String tag) throws Exception {
        db = RecordDbImpl.getInstance();
        db.asyncQuery(TABLE_NAME, null, TYPE + " = ?", new String[]{type}, null, dbCallback, tag);
    }

    public static void getAllRecords(DbCallback dbCallback, String tag) throws Exception {
        db = RecordDbImpl.getInstance();
        db.asyncQuery(TABLE_NAME, null, null, null, null, dbCallback, tag);
    }

    public static void removeRecord(String uid, DbCallback dbCallback, String tag) throws Exception {
        db = RecordDbImpl.getInstance();
        db.deleteRows(TABLE_NAME, RecordTable.UID + "= ?", new String[]{uid}, dbCallback, tag);
    }

    public static void addRecord(String type, String data,String uid, DbCallback dbCallback, String tag) throws Exception {

        db = RecordDbImpl.getInstance();

        ContentValues cv = new ContentValues();
        cv.put(DATA, data);
        cv.put(STATUS, Status.PENDING);
        cv.put(TYPE, type);
        cv.put(UID, uid);

        db.insert(TABLE_NAME, cv, dbCallback, tag);
    }

    public static void updateRecordStatus(String status, String data, DbCallback dbCallback, String tag) throws Exception {
        db = RecordDbImpl.getInstance();
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TABLE_NAME, cv, UID + " = ?", new String[]{data}, dbCallback, tag);
    }

    public static void truncateTable() {
        db.clear(TABLE_NAME);
    }

    public static void clearType(String type, DbCallback callback, String tag) {
        db.deleteRows(TABLE_NAME, TYPE + " = ?", new String[]{type}, callback, tag);
    }
}
