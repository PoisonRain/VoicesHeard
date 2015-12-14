package biz.zacneubert.raspbert.getpodcast.AutoDownload;

/**
 * Created by zacneubert on 9/22/15.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import biz.zacneubert.raspbert.getpodcast.Pebble.ID;

public class AutoDownloadControl extends BroadcastReceiver {
    public static AlarmManager alarmMgr;
    public static PendingIntent alarmIntent;
    public static Intent intent;

    public void setAlarm(int startHour, int startMinute, final Context context) {
        setAlarm(startHour, startMinute, context, 0);
    }

    public static void setAlarm(int startHour, int startMinute, final Context context, int requestCode) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, AutoDownloadControl.class);
        alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMinute);
        String str = calendar.toString();
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        AutoDownloadRefresh.TimeStruct t = new AutoDownloadRefresh.TimeStruct(startHour, startMinute);
        List<AutoDownloadRefresh.TimeStruct> timelist = new ArrayList<>();
        timelist.add(t);
        AutoDownloadRefresh.SaveTimes(context, timelist);
    }

    public void cancel() {
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //final Context c = context;
        Log.i("Alarming", "Received intent");
        int serviceID = ID.getID();
        AutoDownloadService autoDownloadService = new AutoDownloadService();
        Log.i("Alarming", "Created service");
        Intent i = new Intent(context, AutoDownloadService.class);
        Log.i("Alarming", "Created intent");
        context.startService(i);
        Log.i("Alarming", "Started Service");
    }
}
