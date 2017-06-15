package com.db;

import android.content.Context;
import android.util.Log;

import com.db.DBCache;
import com.db.DBCacheImpl;
import com.db.RecordDB;
import com.db.RecordDbImpl;
import com.db.RecordTable;
import com.lazynetwork.RecordCallback;

/**
 * Created by pranav.dixit on 22/05/17.
 */

public class LazyNetwork {

    public static final String TAG = "LazyNetwrok";

    private LazyNetwork(){

    }

    public synchronized static void init(Context context){
        RecordDbImpl.init(context);
        try {
            DBCacheImpl.getInsDbCache();
        } catch (Exception e) {
            Log.i("lazyNetwork: ","this cannot come "+e.getMessage());
        }
    }

    public static void clearAllData() throws Exception {

            DBCacheImpl.getInsDbCache().clearAllData();

    }

    public static void closeDb() throws Exception {

        RecordDbImpl.getInstance().closeDb();

    }
    public static <E extends RecordCallback> void registerRecordTypes(String uniqueKey,Class<E> clazz){
        try {
            DBCacheImpl.getInsDbCache().initType(uniqueKey,clazz);
        } catch (Exception e) {
            Log.i("lazyNetwork: ","this cannot come "+e.getMessage());
        }
    }
}
