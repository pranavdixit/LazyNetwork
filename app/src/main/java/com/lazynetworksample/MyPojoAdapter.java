package com.lazynetworksample;

/**
 * Created by pranav.dixit on 15/06/17.
 */

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.db.LazyNetwork;
import com.lazynetwork.ExecutorCallback;
import com.lazynetwork.NetworkRecord;

import java.util.ArrayList;

/**
 * Created by pranav.dixit on 24/05/17.
 */

public class MyPojoAdapter extends RecyclerView.Adapter<MyPojoAdapter.SampleViewHolder> implements ExecutorCallback<FakePojo>,ClientCallback {

    ArrayList<FakePojo> list;
    NetworkRecord<FakePojo> networkRecord;
    View.OnClickListener listener;
    ArrayList<FakeServerThread> threadList = new ArrayList<>();

    public MyPojoAdapter(ArrayList<FakePojo> list, View.OnClickListener listener) {
        this.list = list;
        this.listener = listener;
        try {
            networkRecord = new NetworkRecord<FakePojo>(this, "my_pojo");
        } catch (Exception e) {
            Log.i("SampleApp",e.getMessage());
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
        }else if(networkRecord.isRecorded(fakePojo)){
            holder.image.setImageDrawable(LazyNetworkSampleApplication.getContext().getDrawable(R.drawable.ic_cloud_upload_black_24dp));
        }else{
            holder.image.setImageDrawable(LazyNetworkSampleApplication.getContext().getDrawable(R.drawable.ic_cloud_black_24dp));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
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

        Toast.makeText(LazyNetworkSampleApplication.getContext(),"something went wrong, with "+list.get(position).name+" request ",Toast.LENGTH_SHORT).show();
        //optional if you want to remove the record and retry it
    }

    public void onClickItem(View v, int position){
        if(isUploading(list.get(position))) {
            Toast.makeText(LazyNetworkSampleApplication.getContext(),"already uploaded "+list.get(position).name+" data ",Toast.LENGTH_SHORT).show();
            return;
        }
        networkRecord.createRecord(list.get(position));
        notifyItemChanged(position);
    }

    boolean isUploading(FakePojo fakePojo){
        return fakePojo.checked || networkRecord.isRecorded(fakePojo);
    }

    @Override
    public void execute(FakePojo object) {
        FakeServerThread fakeServerThread = new FakeServerThread(this,new Handler());
        fakeServerThread.success = object.id;
        fakeServerThread.failure = object.id;
        threadList.add(fakeServerThread);
        fakeServerThread.start();
    }

    @Override
    public void onCacheUpdated() {
        notifyDataSetChanged();
        Log.i(LazyNetwork.TAG, "list size "+networkRecord.getRecords().size());
        for (FakePojo pojo: networkRecord.getRecords()
                ) {
            Log.i(LazyNetwork.TAG, "id "+pojo.id);
        }
    }


    public static final class SampleViewHolder extends RecyclerView.ViewHolder {
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
        for (FakeServerThread thread: threadList
                ) {
            thread.deregister();
        }
    }
}

