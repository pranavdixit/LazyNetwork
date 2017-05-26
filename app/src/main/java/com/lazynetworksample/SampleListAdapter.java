package com.lazynetworksample;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lazynetwork.ExecutorCallback;
import com.lazynetwork.NetworkRecord;

import java.util.ArrayList;

/**
 * Created by pranav.dixit on 24/05/17.
 */

public class SampleListAdapter extends RecyclerView.Adapter<SampleListAdapter.SampleViewHolder> implements ExecutorCallback,ClientCallback {

    ArrayList<FakePojo> list;
    NetworkRecord<FakePojo> networkRecord;
    View.OnClickListener listener;

    public SampleListAdapter(ArrayList<FakePojo> list, View.OnClickListener listener) {
        this.list = list;
        this.listener = listener;
        try {
            networkRecord = new NetworkRecord<FakePojo>(this, "fakePOJOlist");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public SampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        itemView.setOnClickListener(listener);
        return new SampleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SampleViewHolder holder, int position) {
        FakePojo fakePojo = list.get(position);
        holder.nameTv.setText(list.get(position).name);
       if(fakePojo.checked){
           holder.image.setImageDrawable(LazyNetworkSampleApplication.getContext().getDrawable(R.drawable.ic_cloud_done_black_24dp));
       }else if(networkRecord.isRecorded(fakePojo,FakePojo.class)){
           holder.image.setImageDrawable(LazyNetworkSampleApplication.getContext().getDrawable(R.drawable.ic_cloud_upload_black_24dp));
       }else{
           holder.image.setImageDrawable(LazyNetworkSampleApplication.getContext().getDrawable(R.drawable.ic_cloud_black_24dp));
       }
//        holder.nameTv.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void execute(String requestJson) {
        Gson gson = new Gson();
        FakePojo fakePojo = gson.fromJson(requestJson,FakePojo.class);
        FakeServerThread fakeServerThread = new FakeServerThread(this,new Handler());
        fakeServerThread.success = fakePojo.id;
        fakeServerThread.failure = fakePojo.id;

        fakeServerThread.start();
    }

    @Override
    public void success(String data) {
        int position = Integer.parseInt(data);
        networkRecord.removeRecord(list.get(position));

        list.get(position).checked = true;
        notifyItemChanged(position);
    }

    @Override
    public void failure(String data) {
        int position = Integer.parseInt(data);

        networkRecord.removeRecord(list.get(position));

        list.get(position).checked = false;
        notifyItemChanged(position);

        Toast.makeText(LazyNetworkSampleApplication.getContext(),"something went wrong, with "+list.get(position).name+" request ",Toast.LENGTH_LONG).show();

        //optional if you want to remove the record and retry it
    }

    public void onClickItem(View v, int position){
        networkRecord.createRecord(list.get(position));
        notifyItemChanged(position);
//
//        FakeServerThread fakeServerThread = new FakeServerThread(this,new Handler());
//        fakeServerThread.success = Integer.toHexString(position);
//        fakeServerThread.failure = Integer.toHexString(position);
//
//        fakeServerThread.start();

    }


    public static final class SampleViewHolder extends ViewHolder {
        TextView nameTv;
        ImageView image;

        public SampleViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            image = (ImageView) itemView.findViewById(R.id.checkBox);
        }
    }

    public void destroy(){
        networkRecord.deregister();
    }
}