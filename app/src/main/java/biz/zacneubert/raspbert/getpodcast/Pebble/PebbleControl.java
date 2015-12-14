package biz.zacneubert.raspbert.getpodcast.Pebble;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import biz.zacneubert.raspbert.getpodcast.AutoDownload.AutoDownloadService;
import biz.zacneubert.raspbert.getpodcast.Podcast;
import biz.zacneubert.raspbert.getpodcast.PodcastApplication;
import biz.zacneubert.raspbert.getpodcast.WakeLocker;

/**
 * Created by zacneubert on 9/26/15.
 */
public class PebbleControl {
    public static final int MaxPodcastCount = 15;
    public static final int MaxEpisodeCount = 15;

    public static PebbleKit.PebbleNackReceiver nackReceiver;
    public static PebbleKit.PebbleAckReceiver ackReceiver;
    public static PebbleKit.PebbleDataReceiver dataReceiver;

    public static Activity parentActivity;

    private static int tid = 0;
    public static int getTid() {
        return ++tid;
    }

    public static List<PebbleMessage> messageList = new ArrayList<PebbleMessage>();

    private static final UUID PEBBLE_UUID = UUID.fromString("667d03da-32a0-4d6b-84f3-57a011dadb68");

    public static Context context;

    public static void playFileFromSleep(Context context, File file) { //hopefully
        KeyguardManager.KeyguardLock keyguardLock = null;
        try {
            if(context != null) {
                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
                keyguardLock = keyguardManager.newKeyguardLock("keyLockPebble");
                keyguardLock.disableKeyguard();

                WakeLocker.acquire(context);
            }
            Podcast.playEpisodeFromFile(parentActivity, file);
            Thread.sleep(2000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            WakeLocker.release();
            if(keyguardLock != null) keyguardLock.reenableKeyguard();
        }
    }

    public static void initialize(Activity act, Context context) {
        parentActivity = act;

        nackReceiver = new PebbleKit.PebbleNackReceiver(PEBBLE_UUID) {
            @Override
            public void receiveNack(Context context, int i) {
                Log.i("PebblePod", "Nack Received: " + i);
                for(PebbleMessage pm : messageList) {
                    if(pm.tid%256 == i) {
                        pm.send(context);
                        break;
                    }
                }
            }
        };

        ackReceiver = new PebbleKit.PebbleAckReceiver(PEBBLE_UUID) {
            @Override
            public void receiveAck(Context context, int i) {
                Log.i("PebblePod", "Ack Received: " + i);
                for(int j=0; j<messageList.size(); j++) {
                    PebbleMessage pm = messageList.get(j);
                    if(pm.tid == i) {
                        messageList.remove(pm);
                        break;
                    }
                }
            }
        };

        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_UUID) {
            @Override
            public void receiveData(Context context, int i, PebbleDictionary pebbleDictionary) {
                //pebbleDictionary
                PebbleKit.sendAckToPebble(context, i);
                for(int j=0; j<pebbleDictionary.size(); j++) {
                    String s = pebbleDictionary.getString(j);
                    String[] split = s.split(" ");
                    boolean playNewest = split[0].equals("newest");
                    if(playNewest) {
                        s = s.replace("newest ", "");
                    }
                    for(Podcast p : PodcastApplication.allPodcasts()) {
                        if(p.Name.equals(s)) {
                            if(!playNewest)
                                sendEpisodesToPebble(context, p);
                            else {
                                playFileFromSleep(context, p.getNewestEpisode());
                                break;
                            }
                        }
                        for(File file : p.getLocalEpisodeFiles()) {
                            if(file.getName().equals(s)) {
                                playFileFromSleep(context, file);
                                break;
                            }
                        }
                    }
                    switch(s) {
                        case "list":
                            sendPodcastTitlesToPebble(context);
                            break;
                        case "Download All":
                            Intent intent = new Intent(context, AutoDownloadService.class);
                            intent.putExtra("hasContext", false);
                            context.startService(intent);
                            break;
                        default:
                            break;
                    }
                    Log.i("PebblePod", "Data Received: " + s);
                }

            }
        };

        PebbleKit.registerReceivedAckHandler(context, ackReceiver);
        PebbleKit.registerReceivedNackHandler(context, nackReceiver);
        PebbleKit.registerReceivedDataHandler(context, dataReceiver);
    }

    public static void sendStringToPebble(Context context, int key, String string) {
        sendStringToPebble(context, key, string, getTid());
    }

    public static void sendStringToPebble(Context context, int key, String string, int TID) {
        if(TID < 0) TID = getTid();
        PebbleDictionary pd = new PebbleDictionary();
        pd.addString(key, string);
        Log.i("PebblePod", "Sending " + string + " with tid: " + TID);
        PebbleKit.sendDataToPebbleWithTransactionId(context, PEBBLE_UUID, pd, TID);
    }

    public static void sendListToPebble(Context context, List<String> strings) {
        PebbleDictionary pd = new PebbleDictionary();
        int i=0;
        for(String s : strings) {
            i++;
            pd.addString(i, s);
        }
        int TID = getTid();
        Log.i("PebblePod", "Sending data with tid: " + TID);
        PebbleKit.sendDataToPebbleWithTransactionId(context, PEBBLE_UUID, pd, TID);
    }

    public static void sendPodcastTitlesToPebble(Context context) {
        List<String> strings = new ArrayList<String>();
        int i=0;
        for(Podcast p : PodcastApplication.allPodcasts()) {
            PebbleMessage pm = new PebbleMessage(getTid(), i, p.Name);
            messageList.add(pm);
            pm.send(context);
            i++;
            if(i > MaxPodcastCount-2) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendEpisodesToPebble(Context context, Podcast podcast) {
        List<String> strings = new ArrayList<String>();
        int i=0;
        for(String e : podcast.getLocalEpisodeNames()) {
            String[] splitString = e.split("/");
            String filename = splitString[splitString.length-1];
            PebbleMessage pm = new PebbleMessage(getTid(), i++, filename);
            messageList.add(pm);
            pm.send(context);
            if(i==MaxEpisodeCount) return;
        }
    }
}
