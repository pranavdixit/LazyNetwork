package com.lazynetworksample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by pranav.dixit on 23/05/17.
 */

public class SampleActivityList extends Activity implements View.OnClickListener{

    Button btnClickMe;
    RecyclerView rv;
    SampleListAdapter sampleListAdapter;
    ArrayList<FakePojo> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_list_layout);
        btnClickMe = (Button) findViewById(R.id.button);
        btnClickMe.setOnClickListener(this);

        rv = (RecyclerView) findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        for(int i=0;i<10;i++){
            list.add(new FakePojo(""+i,""+i));
        }
        sampleListAdapter = new SampleListAdapter(list,this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        rv.setAdapter(sampleListAdapter);

    }

    @Override
    public void onClick(View v) {
        sampleListAdapter.onClickItem(v,rv.getChildLayoutPosition(v));
//        startActivity(new Intent(this,SampleActivity.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sampleListAdapter.destroy();
    }
}
