package com.db;

import com.lazynetwork.RecordCallback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pranav.dixit on 22/05/17.
 */

public interface DBCache {

    void initCache() throws Exception;
    void addRecord(String type, String data) throws Exception;
    void removeRecord(String type, String data) throws Exception;
    ArrayList<RecordPOJO> getRecords(String type);
    void updateRecordStatus(String type,String status,String data) throws Exception;
    void clearAllData() throws Exception;
    void clearAllTypeRecords(String type) throws Exception;


}
