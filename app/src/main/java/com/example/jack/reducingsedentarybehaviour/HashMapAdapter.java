package com.example.jack.reducingsedentarybehaviour;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Jack on 17/03/2017.
 */

public class HashMapAdapter extends BaseAdapter {

    private ArrayList mData;

    public HashMapAdapter( TreeMap<String,String> map){


        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount(){
        return mData.size();
    }

    @Override
    public TreeMap.Entry<String, String> getItem(int position) {
        return (TreeMap.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_steps, parent, false);
        } else {
            result = convertView;
        }

        TreeMap.Entry<String, String> item = getItem(position);

        // TODO replace findViewById by ViewHolder
        ((TextView) result.findViewById(android.R.id.text1)).setText(item.getKey());
        ((TextView) result.findViewById(android.R.id.text2)).setText(item.getValue());

        return result;
    }
}