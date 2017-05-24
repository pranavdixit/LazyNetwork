package com.lazynetworksample;

import android.app.Application;

import com.lazynetwork.LazyNetwork;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class LazyNetworkSampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LazyNetwork.init(this);
    }
}

