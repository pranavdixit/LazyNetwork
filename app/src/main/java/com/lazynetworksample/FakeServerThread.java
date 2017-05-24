package com.lazynetworksample;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Random;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class FakeServerThread extends Thread{

    ClientCallback clientCallback;
    Handler handler;

    public FakeServerThread(ClientCallback clientCallback,Handler handler){
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
        int num = random.nextInt();
        if(num%3 != 0){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    clientCallback.success("server success!");
                }
            });
        }else{
            handler.post(new Runnable() {
                @Override
                public void run() {
                    clientCallback.failure("server failure :(");
                }
            });

        }


    }
}
