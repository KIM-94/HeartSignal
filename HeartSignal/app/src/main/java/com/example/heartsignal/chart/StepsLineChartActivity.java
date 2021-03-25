package com.example.heartsignal.chart;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.heartsignal.etc.BackPressCloseHandler;
import com.example.heartsignal.MainActivity;
import com.example.heartsignal.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class StepsLineChartActivity extends AppCompatActivity {

    private BackPressCloseHandler backPressCloseHandler;

    MainActivity mymainactivity = new MainActivity();
    LineChart mChart;

    TextView textX2, textY2, textZ2;

    Button finishbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawgraph);

        backPressCloseHandler = new BackPressCloseHandler(this);

        //----------------------Chart init---------------------------------//
        init();

        init();
        threadStart();

        finishbtn = (Button)findViewById(R.id.finishbtn);
        finishbtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }


    // implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    private void init() {
        textX2 = (TextView)findViewById(R.id.textX2);
        textY2 = (TextView)findViewById(R.id.textY2);
        textZ2 = (TextView)findViewById(R.id.textZ2);

        mChart = (LineChart) findViewById(R.id.mChart);
        chartInit();
    }

    private void chartInit() {

        mChart.setDrawGridBackground(true);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.setGridBackgroundColor(Color.BLACK);

// description text
        mChart.getDescription().setEnabled(true);
        Description des = mChart.getDescription();
        des.setEnabled(true);
        des.setText("Real-Time DATA");
        des.setTextSize(15f);
        des.setTextColor(Color.WHITE);

// touch gestures (false-비활성화)
        mChart.setTouchEnabled(false);

// scaling and dragging (false-비활성화)
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);

//auto scale
        mChart.setAutoScaleMinMaxEnabled(true);

// if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

//X축
        mChart.getXAxis().setDrawGridLines(true);
        mChart.getXAxis().setDrawAxisLine(false);

        mChart.getXAxis().setEnabled(true);
        mChart.getXAxis().setDrawGridLines(false);

//Legend
        Legend l = mChart.getLegend();
        l.setEnabled(true);
        l.setFormSize(10f); // set the size of the legend forms/shapes
        l.setTextSize(12f);
        l.setTextColor(Color.WHITE);

//Y축
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(getResources().getColor(R.color.white));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(getResources().getColor(R.color.white));

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


// don't forget to refresh the drawing
        mChart.invalidate();
    }

    public void chartUpdate ( float x, float y, float z){

        LineData data = mChart.getData();

        if (data == null) {
            data = new LineData();
            mChart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        ILineDataSet X_set = data.getDataSetByIndex(0);
        ILineDataSet Y_set = data.getDataSetByIndex(1);
        ILineDataSet Z_set = data.getDataSetByIndex(2);
        // set.addEntry(...); // can be called as well

        if (X_set == null && Y_set == null && Z_set == null) {
            X_set = createSet(R.color.red, "X ");
            data.addDataSet(X_set);
            Y_set = createSet(R.color.green, "Y ");
            data.addDataSet(Y_set);
            Z_set = createSet(R.color.blue, "Z ");
            data.addDataSet(Z_set);
        }

//        if (Y_set == null) {
//            Y_set = createSet(R.color.green, "Y ");
//            data.addDataSet(Y_set);
//        }

        textX2.setText("X : "+x);
        textY2.setText("Y : "+y);
        textZ2.setText("Z : "+z);

        data.addEntry(new Entry((float)X_set.getEntryCount(), (float)x), 0);
        data.addEntry(new Entry((float)Y_set.getEntryCount(), (float)y), 1);
        data.addEntry(new Entry((float)Z_set.getEntryCount(), (float)z), 2);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();

        mChart.setVisibleXRangeMaximum(150);
        // this automatically refreshes the chart (calls invalidate())
        mChart.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
    }

    @SuppressLint("ResourceType")
    private LineDataSet createSet(int color, String XYZ) {


        LineDataSet set = new LineDataSet(null, XYZ+"Real-time Line Data");
        set.setLineWidth(1f);
        set.setDrawValues(false);
        set.setValueTextColor(getResources().getColor(color));
        set.setColor(getResources().getColor(color));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        set.setHighLightColor(Color.rgb(190, 190, 190));

        return set;
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) { // Message id 가 0 이면
                float x, y, z;
                //a = (int) (Math.random() * 100);
                x = mymainactivity.GYRO_X;
                y = mymainactivity.GYRO_Y;
                z = mymainactivity.GYRO_Z;
                chartUpdate(x, y, z);
            }
        }
    };

    class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void threadStart() {
        MyThread thread = new MyThread();
        thread.setDaemon(true);
        thread.start();
    }
}
