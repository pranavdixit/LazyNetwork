package com.lazynetworksample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lazynetwork.ExecutorCallback;
import com.lazynetwork.NetworkRecord;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class SampleActivity extends Activity implements View.OnClickListener,ClientCallback,ExecutorCallback<FakePojo>{

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
            networkRecord = new NetworkRecord<>(this,"fakePOJO",FakePojo.class);
        } catch (Exception e) {
            Log.i("SampleApp",e.getMessage());
        }
        fakePojo = new FakePojo("pranav","abc123");
        checkIsStatusPresent();


    }

    public void checkIsStatusPresent(){
        if(networkRecord.isRecorded(fakePojo)){
            tvReadMe.setText("we have received your response, we will update shortly");
        }
    }

    @Override
    public void onClick(View v) {
            networkRecord.createRecord(fakePojo);

        tvReadMe.setText("we have received your response, we will update shortly");
        startActivity(new Intent(this,SampleActivityList.class));
    }

    @Override
    public void success(final String data){
            networkRecord.removeRecord(fakePojo);
        tvReadMe.setText(data);

    }

    @Override
    public void failure(final String data) {
        tvReadMe.setText(data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fakeServerThread.deregister();
        networkRecord.deregister();
    }

    @Override
    public void execute(FakePojo object) {
        Log.i("lazy","executing server command");
        fakeServerThread.start();
    }
}
