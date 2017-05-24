package com.db;

import android.database.Cursor;

/**
 * Created by pranav.d on 12-08-2016.
 */
public interface DbCallback {
    void onQueryResult(Cursor cursor, String tag);
    void onResultInserted(long id, String tag);
    void onResultDeleted(long id, String tag);
    void onResultUpdated(int rows, String tag);
}
