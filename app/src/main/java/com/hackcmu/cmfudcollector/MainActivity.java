package com.hackcmu.cmfudcollector;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Pubnub pubnub;
    final String pubKey = "pub-c-51f1e02f-37f7-40cc-a7c4-055479877b40";
    final String subKey = "sub-c-af5f4710-640d-11e5-bad4-02ee2ddab7fe";
    final String channel = "a";

    private RelativeLayout mLayout;

    private SensorManager mManager;
    private Sensor mAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (RelativeLayout) findViewById(R.id.main_activity_layout);

        mManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        pubnub = new Pubnub(pubKey, subKey);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManager.unregisterListener(this, mAccelerometer);
    }

    final int DATASET_SIZE = 20;
    boolean b = false;

    ArrayList<Double> xValues = new ArrayList<>();
    ArrayList<Double> yValues = new ArrayList<>();
    ArrayList<Double> zValues = new ArrayList<>();

    boolean canSend = true;

    @Override
    public void onSensorChanged(final SensorEvent event) {
        double xAccel = event.values[0];
        double yAccel = event.values[1];
        double zAccel = event.values[2];

        xValues.add(xAccel);
        yValues.add(yAccel);
        zValues.add(zAccel);

        if (xValues.size() > DATASET_SIZE) {
            xValues.remove(0);
            yValues.remove(0);
            zValues.remove(0);

            if (!canSend) {
                return;
            }

            if (areOutliers(xValues) || areOutliers(yValues) || areOutliers(zValues)) {
                Callback callback = new Callback() {
                    public void successCallback(String channel, Object response) {
                        System.out.println(response.toString());
                        canSend = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                canSend = true;
                            }
                        }, 1000 * 30);
                    }
                    public void errorCallback(String channel, PubnubError error) {
                        System.out.println(error.toString());
                    }
                };
                if (b) {
                    mLayout.setBackgroundColor(Color.BLACK);
                } else {
                    mLayout.setBackgroundColor(Color.WHITE);
                }
                pubnub.publish(channel, "Changed state!", callback);
                b = !b;
                xValues.clear();
                yValues.clear();
                zValues.clear();
            }
        }
    }

    private boolean areOutliers(ArrayList<Double> arrayList) {
        double[] doubleArray = convertToArray(arrayList);

        DescriptiveStatistics statistics = new DescriptiveStatistics(doubleArray);
        double median = statistics.getPercentile(50);

        double[] distFromMedianArray = convertToArray(arrayList);
        for (int idx = 0; idx < DATASET_SIZE; idx++) {
            distFromMedianArray[idx] = Math.abs(doubleArray[idx] - median);
        }

        statistics = new DescriptiveStatistics(distFromMedianArray);
        final double CONSISTENCY_CONSTANT = 1.4826;
        double medianAbsoluteDeviation = statistics.getPercentile(50) * CONSISTENCY_CONSTANT;

        for (int idx = 0; idx < DATASET_SIZE; idx++) {
            distFromMedianArray[idx] /= medianAbsoluteDeviation;
        }
        statistics = new DescriptiveStatistics(distFromMedianArray);
        return statistics.getMax() > 10;
    }

    private double[] convertToArray(ArrayList<Double> arrayList) {
        double[] doubleArray = new double[DATASET_SIZE];
        for (int idx = 0; idx < DATASET_SIZE; idx++) {
            doubleArray[idx] = arrayList.get(idx);
        }
        return doubleArray;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
