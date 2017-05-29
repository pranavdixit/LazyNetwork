package com.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pranav.dixit on 19/05/17.
 */

class RecordDbImpl implements RecordDB {
    private static volatile RecordDbImpl recordDBImpl;
    private static final int coreSize = 10;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(coreSize);
    private static Database database;

    private final static String name = "LazyNetwork";
    private final static int version = 1;



    private RecordDbImpl(Context context) {
        database = new Database(context);
    }

    public static RecordDbImpl getInstance() throws Exception{
        if (recordDBImpl == null) {
            synchronized (recordDBImpl) {
                if (recordDBImpl == null) {
                    throw new Exception("Please init LazyNetwork in application class");
                }
            }
        }
        return recordDBImpl;
    }

    public static void init(Context context) {
        if (recordDBImpl == null) {
            synchronized (RecordDbImpl.class){
                if (recordDBImpl == null) {
                    recordDBImpl = new RecordDbImpl(context);
                }
            }
        }
    }

    public void insert(final String table, final ContentValues cv, final DbCallback dbCallback, final String tag) {
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                final long id = database.getWritableDatabase().insert(table, null, cv);
                if(dbCallback != null){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            dbCallback.onResultInserted(id,tag);
                        }
                    });
                }
            }
        });
    }

    /**
     * selection = "read = ?,price < ?"
     * args = "?" in selection will be replaced by args respectively
     * returns data with default sort, no grouping and no having clause
     */
    public Cursor query(final String table, final String[] columns, final String selection, final String[] args,final String orderBy, final DbCallback dbCallback) {
        return database.getReadableDatabase().query(table, columns, selection, args, null, null, orderBy);
    }

    public void asyncQuery(final String table, final String[] columns, final String selection, final String[] args,final String orderBy, final DbCallback dbCallback,final String tag){
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                final Cursor cursor = database.getReadableDatabase().query(table, columns, selection, args, null, null, orderBy);
                if(dbCallback != null){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            dbCallback.onQueryResult(cursor,tag);
                        }
                    });
                }
            }
        });
    }

    public void update(final String table, final ContentValues cv, final String where, final String[] args, final DbCallback dbCallback, final String tag) {
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                final int rows = database.getReadableDatabase().update(table,cv,where,args);
                if(dbCallback != null){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            dbCallback.onResultUpdated(rows,tag);
                        }
                    });
                }
            }
        });

    }

    //    delete and return number of rows deleted, to delete all rows pass selection as null
    public void deleteRows(final String table, final String selection, final String[] args,final DbCallback dbCallback,final String tag) {
        if (selection == null) {
            Log.i("LazyDatabase", "selection cannot be null in delete row");
            return;
        }
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                final int rows = database.getWritableDatabase().delete(table, selection, args);
                Log.i("LazyDatabase", "Total number of rows deleted "+rows);
                if(dbCallback != null){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            dbCallback.onResultDeleted(rows,tag);
                        }
                    });
                }

            }
        });
    }

    //    truncate table
    public void clear(final String table) {
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                database.getWritableDatabase().delete(table, null, null);
            }
        });
    }

    public void closeDb() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                database.close();
            }
        });
    }


        private static final class Database extends SQLiteOpenHelper{

        public Database(Context context) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(RecordTable.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
