package biz.zacneubert.raspbert.getpodcast.AutoDownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Created by zacneubert on 9/26/15.
 */
public class AutoDownloadRefresh extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AlarmRefresh", "AutoDownloadRefresh Intent Received.");

        RefreshTimes(context);
    }

    public static class TimeStruct {
        public int Hour;
        public int Minute;

        public TimeStruct(int h, int m) {
            Hour = h;
            Minute = m;
        }

        public TimeStruct(String h, String m) {
            Hour = Integer.parseInt(h);
            Minute = Integer.parseInt(m);
        }
    }

    public static List<TimeStruct> getRefreshTimes(Context c) {
        List<TimeStruct> times = LoadTimes(c);

        return times;
    }

    public static File AlarmFile(Context context) {
        return new File(Environment.getExternalStorageDirectory() +
                "/" + context.getResources().getString(R.string.PodcastFolder) + "/" +
                context.getResources().getString(R.string.AlarmFilename));
    }

    public static List<TimeStruct> LoadTimes(Context c) {
        File inFile = AutoDownloadRefresh.AlarmFile(c);

        Scanner sc = null;
        try {
            if(!inFile.exists()) inFile.createNewFile();
            sc = new Scanner(inFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<TimeStruct> timeStructList = new ArrayList<TimeStruct>();
        String line;
        String[] splitLine;
        while(sc.hasNextLine()) {
            line = sc.nextLine();
            splitLine = line.split(",");
            timeStructList.add(new TimeStruct(splitLine[0], splitLine[1]));
        }
        return timeStructList;
    }

    public static void SaveTimes(Context c, List<TimeStruct> times) {
        try {
            File saveFile = AlarmFile(c);
            if(!saveFile.exists()) saveFile.createNewFile();
            PrintWriter pw = new PrintWriter(saveFile);
            for (TimeStruct t : times) {
                pw.write(""+t.Hour);
                pw.write(",");
                pw.write(""+t.Minute);
                pw.write("\n");
            }
            pw.close();
        }
        catch (FileNotFoundException fnfe) {
            Log.e("Podcast|Alarm", "File not found", fnfe);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void RefreshTimes(Context c) {
        int i=0;
        for(TimeStruct t : getRefreshTimes(c)) {
            i++;
            RefreshTime(t, c, i);
        }
    }

    public static void RefreshTime(TimeStruct t, Context context, int requestCode) {
        AutoDownloadControl.setAlarm(t.Hour, t.Minute, context, requestCode);
    }
}
