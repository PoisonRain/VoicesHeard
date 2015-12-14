package biz.zacneubert.raspbert.getpodcast;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import biz.zacneubert.raspbert.getpodcast.RSS.RssItem;
import biz.zacneubert.raspbert.getpodcast.Settings.Setting_Root_Folder;

/**
 * Created by zacneubert on 9/18/15.
 */
public class Episode implements Comparable<Episode> {
    public String name;
    public String filename;
    public String url;
    public Podcast podcast;
    public Date publishDate;

    public File file; //NOT USUALLY !NULL

    public Episode(RssItem item) {

    }

    public Episode(String name, String filename, String url, Podcast podcast, Date PublishDate) {
        this.name = name;
        this.filename = filename;
        this.url = url;
        this.podcast = podcast;
        this.publishDate = PublishDate;
    }

    public Episode(File f) {
        this.name = f.getName();
        this.filename = f.getName();
        this.url = null;
        this.podcast = null;
        this.publishDate = new Date(f.lastModified());

        this.file = f;
    }

    public boolean exists(Context c) {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        String location = storageDir + "/" + Setting_Root_Folder.getSavedValue(c) + "/" + podcast.Name + "/" + filename;
        File f = new File(location);
        return f.exists();
    }

    public String getLocation(Context c) {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        String location = storageDir + "/" + Setting_Root_Folder.getSavedValue(c) + "/" + podcast.Name + "/" + filename;
        return location;
    }

    public void download(Boolean hasContext, Context c) {
        try {
            podcast.tryCreateDirectory();
            downloadFile(this.url, getLocation(c), hasContext);
        }
        catch (Exception e) {
            Log.e("Podcast|Alarming", e.toString(), e);
            File f = new File(getLocation(c));
            if(f.exists()) f.delete(); //If download fails, delete the partial
        }
    }

    private void downloadFile(String url, String filepath, Boolean hasContext) throws IOException {
        URL connectURL = new URL(url);
        URLConnection connection = connectURL.openConnection();
        connection.connect();
        int filesize = connection.getContentLength();
        InputStream input = new BufferedInputStream(connectURL.openStream(), 8192);
        OutputStream outstream = new FileOutputStream(filepath);
        byte[] data = new byte[1024];
        int total = 0;

        if(hasContext) podcast.setProgressInitial(filesize, "Downloading " + this.filename);

        int count;
        int percent;
        int postFrequency = 100; //1000 per second
        double oldTime = System.currentTimeMillis();
        while((count = input.read(data)) != -1) {
            total += count;
            percent = (int)((total*100)/filesize);
            double newTime = System.currentTimeMillis();
            if(newTime - oldTime > postFrequency) {
                oldTime = newTime;
                Log.i("Downloading|Alarming", "\t\t"+total);
                try {
                    if (hasContext) podcast.setProgress(total);
                }
                catch (Exception e) {
                    Log.w("Downloading", e.getMessage());
                    //who cares if we can't update progress once
                }
            }
            outstream.write(data, 0, count);
        }
        //Some RSS feeds (maybe all?) have enclosures with file size. Maybe check that out.
        if(hasContext) {
            podcast.setProgressInfo("Download Complete.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            podcast.setProgressDismiss();
        }
        Log.i("Alarming|Downloading", "\t" + name.toString() + " complete");
        outstream.flush();
        outstream.close();
        input.close();
    }

    public boolean delete(Context c) {
        File f = new File(getLocation(c));
        if(f.exists()) return f.delete();
        return false;
    }

    @Override
    public int compareTo(Episode another) {
        return this.publishDate.compareTo(another.publishDate);
    }
}