package com.example.jack.reducingsedentarybehaviour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.TreeMap;

public class HistoryActivity extends AppCompatActivity {

    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;
    private HashMap<String, String> stepData;
    private TreeMap<String,String> steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        stepData = (HashMap<String, String>) intent.getSerializableExtra("stepData");

        steps = new TreeMap<>();
        steps.putAll(stepData);

        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.mainListView);

        HashMapAdapter adapter = new HashMapAdapter(steps);
        mainListView.setAdapter(adapter);

      /*  BarChart wChart = (BarChart) findViewById(R.id.weeklyBarchart);
        List<BarModel> bars = new ArrayList<>();
        bars.add(new BarModel("Mon",10500f, 0xFF123456));
        bars.add(new BarModel("Tue",6500f,  0xFF343456));
        bars.add(new BarModel("Wed",7800f, 0xFF563456));
        bars.add(new BarModel("Thurs",12200f, 0xFF873F56));
        bars.add(new BarModel("Fri",1500f, 0xFF56B7F1));
        bars.add(new BarModel("Sat",4700f,  0xFF343456));
        bars.add(new BarModel("Sun",1200f, 0xFF1FF4AC));

        wChart.addBarList(bars);

        wChart.startAnimation();*/
    }

    protected void onResume() {
        super.onResume();
        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.mainListView);

        HashMapAdapter adapter = new HashMapAdapter(steps);
        mainListView.setAdapter(adapter);
    }

}
