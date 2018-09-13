package com.lkk.free.mergedadaptersample;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lkk.free.mergedadapter.MergedListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MergedListAdapter mergedListAdapter;
    private ArrayAdapter adapter1;
    private ArrayAdapter adapter2;
    private List list1 = new ArrayList();
    private List list2 = new ArrayList();
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list1.addAll(Arrays.asList(1, 2, 3));
        list2.addAll(Arrays.asList("hello", "world"));

        adapter1 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list1);
        adapter2 = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, list2);

        mergedListAdapter = MergedListAdapter.newInstance().setAdapters(adapter1, adapter2);
        //mergedListAdapter = new MergedListAdapter.Builder<ArrayAdapter>(adapter1, adapter2).build();

        ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(mergedListAdapter);
    }

    public void updateList(View view) {
        list1.add(4);
        adapter1.notifyDataSetChanged();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                list2.add("Merged adapter");
                adapter2.notifyDataSetChanged();
            }
        }, 2000);
    }
}
