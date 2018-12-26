package com.siddhantkushwaha.raven.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.siddhantkushwaha.raven.entity.User;

import java.util.ArrayList;

public class CreditsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<User> data;

    public CreditsAdapter(Context context, ArrayList<User> data) {

        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
