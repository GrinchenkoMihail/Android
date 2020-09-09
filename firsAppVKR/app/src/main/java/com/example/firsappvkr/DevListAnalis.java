package com.example.firsappvkr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DevListAnalis extends BaseAdapter {
    private ListView lisrDev;
    private static final int RESOURS_lAYOUT = R.layout.list_dev;
    private LayoutInflater inflater;
    public ArrayList<String> nameDev = new ArrayList<>();

    public void addDev(String name) {
        nameDev.add(name);
    }

    public void deleteDev(String name) {
        nameDev.remove(name);
    }


    @Override
    public int getCount() {
        return nameDev.size();
    }

    @Override
    public Object getItem(int position) {
        return getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = inflater.inflate(RESOURS_lAYOUT, parent, false);
        ((TextView) view.findViewById(R.id.tv_name_dev)).setText("jr");
        return view;
    }
}
