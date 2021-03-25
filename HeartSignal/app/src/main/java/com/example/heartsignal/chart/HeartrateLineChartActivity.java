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

public class HeartrateLineChartActivity extends AppCompatActivity {

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
        }) ;
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

    public void chartUpdate ( float irValue){

        LineData data = mChart.getData();
        if (data == null) {
            data = new LineData();
            mChart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        ILineDataSet IR_set = data.getDataSetByIndex(0);
        if (IR_set == null) {
            IR_set = createSet(R.color.red, "IR ");
            data.addDataSet(IR_set);
        }


        data.addEntry(new Entry((float)IR_set.getEntryCount(), (float)irValue), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();

        mChart.setVisibleXRangeMaximum(150);
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
                float ir=0, bpm=0;
                String state = "";
                //a = (int) (Math.random() * 100);
                ir = mymainactivity.HEARTRATE_IR;
                bpm = mymainactivity.HEARTRATE_BPM;
                state = mymainactivity.HEARTRATE_STATE;
                chartUpdate(ir);

                textX2.setText("IR : "+ir);
                textY2.setText("BPM : "+bpm);
                textZ2.setText("STATE : "+state);
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
