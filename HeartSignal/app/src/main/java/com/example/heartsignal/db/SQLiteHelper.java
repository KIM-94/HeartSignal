package com.example.heartsignal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteHelper extends SQLiteOpenHelper {

    // heartrate 테이블
    public static final String heartrate_TABLE_NAME       = "heartrate";
    public static final String heartrate_COLUMN_ID        = "id";
    public static final String heartrate_COLUMN_DATE      = "time";
    public static final String heartrate_COLUMN_HEARTRATE      = "hr";
    public static final String heartrate_COLUMN_STATE     = "st";

    private static final String DATABASE_CREATE_HEARTRATE = "create table "
            + heartrate_TABLE_NAME + "(" + heartrate_COLUMN_ID + " integer primary key autoincrement, "
            + heartrate_COLUMN_DATE + " text, "
            + heartrate_COLUMN_HEARTRATE + " integtexter, "
            + heartrate_COLUMN_STATE + " text);";

    // steps 테이블
    public static final String steps_TABLE_NAME       = "steps";
    public static final String steps_COLUMN_ID        = "id";
    public static final String steps_COLUMN_DATE      = "time";
    public static final String steps_COLUMN_INTENSITY      = "intensity";
    public static final String steps_COLUMN_STEPS     = "steps";
    public static final String steps_COLUMN_STATE     = "st";

    private static final String DATABASE_CREATE_STEPS = "create table "
            + steps_TABLE_NAME + "(" + steps_COLUMN_ID + " integer primary key autoincrement, "
            + steps_COLUMN_DATE + " text, "
            + steps_COLUMN_INTENSITY + " integer, "
            + steps_COLUMN_STEPS + " integer, "
            + steps_COLUMN_STATE + " text);";

    // gyro sensor 테이블 - datetime, x, y, z, x+y+z
    public static final String gyrosensor_TABLE_NAME       = "gyrosensor";
    public static final String gyrosensor_COLUMN_ID        = "id";
    public static final String gyrosensor_COLUMN_DATE      = "time";
    public static final String gyrosensor_COLUMN_GYRO_X      = "gyrosensor_x";
    public static final String gyrosensor_COLUMN_GYRO_Y     = "gyrosensor_y";
    public static final String gyrosensor_COLUMN_GYRO_Z     = "gyrosensor_z";
    public static final String gyrosensor_COLUMN_GYRO_SUM     = "gyrosensor_sum";

    private static final String DATABASE_CREATE_GYROSENSOR = "create table "
            + gyrosensor_TABLE_NAME + "(" + gyrosensor_COLUMN_ID + " integer primary key autoincrement, "
            + gyrosensor_COLUMN_DATE + " text, "
            + gyrosensor_COLUMN_GYRO_X + " float, "
            + gyrosensor_COLUMN_GYRO_Y + " float, "
            + gyrosensor_COLUMN_GYRO_Z + " float, "
            + gyrosensor_COLUMN_GYRO_SUM + " float);";


    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // 앱을 삭제후 앱을 재설치하면 기존 DB파일은 앱 삭제시 지워지지 않기 때문에
        // 테이블이 이미 있다고 생성 에러남
        // 앱을 재설치시 데이터베이스를 삭제해줘야함.
        db.execSQL("DROP TABLE IF EXISTS " + heartrate_TABLE_NAME);
        db.execSQL(DATABASE_CREATE_HEARTRATE);
        db.execSQL("DROP TABLE IF EXISTS " + steps_TABLE_NAME);
        db.execSQL(DATABASE_CREATE_STEPS);
        db.execSQL("DROP TABLE IF EXISTS " + gyrosensor_TABLE_NAME);
        db.execSQL(DATABASE_CREATE_GYROSENSOR);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + heartrate_TABLE_NAME);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + steps_TABLE_NAME);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + gyrosensor_TABLE_NAME);
        onCreate(db);

// 기존 테이블에 레코드 추가시 사용
//        if (oldVersion < 2) {
//            db.execSQL(DATABASE_ALTER_TEAM_1);
//        }
//        if (oldVersion < 3) {
//            db.execSQL(DATABASE_ALTER_TEAM_2);
//        }
    }

    public void insertHeartrateData(String a, int b, String c) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(heartrate_COLUMN_DATE, a);
            cv.put(heartrate_COLUMN_HEARTRATE, b);
            cv.put(heartrate_COLUMN_STATE, c);

            Log.d("", "SQL insert heartrate- " + cv);

            db.insert("heartrate", null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    // datetime, intensity, steps, state
    public void insertStepsData(String a, int b, int c, String d) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(steps_COLUMN_DATE, a);
            cv.put(steps_COLUMN_INTENSITY, b);
            cv.put(steps_COLUMN_STEPS, c);
            cv.put(steps_COLUMN_STATE, d);

            Log.d("", "SQL insert steps - " + cv);

            db.insert("steps", null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    // datetime, gyro_x, gyro_y, gyro_z, gyro_sum
    public void insertGyroSensorData(String a, float b, float c, float d, float e) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(gyrosensor_COLUMN_DATE, a);
            cv.put(gyrosensor_COLUMN_GYRO_X, b);
            cv.put(gyrosensor_COLUMN_GYRO_Y, c);
            cv.put(gyrosensor_COLUMN_GYRO_Z, d);
            cv.put(gyrosensor_COLUMN_GYRO_SUM, e);

            Log.d("", "SQL insert gyrosensor - " + cv);

            db.insert("gyrosensor", null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public ArrayList<HashMap<String, String>> getStepsContacts(String date) {
        ArrayList<HashMap<String, String>> contactList = new ArrayList<>();

//        date = "2021/03/17";
        String timeA = date + " 00:00:00";
        String timeB = date + " 23:59:59";

        String selectQuery2 = "SELECT * FROM steps WHERE time BETWEEN " + "'" + timeA + "'" + " AND " + "'" +timeB+"'" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery2, null);

        // looping through all rows and adding to list
        while (cursor.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();

//            Log.d("", "1 : " + cursor.getColumnIndex(steps_COLUMN_DATE));
//            Log.d("", "2 : " + cursor.getColumnIndex(steps_COLUMN_INTENSITY));
//            Log.d("", "3 : " + cursor.getColumnIndex(steps_COLUMN_STEPS));
//            Log.d("", "4 : " + cursor.getColumnIndex(steps_COLUMN_STATE));

            hashMap.put(steps_COLUMN_DATE, cursor.getString(cursor.getColumnIndex(steps_COLUMN_DATE)));
            hashMap.put(steps_COLUMN_INTENSITY, cursor.getString(cursor.getColumnIndex(steps_COLUMN_INTENSITY)));
            hashMap.put(steps_COLUMN_STEPS, cursor.getString(cursor.getColumnIndex(steps_COLUMN_STEPS)));
            hashMap.put(steps_COLUMN_STATE, cursor.getString(cursor.getColumnIndex(steps_COLUMN_STATE)));

            contactList.add(hashMap);
        }

        // return contact list
        return contactList;
    }


    public ArrayList<HashMap<String, String>> getHeartrateContacts(String date) {
        ArrayList<HashMap<String, String>> contactList = new ArrayList<>();

//        date = "2021/03/17";
        String timeA = date + " 00:00:00";
        String timeB = date + " 23:59:59";

        String selectQuery2 = "SELECT * FROM heartrate WHERE time BETWEEN " + "'" + timeA + "'" + " AND " + "'" +timeB+"'" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery2, null);

        // looping through all rows and adding to list
        while (cursor.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();

            Log.d("", "1 : " + cursor.getColumnIndex(heartrate_COLUMN_DATE));
            Log.d("", "2 : " + cursor.getColumnIndex(heartrate_COLUMN_HEARTRATE));
            Log.d("", "3 : " + cursor.getColumnIndex(heartrate_COLUMN_STATE));

            hashMap.put(heartrate_COLUMN_DATE, cursor.getString(cursor.getColumnIndex(heartrate_COLUMN_DATE)));
            hashMap.put(heartrate_COLUMN_HEARTRATE, cursor.getString(cursor.getColumnIndex(heartrate_COLUMN_HEARTRATE)));
            hashMap.put(heartrate_COLUMN_STATE, cursor.getString(cursor.getColumnIndex(heartrate_COLUMN_STATE)));

            contactList.add(hashMap);
        }
        return contactList;
    }
}