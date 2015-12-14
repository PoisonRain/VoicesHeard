package biz.zacneubert.raspbert.getpodcast.Pebble;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zacneubert on 9/23/15.
 */
public class ID {
    private final static AtomicInteger c = new AtomicInteger(777);
    public static int getID() {
        return c.incrementAndGet();
    }
}