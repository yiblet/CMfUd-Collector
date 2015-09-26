package com.hackcmu.cmfudcollector;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private GraphView mGraphView;
    private LineGraphSeries<DataPoint> xSeries;
    private LineGraphSeries<DataPoint> ySeries;
    private LineGraphSeries<DataPoint> zSeries;

    private long referenceTime;

    private SensorManager mManager;
    private Sensor mAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGraphView = new GraphView(this);
        addContentView(mGraphView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(500);

        xSeries = new LineGraphSeries<>();
        xSeries.setTitle("X");
        xSeries.setColor(Color.RED);
        ySeries = new LineGraphSeries<>();
        ySeries.setTitle("Y");
        ySeries.setColor(Color.GREEN);
        zSeries = new LineGraphSeries<>();
        zSeries.setTitle("Z");
        zSeries.setColor(Color.BLUE);
        mGraphView.addSeries(xSeries);
        mGraphView.addSeries(ySeries);
        mGraphView.addSeries(zSeries);

        mManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Time time = new Time();
        time.setToNow();
        referenceTime = time.toMillis(true);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        Time time = new Time();
        time.setToNow();
        long currentTime = (time.toMillis(true) - referenceTime) / 100;
        xSeries.appendData(new DataPoint(currentTime, event.values[0] * 100), true, 200);
        ySeries.appendData(new DataPoint(currentTime, event.values[1] * 100), true, 200);
        zSeries.appendData(new DataPoint(currentTime, event.values[2] * 100), true, 200);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
