package com.example.jack.reducingsedentarybehaviour;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.txusballesteros.widgets.FitChart;
import com.txusballesteros.widgets.FitChartValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    final Context context = this;
    private Button button;

    private GoogleApiClient fitClient;
    private HashMap<String, String> stepData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(new SimpleDateFormat("EEEE d MMMM").format(new Date()));//Set day and date as the title
        stepData = new HashMap<>();

        buildFitClient();

        button = (Button) findViewById(R.id.buttonAlert);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                //set Title
                alertDialogBuilder.setTitle("Wakeup");
                //set message
                alertDialogBuilder
                        .setMessage("Click here to do something")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });


    }

    protected void onResume() {
        super.onResume();
        new ViewDailyStepCountTask().execute();//Used to update step count
    }

    private void buildFitClient() {
        //Connect to googleApi
        fitClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .build();
        fitClient.connect();

        new ViewDailyStepCountTask().execute();//Find out current step count
        new ViewWeeklyStepCountTask().execute();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("HistoryAPI", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("HistoryAPI", "onConnectionFailed");
    }

    public void onConnected(@Nullable Bundle bundle) {
        Log.e("HistoryAPI", "onConnected");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent j = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(j);
                return true;
            case R.id.menu_history:
                Intent i = new Intent(getApplicationContext(), HistoryActivity.class);
                i.putExtra("stepData", stepData);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class ViewDailyStepCountTask extends AsyncTask<Long, Void, Long> {

        protected Long doInBackground(Long... params) {
            long total = 0;

            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(fitClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
            } else {
                Log.w(TAG, "There was a problem getting the step count.");
            }
            Log.i(TAG, "Total steps: " + total);

            return total;
        }

        @Override
        protected void onPostExecute(Long result) {
            TextView steps = (TextView) findViewById(R.id.step_text);
            steps.setText((int) (long) result + " steps today");
            makeChart(result);

        }
    }

    private class ViewWeeklyStepCountTask extends AsyncTask<Long, Void, Long> {


        protected Long doInBackground(Long... Params) {
            // Setting a start and end date using a range of 1 week before this moment.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -2);
            long startTime = cal.getTimeInMillis();

            java.text.DateFormat dateFormat = getDateInstance();
            //Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
            //Log.i(TAG, "Range End: " + dateFormat.format(endTime));

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    // The data request can specify multiple data types to return, effectively
                    // combining multiple data queries into one call.
                    // In this example, it's very unlikely that the request is for several hundred
                    // datapoints each consisting of a few steps and a timestamp.  The more likely
                    // scenario is wanting to see how many steps were walked per day, for 7 days.
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                    // bucketByTime allows for a time span, whereas bucketBySession would allow
                    // bucketing by "sessions", which would need to be defined in code.
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();

            // Invoke the HistoryActivity API to fetch the data with the query and await the result of
            // the read request.
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(fitClient, readRequest).await(1, TimeUnit.MINUTES);

            //Reads data returned from fitApi.
            //Data is returned in buckets(in this case bucket = day), look at each day individually
            if (dataReadResult.getBuckets().size() > 0) {
                Log.e("HistoryActivity", "Number of buckets: " + dataReadResult.getBuckets().size());
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        showDataSet(dataSet);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
        }
    }

    private void showDataSet(DataSet dataSet) {

        // Log.e("HistoryActivity", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = new SimpleDateFormat("dd EEEE MMMM");
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
           /* Log.e("HistoryActivity", "Data point:");
            Log.e("HistoryActivity", "\tType: " + dp.getDataType().getName());*/
            Log.e("HistoryActivity", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("HistoryActivity", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.e("HistoryActivity", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                stepData.put((dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS))), dp.getValue(field).toString());
            }
        }


    }

    private void makeChart(float result) {
        final FitChart fitChart = (FitChart) findViewById(R.id.fitChart);
        fitChart.setMinValue(0);
        fitChart.setMaxValue(100);

        //result = 5000;//example value, Remove to use real values
        Resources resources = getResources();
        Collection<FitChartValue> values = new ArrayList<>();
        values.add(new FitChartValue(result / 100, resources.getColor(R.color.chart_value_2)));
        fitChart.setValues(values);

    }

}
