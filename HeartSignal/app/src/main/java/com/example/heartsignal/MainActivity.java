package com.example.heartsignal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.heartsignal.chart.HeartrateLineChartActivity;
import com.example.heartsignal.chart.StepsLineChartActivity;
import com.example.heartsignal.db.SQLiteHelper;
import com.example.heartsignal.db.get_DATETIME;
import com.example.heartsignal.db.get_DB;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public SQLiteHelper mSQLiteHelper;
    public get_DATETIME mDatetime ;

    public ConstraintLayout steps_btn, heartrate_btn, heartrateChartnoti;
    public LottieAnimationView heartrate_lottie, lottie_finger;
    public TextView steps_text, heartrate_text, Bluetoothvalue1;
    public Button stepschartbutton, heartratechartbutton, startHRmonitor, startSTEPmonitor,ble_btn;
    public Button phonebtn1, phonebtn2, phonebtn3, phonebtn4;


    boolean IsConnect0 = false, IsConnect1 = false;

    boolean SHOCK_DETECT = false;
    int SHOCK_COUNT = 0;
    int INTENSITY_COUNT = 0;

    boolean INTENSITY_DETECT = false;

    BluetoothAdapter BA;
    BluetoothDevice B0,B1;

    ConnectThread BC0;
    ConnectThread BC1;

    ConnectedThread connectedThread;

    ArrayList array0;
    ArrayList array1;

    final String B0MA = "98:D3:31:FD:34:86"; //Bluetooth0 MacAddress
    final String B1MA = "98:D3:71:FD:49:1E"; //Bluetooth1 MacAddress

    final String SPP_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //SPP UUID
    final UUID SPP_UUID = UUID.fromString(SPP_UUID_STRING);

    final int DISCONNECT = 0;
    final int CONNECTING = 1;
    final int CONNECTED = 2;
    final int INPUTDATA = 9999;
    final int STEPS_INPUTDATA = 1001;
    final int HEARTRATE_INPUTDATA = 1002;
    final int GYRO_INPUTDATA = 1003;

    int STEPS = 0;
    int HEARTRATE = 0;
    float BPM = 0;
    String STATE = "";
    float add_BPM = 0;
    int count = 1;

    public static float GYRO_X, GYRO_Y, GYRO_Z;
    public static float HEARTRATE_IR, HEARTRATE_BPM;
    public static String HEARTRATE_STATE = "";


    private Thread stepscheckThread = null;
    public static Boolean isRunning = true;

    private BarChart stepsbarchart, heartratebarchart;
    private PieChart stepspiechart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd");
        String nowdatetime = datetimeFormat.format(date);

        //        activity_init();
        //----------------------SQLite---------------------------------//
        mSQLiteHelper = new SQLiteHelper(getApplicationContext(), "HeartSignal.db", null, 1);
        STEPS = get_DB.get_steps(mSQLiteHelper);
        HEARTRATE = get_DB.get_heartrate(mSQLiteHelper);

        // lottie 설정
        heartrate_lottie = (LottieAnimationView) findViewById(R.id.heartrate_lottie);
        lottie_finger = (LottieAnimationView) findViewById(R.id.lottie_finger);
        stopAnimation(heartrate_lottie);
        stopAnimation(lottie_finger);

        //----------------------View Init---------------------------------//
        steps_btn = (ConstraintLayout)findViewById(R.id.steps_btn);
        heartrate_btn = (ConstraintLayout)findViewById(R.id.heartrate_btn);
        heartrateChartnoti = (ConstraintLayout)findViewById(R.id.heartrateChartnoti);

        steps_text = (TextView)findViewById(R.id.steps_text);
        heartrate_text = (TextView)findViewById(R.id.heartrate_text);
        Bluetoothvalue1 = (TextView)findViewById(R.id.Bluetoothvalue1);

        ble_btn = (Button) findViewById(R.id.ble_btn);
        startSTEPmonitor = (Button)findViewById(R.id.startSTEPmonitor);
        heartratechartbutton = (Button)findViewById(R.id.heartratechartbutton);
        startHRmonitor = (Button)findViewById(R.id.startHRmonitor);

        phonebtn1 = (Button)findViewById(R.id.phonebtn1);
        phonebtn2 = (Button)findViewById(R.id.phonebtn2);
        phonebtn3 = (Button)findViewById(R.id.phonebtn3);
        phonebtn4 = (Button)findViewById(R.id.phonebtn4);

        stepsbarchart = (BarChart)findViewById(R.id.stepsbarchart);
        heartratebarchart = (BarChart)findViewById(R.id.heartratechart);

        stepspiechart = (PieChart)findViewById(R.id.stepspiechart);

        //----------------------SET Listener---------------------------------//
//        connectbtn0.setOnClickListener(this);
        ble_btn.setOnClickListener(this);

        steps_btn.setOnClickListener(this);
        heartrate_btn.setOnClickListener(this);

        heartratechartbutton.setOnClickListener(this);
        startHRmonitor.setOnClickListener(this);

        phonebtn1.setOnClickListener(this);
        phonebtn2.setOnClickListener(this);
        phonebtn3.setOnClickListener(this);
        phonebtn4.setOnClickListener(this);

        heartrateChartnoti.setOnClickListener(this);
        startSTEPmonitor.setOnClickListener(this);

        //----------------------Bluetooth init---------------------------------//
        BA = BluetoothAdapter.getDefaultAdapter();

        if(!BA.isEnabled()){
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i,5000);
        }

        B0 = BA.getRemoteDevice(B0MA);
        B1 = BA.getRemoteDevice(B1MA);

        steps_text.setText(""+STEPS);
        heartrate_text.setText(""+HEARTRATE);


        // main 스레드 - 움직임, 걸음, 충격, 밴트착용 데이터 수집
        stepscheckThread = new Thread(new stepscheckThread());
        try{
            isRunning = true;
            stepscheckThread.start();
        } catch (Exception e){
//            Toast.makeText(getApplicationContext(),"웨어러블 장비와 연결해 주세요.",Toast.LENGTH_SHORT).show();
        }

        //----------------------Steps BarChart---------------------------------//
        ArrayList<BarEntry> dataVals = get_DB.get_steps_dataVals(mSQLiteHelper.getStepsContacts(get_DATETIME.get_date()));
        // bar chart 생성
        BarDataSet bardataset = new BarDataSet(dataVals, "steps");
        bardataset.setHighLightAlpha(10);
        bardataset.setColor(Color.parseColor("#C3B2B4"));
        bardataset.getXMax();
        bardataset.setLabel("steps");
        bardataset.setValueTextSize(0);     // 데이터 값 제거

        BarData data3 = new BarData(bardataset);
        stepsbarchart.setData(data3);
        stepsbarchart.getBarData().getBarWidth();
        stepsbarchart.getXAxis().setDrawGridLines(false);
        stepsbarchart.getAxisLeft().setDrawGridLines(false);
        stepsbarchart.getAxisRight().setDrawGridLines(false);
        stepsbarchart.setDrawGridBackground(false); //격자구조 넣을건지
        stepsbarchart.setPinchZoom(false); // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
        stepsbarchart.getAxisLeft().setDrawLabels(false); // 값 적는거 허용 (0, 50, 100)
        stepsbarchart.getAxisRight().setDrawLabels(false); // 값 적는거 허용 (0, 50, 100)
        stepsbarchart.getXAxis().setDrawLabels(false);
        stepsbarchart.getAxisLeft().setDrawAxisLine(false); // 축 그리기 설정
        stepsbarchart.getAxisRight().setDrawAxisLine(false); // 축 그리기 설정
        stepsbarchart.getXAxis().setDrawAxisLine(false);
        stepsbarchart.animateY(1000); // 밑에서부터 올라오는 애니매이션 적용
        stepsbarchart.setTouchEnabled(false); // 그래프 터치해도 아무 변화없게 막음
        stepsbarchart.setDrawGridBackground(false); //격자구조 넣을건지
        stepsbarchart.getDescription().setText(""); // 오른쪽 아래 텍스트 설정


        //----------------------Heartrate BarChart---------------------------------//
        ArrayList<BarEntry> heartratedataVals = get_DB.get_heartrate_dataVals(mSQLiteHelper.getHeartrateContacts(get_DATETIME.get_date()));
        // bar chart 생성
        BarDataSet heartratebardataset = new BarDataSet(heartratedataVals, "heartrate");
        heartratebardataset.setHighLightAlpha(10);
        heartratebardataset.setColor(Color.parseColor("#9CD5DD"));
        heartratebardataset.getXMax();
        heartratebardataset.setLabel("heartrate");
        heartratebardataset.setValueTextSize(0);    // 데이터 값 제거

        BarData heartratedata = new BarData(heartratebardataset);
        heartratebarchart.setData(heartratedata);
        heartratebarchart.getBarData().getBarWidth();
        heartratebarchart.getXAxis().setDrawGridLines(false);
        heartratebarchart.getAxisLeft().setDrawGridLines(false);
        heartratebarchart.getAxisRight().setDrawGridLines(false);
        heartratebarchart.setDrawGridBackground(false); //격자구조 넣을건지
        heartratebarchart.setPinchZoom(false); // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
        heartratebarchart.getAxisLeft().setDrawLabels(false); // 값 적는거 허용 (0, 50, 100)
        heartratebarchart.getAxisRight().setDrawLabels(false); // 값 적는거 허용 (0, 50, 100)
        heartratebarchart.getXAxis().setDrawLabels(false);
        heartratebarchart.getAxisLeft().setDrawAxisLine(false); // 축 그리기 설정
        heartratebarchart.getAxisRight().setDrawAxisLine(false); // 축 그리기 설정
        heartratebarchart.getXAxis().setDrawAxisLine(false);
        heartratebarchart.animateY(1000); // 밑에서부터 올라오는 애니매이션 적용
        heartratebarchart.setTouchEnabled(false); // 그래프 터치해도 아무 변화없게 막음
        heartratebarchart.setDrawGridBackground(false); //격자구조 넣을건지
        heartratebarchart.getDescription().setText(""); // 오른쪽 아래 텍스트 설정



        //----------------------Steps PieChart---------------------------------//
        stepspiechart.setUsePercentValues(true);
        stepspiechart.getDescription().setEnabled(false);
        stepspiechart.setExtraOffsets(5,10,5,5);

        stepspiechart.setDragDecelerationFrictionCoef(0.95f);

        stepspiechart.setDrawHoleEnabled(false);
        stepspiechart.setHoleColor(Color.WHITE);
        stepspiechart.setTransparentCircleRadius(600f);
        stepspiechart.animateY(1000, Easing.EaseInOutCubic); //애니메이션

        ArrayList<PieEntry> yValues = new ArrayList<PieEntry>();
        yValues.add(new PieEntry(STEPS,"steps"));
        yValues.add(new PieEntry(5000f,"Goal"));

        PieDataSet dataSet = new PieDataSet(yValues,"");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData((dataSet));
        data.setValueTextSize(0);
        data.setValueTextColor(Color.YELLOW);

        stepspiechart.setData(data);
    }

    // Lottie animation play
    private void setUpAnimation(LottieAnimationView animationView) {
        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        // 시작
        animationView.playAnimation();
    }

    // Lottie animation pause
    private void stopAnimation(LottieAnimationView animationView) {
        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        // 시작
        animationView.pauseAnimation();
    }

    // 심박수 측정 - 15초(15*1000 ms) 동안 0.1초(100 ms)마다 실행
    CountDownTimer CDT = new CountDownTimer(15 * 1000, 100) {

        public void onTick(long millisUntilFinished) {
            //반복실행할 구문
            add_BPM = BPM + add_BPM;
            count++;
            Log.d("CDT","test "+BPM+" "+add_BPM+" "+count);
        }
        public void onFinish() {
            stopAnimation(heartrate_lottie);

            int get_bpm = (int)(add_BPM/count);
            Log.d("CDT avg",""+get_bpm);

            heartrate_text.setText(""+get_bpm);
            mSQLiteHelper.insertHeartrateData(mDatetime.get_datetime(), get_bpm, "finger");
            add_BPM = 0;
            count = 1;
            isRunning = true;   // main 스레드 동작
        }
    };


    // main 스레드 handler 5초 간격으로 동작
    @SuppressLint("HandlerLeak")
    Handler stepscheckhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                connectedThread.sendData("getsteps");
            } catch (Exception e){
//                Toast.makeText(getApplicationContext(),"웨어러블 장비와 연결해 주세요.",Toast.LENGTH_SHORT).show();
                Bluetoothvalue1.setText("웨어러블 장비와 연결해 주세요.");
            }
        }
    };

    // main 스레드 정의 isRunning = true 인 경우 handler 실행
    public class stepscheckThread implements Runnable {
        @Override
        public void run() {
            int i = 0;

            while (true) {
                while (isRunning) { //일시정지를 누르면 멈춤
                    Message msg = new Message();
                    msg.arg1 = i++;
                    stepscheckhandler.sendMessage(msg);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                            }
                        });
                        return; // 인터럽트 받을 경우 return
                    }
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5000) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    //Bluetooth state -> View Change
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 1){
                switch (msg.arg1){
                    case DISCONNECT:
                        IsConnect1 = false;
                        Bluetoothvalue1.setText("DISCONNECT");
                        ble_btn.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_bluetooth_disabled_24));
                        isRunning = false;
                        break;
                    case CONNECTING:
                        Bluetoothvalue1.setText("CONNECTING");
                        ble_btn.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_settings_bluetooth_24));
                        break;
                    case CONNECTED:
                        IsConnect1 = true;
                        Bluetoothvalue1.setText("CONNECTED");
                        ble_btn.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_bluetooth_connected_24));
                        isRunning = true;
                        break;

                    case GYRO_INPUTDATA:
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                        String nowdatetime = datetimeFormat.format(date);

                        String s = (String)msg.obj;     // 자이로 센서로 부터 수신받은 데이터 형식 -> 0.00(x) 0.00(y) 0.00(z)

                        float gyro_x = Float.parseFloat(s.split(" ")[1]);
                        float gyro_y = Float.parseFloat(s.split(" ")[2]);
                        float gyro_z = Float.parseFloat(s.split(" ")[3].split("\n")[0]);
                        float gyro_sum = gyro_x + gyro_y + gyro_z;

                        GYRO_X = gyro_x;
                        GYRO_Y = gyro_y;
                        GYRO_Z = gyro_z;

                        mSQLiteHelper.insertGyroSensorData(nowdatetime, gyro_x, gyro_y, gyro_z, gyro_sum);
                        Log.d("ble2",s);
                        break;

                    case STEPS_INPUTDATA:
                        long now1 = System.currentTimeMillis();
                        Date date1 = new Date(now1);
//                        SimpleDateFormat datetimeFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                        SimpleDateFormat datetimeFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String nowdatetime1 = datetimeFormat1.format(date1);

                        String steps = (String)msg.obj;     // 자이로 센서로 부터 수신받은 데이터 형식 -> 0.00 0.00 0.00
                        Log.d("STEPS_INPUTDATA", steps);
                        int get_intensity=0;
                        try {
                            get_intensity = Integer.parseInt(steps.split(" ")[3]);
                        } catch (Exception e){

                        }

                        int get_shock = Integer.parseInt(steps.split(" ")[4]);

                        int get_steps = Integer.parseInt(steps.split(" ")[1]);
                        STEPS = STEPS + get_steps;
                        steps_text.setText(Integer.toString(STEPS));

                        String get_state = steps.split(" ")[2];

                        if (get_shock > 0){
                            SHOCK_DETECT = true;
                        }

                        if (get_intensity==0){
                            INTENSITY_DETECT = true;
                        }

                        if (INTENSITY_DETECT){
                            if (get_intensity>0 || get_state.equals("finger")){
                                INTENSITY_DETECT = false;
                                INTENSITY_COUNT = 0;
                            }
                            else{
                                INTENSITY_COUNT++;
                                if(INTENSITY_COUNT>12*60){
                                    // 1시간 이상 움직임이 감지되지 않은 경우
                                    try {
                                        //전송
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage("01039297060", null, "1시간 이상 움직임이 감지되지 않았습니다.", null, null);
                                        Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        if(SHOCK_DETECT){
                            if(get_steps>0){
                                SHOCK_DETECT = false;
                                SHOCK_COUNT = 0;
                            }
                            else {
                                SHOCK_COUNT++;
                                if(SHOCK_COUNT>12){
                                    // 쓰러짐 문자전송
                                    try {
                                        //전송
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage("01039297060", null, "쓰러짐이 감지 되었습니다.", null, null);
                                        Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                    SHOCK_DETECT = false;
                                }
                            }
                        }

                        mSQLiteHelper.insertStepsData(nowdatetime1, get_intensity, get_steps, get_state);
                        isRunning = true;
                        Log.d("steps",steps);
                        break;

                    case HEARTRATE_INPUTDATA:
                        long now2 = System.currentTimeMillis();
                        Date date2 = new Date(now2);
                        SimpleDateFormat datetimeFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                        String nowdatetime2 = datetimeFormat2.format(date2);

                        String heartrate = (String)msg.obj;     // 자이로 센서로 부터 수신받은 데이터 형식 -> 0.00 0.00 0.00
                        Log.d("HEARTRATE_INPUTDATA", heartrate);

                        float irValue = Float.parseFloat(heartrate.split(" ")[1]);
                        HEARTRATE_IR = irValue;

                        float beatsPerMinute = Float.parseFloat(heartrate.split(" ")[2]);
                        BPM = beatsPerMinute;
                        HEARTRATE_BPM = beatsPerMinute;

                        String state = heartrate.split(" ")[3];
                        STATE = state;
                        HEARTRATE_STATE = state;

                        Log.d("steps",heartrate);
                        break;
                }
            }
            return true;
        }
    });

    public void onClick(View v) {
        if(v.getId() == R.id.ble_btn){
            if(IsConnect1){
                //블루투스 연결된 상태
                if(BC1 != null){
                    try {
                        BC1.cancel();

                        Message m = new Message();
                        m.what = 1;
                        m.arg1 = DISCONNECT;
                        handler.sendMessage(m);

                        BC1 = null;
                    } catch (IOException e) { }
                }
            }else{
                //블루투스 끈어진
                array1 = new ArrayList();
                BC1 = new ConnectThread(B1,1);
                BC1.start();
            }
        }

        else if (v.getId() == R.id.steps_btn){
            isRunning = true;
            try {
                connectedThread.sendData("getsteps");
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),"웨어러블 장비와 연결해 주세요.",Toast.LENGTH_SHORT).show();
            }
        }

        else if (v.getId() == R.id.heartrate_btn){
            isRunning = false;
            connectedThread.sendData("max");
            setUpAnimation(heartrate_lottie);
            CDT.start(); //CountDownTimer 실행
        }

        else if (v.getId() == R.id.heartratechartbutton){
            heartrateChartnoti.setVisibility(VISIBLE);
            setUpAnimation(lottie_finger);
        }

        else if (v.getId() == R.id.heartrateChartnoti){
            heartrateChartnoti.setVisibility(GONE);
            stopAnimation(lottie_finger);
        }

        // 심박 센서 모니터링
        else if (v.getId() == R.id.startHRmonitor){
            heartrateChartnoti.setVisibility(GONE);
            stopAnimation(lottie_finger);

            try {
                connectedThread.sendData("max");
                isRunning = false;
                Intent intent=new Intent(MainActivity.this, HeartrateLineChartActivity.class);
                startActivity(intent);
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),"웨어러블 장비와 연결해 주세요.",Toast.LENGTH_SHORT).show();
            }
        }

        // gyro 센서 모니터링
        else if (v.getId() == R.id.startSTEPmonitor){
            try {
                isRunning = false;
                connectedThread.sendData("mpu");
                Intent intent=new Intent(MainActivity.this, StepsLineChartActivity.class);
                startActivity(intent);
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),"웨어러블 장비와 연결해 주세요.",Toast.LENGTH_SHORT).show();
            }
        }

        // 보호자1
        else if (v.getId() == R.id.phonebtn1){
            Intent tt = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:01012345678"));
            startActivity(tt);
        }

        // 보호자2
        else if (v.getId() == R.id.phonebtn2){
            Intent tt = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:01023482345"));
            startActivity(tt);
        }

        // 보호자3
        else if (v.getId() == R.id.phonebtn3){
            Intent tt = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:01033334444"));
            startActivity(tt);
        }

        // 보호자4
        else if (v.getId() == R.id.phonebtn4){
            Intent tt = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:01011112222"));
            startActivity(tt);
        }
    }



    //connect bluetooth
    class ConnectThread extends Thread{
        BluetoothDevice BD;
        BluetoothSocket BS;

        int bluetooth_index;

        ConnectThread(BluetoothDevice device , int index){
            BD = device;
            bluetooth_index = index;
        }

        @Override
        public void run() {
            try {
                sendMessage(CONNECTING);

                BS = BD.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                try{
                    BS.connect();
                } catch (Exception e){
//                    stopAnimation(ble_btn);
                    IsConnect1 = false;
                }

                connectedThread = new ConnectedThread(BS, bluetooth_index);
                connectedThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    cancel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if(connectedThread != null){
                    connectedThread.cancel();
                }
            }
        }

        public void cancel() throws IOException {
            if(BS != null) {
                BS.close();
                BS = null;
            }

            if(connectedThread != null){
                connectedThread.cancel();
            }

            sendMessage(DISCONNECT);
        }

        public void sendMessage(int arg){
            Message m = new Message();
            m.what = bluetooth_index;
            m.arg1 = CONNECTING;

            handler.sendMessage(m);
        }
    }

    //connected bluetooth - communication
    class ConnectedThread extends Thread{

        InputStream in = null;
        OutputStream out = null;

        int bluetooth_index;

        boolean is =false;

        public ConnectedThread(BluetoothSocket bluetoothsocket, int index) {
            bluetooth_index = index;

            try {
                in = bluetoothsocket.getInputStream();
                out = bluetoothsocket.getOutputStream();

                is = true;

                if(bluetooth_index == 0) IsConnect0 = is;
                else IsConnect1 = is;

                sendMessage(CONNECTED);

            } catch (IOException e) {
                cancel();
            }
        }

        public void sendData(String text) {
            try{
                // 데이터 송신
                out.write(text.getBytes());
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BufferedReader Buffer_in = new BufferedReader(new InputStreamReader(in));

            while (is){
                try {
                    String s = Buffer_in.readLine();

                    if(!s.equals("")){
                        String rx_data = s.split(" ")[0];
                        if(rx_data.equals("steps")){
                            sendMessage(STEPS_INPUTDATA,s);
                        }
                        if(rx_data.equals("heartrate")){
                            sendMessage(HEARTRATE_INPUTDATA,s);
                        }
                        if(rx_data.equals("gyro")){
                            sendMessage(GYRO_INPUTDATA,s);
                        }
                    }
                } catch (IOException e) { }
            }

        }

        public void sendMessage(int arg){
            Message m = new Message();
            m.what = bluetooth_index;
            m.arg1 = arg;

            handler.sendMessage(m);
        }

        public void sendMessage(int arg, String s){
            Message m = new Message();
            m.what = bluetooth_index;
            m.arg1 = arg;
            m.obj = s;

            handler.sendMessage(m);
        }

        public void cancel(){
            is = false;

            if(bluetooth_index == 0) IsConnect0 = is;
            else IsConnect1 = is;

            if(in != null){
                try {
                    in.close();
                    in=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sendMessage(DISCONNECT);
        }
    }
}