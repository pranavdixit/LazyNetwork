package com.lazynetworksample;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public interface ClientCallback {

    void success(String data);
    void failure(String data);
}
