package com.lazynetwork;

import android.content.Context;
import android.util.Log;

import com.db.DBCache;
import com.db.DBCacheImpl;
import com.db.RecordDB;
import com.db.RecordDbImpl;

/**
 * Created by pranav.dixit on 22/05/17.
 */

public class LazyNetwork {

    public synchronized static void init(Context context){
        RecordDbImpl.init(context);
        try {
            DBCacheImpl.getInsDbCache().initCache();
        } catch (Exception e) {
            Log.i("lazyNetwork: ","this cannot come");
        }
    }
}
