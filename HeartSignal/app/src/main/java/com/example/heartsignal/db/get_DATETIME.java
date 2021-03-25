package com.example.heartsignal.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class get_DATETIME {

    public static String get_datetime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String nowdatetime = datetimeFormat.format(date);

        return nowdatetime;
    }

    public static String get_date(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd");
        String nowdatetime = datetimeFormat.format(date);

        return nowdatetime;
    }
}
