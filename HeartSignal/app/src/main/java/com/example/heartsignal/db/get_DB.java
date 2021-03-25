package com.example.heartsignal.db;

import android.util.Log;

import com.example.heartsignal.db.SQLiteHelper;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class get_DB {

    private static long now = System.currentTimeMillis();
    private static Date date = new Date(now);
    private static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static String nowdatetime = datetimeFormat.format(date);

    private static int STEPS=0;
    private static int HEARTRATE=0;

    public static int get_steps(SQLiteHelper mSQLiteHelper){
        ArrayList<HashMap<String, String>> mArrayList2;
        mArrayList2 = mSQLiteHelper.getStepsContacts(nowdatetime);

        HashMap<String,String> hashMap1 = new HashMap<>();
        for(int i=0; i<mArrayList2.size(); i++){

            hashMap1 = mArrayList2.get(i);

            String text_Steps = hashMap1.get("steps");
            STEPS = STEPS + Integer.parseInt(text_Steps);
        }
        return STEPS;
    }

    public static int get_heartrate(SQLiteHelper mSQLiteHelper){
        ArrayList<HashMap<String, String>> mArrayList2;
        mArrayList2 = mSQLiteHelper.getHeartrateContacts(nowdatetime);

        String lastElement = null;
        if ( !mArrayList2.isEmpty() ){
            HashMap<String,String> hashMap1 = new HashMap<>();
            hashMap1 = mArrayList2.get(mArrayList2.size() - 1);
            String text_Heartrate = hashMap1.get("hr");
            HEARTRATE = Integer.parseInt(text_Heartrate);
        }
        else {
            HEARTRATE = 0;
        }

        return HEARTRATE;
    }

    public static ArrayList<BarEntry> get_dataVals(ArrayList<HashMap<String, String>> mArrayList2){

        ArrayList<BarEntry> _dataVals = new ArrayList<>();
        List<Entry> humidataValsLine = new ArrayList<>();
        HashMap<String,String> hashMap1 = new HashMap<>();
        long temp_time = 0;
        int temp_steps = 0;

        for (int i=0; i<24; i++){
            _dataVals.add(new BarEntry(i, 0));
        }

        for (int i=0; i<mArrayList2.size(); i++){
            try{
                String[] stTime = mArrayList2.get(i).get(SQLiteHelper.steps_COLUMN_DATE).split(" ")[1].split(":");
                temp_time = Integer.parseInt(stTime[0])*3600 + Integer.parseInt(stTime[1])*60 + (int)Float.parseFloat(stTime[2]);
                temp_time = temp_time/3600;
                temp_steps = Integer.parseInt(mArrayList2.get(i).get(SQLiteHelper.steps_COLUMN_STEPS));
                Log.d("stTime ",""+temp_time/3600);
            } catch (Exception e){
                Log.d("stTime ","null");
            }
            _dataVals.add(new BarEntry(temp_time, temp_steps));
        }
        return _dataVals;
    }

    public static ArrayList<BarEntry> get_steps_dataVals(ArrayList<HashMap<String, String>> mArrayList2){

        ArrayList<BarEntry> _dataVals = new ArrayList<>();
        List<Entry> humidataValsLine = new ArrayList<>();
        HashMap<String,String> hashMap1 = new HashMap<>();

        int temp_time = 0;
        int temp_steps = 0;
        int time_steps[][] = null;
        time_steps = new int[24][3600];

        for (int i=0; i<24; i++){
            time_steps[i][0] = 0;
            _dataVals.add(new BarEntry(i, 0));
        }

        for (int i=0; i<mArrayList2.size(); i++){
            try{
                String[] stTime = mArrayList2.get(i).get(SQLiteHelper.steps_COLUMN_DATE).split(" ")[1].split(":");
                temp_time = Integer.parseInt(stTime[0])*3600 + Integer.parseInt(stTime[1])*60 + (int)Float.parseFloat(stTime[2]);
                temp_time = temp_time/3600;
                temp_steps = Integer.parseInt(mArrayList2.get(i).get(SQLiteHelper.steps_COLUMN_STEPS));

                time_steps[temp_time][i] = temp_steps;

                Log.d("stTime ",""+temp_time/3600);
            } catch (Exception e){
                Log.d("stTime ","null");
            }
        }
        for (int i=0; i<24; i++){
            int avg_steps=0;
            for (int j=0; j<time_steps[i].length; j++){
                avg_steps = time_steps[i][j] + avg_steps;
            }
            _dataVals.add(new BarEntry(i, avg_steps));
        }
        return _dataVals;
    }


    public static ArrayList<BarEntry> get_heartrate_dataVals(ArrayList<HashMap<String, String>> mArrayList2){

        ArrayList<BarEntry> _dataVals = new ArrayList<>();
        List<Entry> humidataValsLine = new ArrayList<>();
        HashMap<String,String> hashMap1 = new HashMap<>();

        int temp_time = 0;
        int temp_heartrate = 0;
        int time_heartrate[][] = null;
        time_heartrate = new int[24][3600];

        for (int i=0; i<24; i++){
            time_heartrate[i][0] = 0;
            _dataVals.add(new BarEntry(i, 0));
        }

        for (int i=0; i<mArrayList2.size(); i++){
            try{
                String[] stTime = mArrayList2.get(i).get(SQLiteHelper.heartrate_COLUMN_DATE).split(" ")[1].split(":");
                temp_time = Integer.parseInt(stTime[0])*3600 + Integer.parseInt(stTime[1])*60 + (int)Float.parseFloat(stTime[2]);
                temp_time = temp_time/3600;
                temp_heartrate = Integer.parseInt(mArrayList2.get(i).get(SQLiteHelper.heartrate_COLUMN_HEARTRATE));

                time_heartrate[temp_time][i] = temp_heartrate;

                Log.d("hrTime ",""+temp_time+" "+i);
            } catch (Exception e){
                Log.d("hrTime ","null");
            }
        }
        for (int i=0; i<24; i++){
            int avg_heartrate=0;
            int count = 0;
            for (int j=0; j<time_heartrate[i].length; j++){
                avg_heartrate = time_heartrate[i][j] + avg_heartrate;
            }
            _dataVals.add(new BarEntry(i, avg_heartrate));
        }
        return _dataVals;
    }
}
