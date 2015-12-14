package biz.zacneubert.raspbert.getpodcast.AutoDownload;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.app.PendingIntent;

import java.util.ArrayList;
import java.util.List;

import biz.zacneubert.raspbert.getpodcast.Episode;
import biz.zacneubert.raspbert.getpodcast.Pebble.ID;
import biz.zacneubert.raspbert.getpodcast.Podcast;
import biz.zacneubert.raspbert.getpodcast.R;
import biz.zacneubert.raspbert.getpodcast.SummaryActivity;

/**
 * Created by zacneubert on 9/23/15.
 */
public class AutoDownloadService extends Service {
    public static List<Episode> allNewEpisodes;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("Alarming", "It created, so that's good I suppose.");
    }

    public void runDownloadService(final Boolean hasContext) {
        Log.i("Alarming", "Entered Service.onStartCommand");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("Checking for podcasts...");
        notificationBuilder.setSmallIcon(R.drawable.whiteicon);

        Log.i("Alarming", "Created passive notification");

        Notification n = notificationBuilder.build();
        this.startForeground(ID.getID(), n);

        Log.i("Alarming", "Executed this.startForeground");

        final Context context = this.getApplicationContext();
        final AutoDownloadService thisService = this;

        Thread doNetworking = new Thread() {
            public void run() {
                Log.i("Alarming", "Hello I am alarming");
                //Toast.makeText(c, "Hello I am alarming.", Toast.LENGTH_LONG).show();
                Boolean episodesFound = false;
                allNewEpisodes = new ArrayList<Episode>();
                Log.i("Alarming", "Checking for episodes...");
                List<Podcast> podcasts = Podcast.getPodcasts();
                //Toast.makeText(c, "Hello I have podcasts.", Toast.LENGTH_LONG).show();
                for(Podcast podcast : podcasts) {
                    try {
                        Podcast.clearHighlight(podcast);
                        Log.i("Alarming", podcast.Name);
                        //Toast.makeText(c, "Hello I am " + podcast.Name, Toast.LENGTH_SHORT).show();
                        List<Episode> Episodes = podcast.getNewEpisodeList(false, context);
                        allNewEpisodes.addAll(Episodes);
                        for (Episode e : Episodes) {
                            try {
                                //Toast.makeText(c, "\tHello I am " + e.name, Toast.LENGTH_SHORT).show();
                                Log.i("Alarming", "\t" + e.name);
                                Podcast.setHighlighted(podcast);
                                e.download(hasContext, context);
                                episodesFound = true;
                            } catch (Exception exp) {
                                Log.e("Alarming", "\tFailed: " + exp.toString());
                            }
                        }
                    }
                    catch (Exception podExp) {
                        //keep going even if a whole podcast fails
                        Log.e("Alarming", "Failed: " + podExp.toString());
                    }
                }

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setSmallIcon(R.drawable.whiteicon);

                int notificationID = ID.getID();

                Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(uri);
                if(allNewEpisodes.size() != 0) {
                    Log.i("Alarming", "New episodes found");
                    mBuilder.setContentTitle("New Episodes have arrived!");
                    StringBuilder sb = new StringBuilder();
                    for(Episode e : allNewEpisodes) {
                        sb.append(e.name);
                        sb.append("\n");
                    }
                    mBuilder.setContentText(sb.toString());//allNewEpisodes.get(0).name + " downloaded");
                }
                else {
                    Log.i("Alarming", "No new episodes found");
                    mBuilder.setContentTitle("No new episodes.");
                    mBuilder.setContentText(":(");
                }
                Intent notificationIntent = new Intent(context, SummaryActivity.class);
                PendingIntent openAppIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                mBuilder.setContentIntent(openAppIntent);

                Log.i("Alarming", "Making Notification...");
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(notificationID, mBuilder.build());

                Log.i("Alarming", "We have reached the end... together.");
                thisService.stopForeground(true);
                thisService.stopSelf();

                //Toast.makeText(c, "Hello I am not broken (yet)", Toast.LENGTH_LONG).show();
            }
        };
        doNetworking.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context c = this.getApplicationContext();
        Boolean hasContext =intent.getBooleanExtra("hasContext", false);
        this.runDownloadService(hasContext);
        return START_STICKY;
    }

}
