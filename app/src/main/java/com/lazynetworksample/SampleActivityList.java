package com.lazynetworksample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class SampleActivityList extends Activity implements View.OnClickListener{

    Button btnClickMe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_list_layout);
        btnClickMe = (Button) findViewById(R.id.button);
        btnClickMe.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this,SampleActivity.class));
    }
}
