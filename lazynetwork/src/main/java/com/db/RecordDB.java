package com.db;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by pranav.dixit on 19/05/17.
 */

public interface RecordDB {

    void insert(final String table, final ContentValues cv, final DbCallback dbCallback, final String tag);
    Cursor query(final String table, final String[] columns, final String selection, final String[] args,final String orderBy, final DbCallback dbCallback);
    void asyncQuery(final String table, final String[] columns, final String selection, final String[] args,final String orderBy, final DbCallback dbCallback,final String tag);
    void update(final String table, final ContentValues cv, final String where, final String[] args, final DbCallback dbCallback, final String tag);
    void deleteRows(final String table, final String selection, final String[] args,final DbCallback dbCallback,final String tag);
    void clear(final String table);
    void closeDb();


}
