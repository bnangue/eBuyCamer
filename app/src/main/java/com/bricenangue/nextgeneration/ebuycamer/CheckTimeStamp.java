package com.bricenangue.nextgeneration.ebuycamer;

import android.content.Context;
import android.provider.Settings;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bricenangue on 28/12/2016.
 */

public class CheckTimeStamp {
    private Context context;
    private long timeStampToCompare;

    public CheckTimeStamp(Context context, long timeStampToCompare) {
        this.context = context;
        this.timeStampToCompare = timeStampToCompare;
    }


    public String checktime(){
        Date date = new Date(timeStampToCompare);
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        final String dateFormatted = formatter.format(date);
        String time=dateFormatted;

        if (DateUtils.isToday(timeStampToCompare)) {

           time=context.getString(R.string.today) + " " +dateFormatted;

        }else if (isdayAgo(timeStampToCompare,-1)){
            //yesterday
            time=context.getString(R.string.yesterday);

        }else if (isdayAgo(timeStampToCompare,-2)){

            time=context.getString(R.string.two_days_ago);

        }else if (isdayAgo(timeStampToCompare,-3)){

            time=context.getString(R.string.three_days_ago);
            //3 days ago

        }else if (isdayAgo(timeStampToCompare,-4)){

            time=context.getString(R.string.four_days_ago);

        }else if (isdayAgo(timeStampToCompare,-5)){

            time=context.getString(R.string.five_days_ago);

        }else if (isdayAgo(timeStampToCompare,-6)){
            time=context.getString(R.string.six_days_ago);
        }else if (isdayAgo(timeStampToCompare,-7)){
            time=context.getString(R.string.a_week_ago);
            //one week ago

        }else if (isdayAgo(timeStampToCompare,-8)){
            time=context.getString(R.string.eight_days_ago);
            //one week ago

        }else if (isdayAgo(timeStampToCompare,-9)){
            time=context.getString(R.string.nine_days_ago);
            //one week ago

        }else if (isdayAgo(timeStampToCompare,-10)){
            time=context.getString(R.string.ten_days_ago);
            //one week ago

        }else if (isdayAgo(timeStampToCompare,-11)){
            time=context.getString(R.string.eleven_days_ago);
            //one week ago

        }else if (isdayAgo(timeStampToCompare,-12)){
            time=context.getString(R.string.twelve_days_ago);
            //one week ago

        }else if (isdayAgo(timeStampToCompare,-13)){
            time=context.getString(R.string.thirteen_days_ago);
            //13 days ago

        }else if (isdayAgo(timeStampToCompare,-14)){
            time=context.getString(R.string.two_weeks_ago);
            //2 week ago

        }else if (isdayAgo(timeStampToCompare,-21)){
            time=context.getString(R.string.three_weeks_ago);
            //3 weeks ago

        }else if (isdayAgo(timeStampToCompare,-30)){
            time=context.getString(R.string.one_month_ago);
            //1 month ago

        }else if (isdayAgo(timeStampToCompare,-60)){
            time=context.getString(R.string.two_months_ago);
            //months ago

        }
        return time;
    }


    public static boolean isdayAgo(long date, int daysAgo) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE,daysAgo);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }
}
