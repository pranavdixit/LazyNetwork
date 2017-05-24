package com.db;

import com.lazynetwork.RecordCallback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pranav.dixit on 22/05/17.
 */

public interface DBCache {

    void initCache() throws Exception;
    <E>void addRecord(String type, E object) throws Exception;
    <E>void removeRecord(String type, E object) throws Exception;
    ArrayList<RecordPOJO> getRecords(String type);
    void updateRecordStatus(String type,String status,String data) throws Exception;

}
