package com.lazynetworksample;

import android.app.Application;
import android.content.Context;

import com.db.LazyNetwork;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class LazyNetworkSampleApplication extends Application {

    private static LazyNetworkSampleApplication instance;

    public static LazyNetworkSampleApplication getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        LazyNetwork.init(this);
//        try {
//            LazyNetwork.clearAllData();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}

