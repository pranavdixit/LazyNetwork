package com.lazynetworksample;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Random;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class FakeServerThread extends Thread {

    ClientCallback clientCallback;
    Handler handler;
    String success = "server success!";
    String failure ="server failure :(";

    public FakeServerThread(ClientCallback clientCallback, Handler handler) {
        this.clientCallback = clientCallback;
        this.handler = handler;
    }


    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Random random = new Random();
        int num = random.nextInt(100);
        if (num % 2 != 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (clientCallback != null)
                        clientCallback.success(success);
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (clientCallback != null)
                        clientCallback.failure(failure);
                }
            });

        }


    }

    void register(ClientCallback clientCallback) {
        this.clientCallback = clientCallback;
    }

    void deregister() {
        clientCallback = null;
    }
}
