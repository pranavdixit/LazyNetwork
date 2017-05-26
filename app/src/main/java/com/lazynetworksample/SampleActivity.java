package com.lazynetworksample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lazynetwork.ExecutorCallback;
import com.lazynetwork.NetworkRecord;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class SampleActivity extends Activity implements View.OnClickListener,ClientCallback,ExecutorCallback{

    Button btnClickMe;
    TextView tvReadMe;
    NetworkRecord<FakePojo> networkRecord;
    FakePojo fakePojo;
    FakeServerThread fakeServerThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_layout);
        btnClickMe = (Button) findViewById(R.id.button);
        tvReadMe = (TextView) findViewById(R.id.textView);

        btnClickMe.setOnClickListener(this);
        fakeServerThread = new FakeServerThread(this, new Handler());

        try {
            networkRecord = new NetworkRecord<>(this,"fakePOJO");
        } catch (Exception e) {
            e.printStackTrace();
        }
        fakePojo = new FakePojo("pranav","abc123");
        checkIsStatusPresent();

    }

    public void checkIsStatusPresent(){
        if(networkRecord.isRecorded(fakePojo,FakePojo.class)){
            tvReadMe.setText("we have received your response, we will update shortly");
        }
    }

    @Override
    public void onClick(View v) {
            networkRecord.createRecord(fakePojo);

        tvReadMe.setText("we have received your response, we will update shortly");
        startActivity(new Intent(this,SampleActivityList.class));
//        finish();
    }

    @Override
    public void success(final String data){
            networkRecord.removeRecord(fakePojo);
        tvReadMe.setText(data);

    }

    @Override
    public void failure(final String data) {
        tvReadMe.setText(data);
//        Toast.makeText(this,data,Toast.LENGTH_LONG).show();

    }

    @Override
    public void execute(String requestJson) {
        Log.i("lazy","executing server command");
        fakeServerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fakeServerThread.deregister();
        networkRecord.deregister();
    }
}
