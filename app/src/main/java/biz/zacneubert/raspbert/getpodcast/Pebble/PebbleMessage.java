package biz.zacneubert.raspbert.getpodcast.Pebble;

import android.content.Context;

/**
 * Created by zacneubert on 9/27/15.
 */
public class PebbleMessage {
    public int tid;
    public String value;
    public int key;
    public int retryCount;

    public static int MaxRetries = 5;

    public PebbleMessage(int tid, int key, String val) {
        this.tid = tid;
        this.key = key;
        this.value = val;
        retryCount = 0;
    }

    public void send(Context context) {
        if(retryCount++ > MaxRetries) return;
        this.tid = PebbleControl.getTid();
        PebbleControl.sendStringToPebble(context, key, (String) value, this.tid);
    }
}
