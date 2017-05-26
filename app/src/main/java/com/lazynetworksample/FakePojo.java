package com.lazynetworksample;

import com.lazynetwork.RecordCallback;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class FakePojo implements RecordCallback {
    public String name;
    public String id;
    public boolean checked = false;

    FakePojo(String name, String id){
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean recordEqual(Object obj1, Object obj2) {
        FakePojo fakePojo = (FakePojo)obj2;
        if(id.equals(fakePojo.id))
        return true;
        return false;
    }
}
