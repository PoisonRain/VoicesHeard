package biz.zacneubert.raspbert.getpodcast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import biz.zacneubert.raspbert.getpodcast.RSS.RssEnclosure;
import biz.zacneubert.raspbert.getpodcast.RSS.RssFeed;
import biz.zacneubert.raspbert.getpodcast.RSS.RssItem;
import biz.zacneubert.raspbert.getpodcast.RSS.RssReader;
import biz.zacneubert.raspbert.getpodcast.Settings.Setting_Naming;
import biz.zacneubert.raspbert.getpodcast.Settings.Setting_Sorting;

import static android.support.v4.app.ActivityCompat.startActivity;


/**
 * Created by zacneubert on 9/16/15.
 */
public class Podcast implements Comparable<Podcast>
{
    public String Name, Url;
    public int Limit;
    public static SummaryActivity summaryActivity;
    public static Context context;
    public static ProgressUnit progressDialog;
    public static Handler handler;
    public int priority = 0;

    private static List<String> highlighted = new ArrayList<>();
    public static void setHighlighted(Podcast p) {
        synchronized (highlighted) {
            if (!isHighlighted(p)) highlighted.add(p.Name);
        }
    }
    public static void clearHighlight(Podcast p) {
        synchronized (highlighted) {
            highlighted.remove(p.Name);
        }
    }
    public static boolean isHighlighted(Podcast p) {
        synchronized (highlighted) {
            return highlighted.contains(p.Name);
        }
    }

    public View podcastView;

    static Queue<Episode> EpisodeQueue = new LinkedList<Episode>();
    public static void DownloadQueue(Context c) {
        while(!EpisodeQueue.isEmpty()) {
            final Episode episode = EpisodeQueue.remove();
            makeToast("Downloading " + episode.filename);
            episode.podcast.priority++;
            episode.download(true, c);
            refreshUI();
        }
        //Log.i("RSS Reader", "Finished Queue");
    }

    public static void refreshUI() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                summaryActivity.refreshUI();
            }
        });
    }

    public static void makeToast(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public File[] ExistingEpisodes() {
        File location = getLocation();
        File[] files = location.listFiles();
        String[] strs = location.list();
        return files;
    }

    public static void ConstantDownload(Context c) {
        while(true) {
            DownloadQueue(c);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public Podcast(String name, String url, int limit) {
        Name=name;
        Url=url;
        Limit=limit;
    }

    public static void triggerPodcastSet(final List<Podcast> list, final boolean toast, final Context c) {
        Thread worker = new Thread() {
            public void run() {
                if(toast) makeToast("Checking for episodes...");
                Boolean newFound = false;
                for(Podcast podcast : list) {
                    newFound = newFound || podcast.enqueueEpisodes(c);
                }
                if(!newFound && toast) makeToast("No new episodes");
                //DownloadQueue();
                //This is now covered by the worker Thread(s) in SummaryActivity
            }
        };
        worker.start();
    }

    public boolean containsUrl(String url) {
        for(Episode e : EpisodeQueue) {
            String a = url;
            String b=e.url;
            Boolean eq = e.url.equals(url);
            if(e.url.equals(url))
                return true;
        }
        return false;
    }

    Boolean enqueueEpisodes(Context c) {
        List<Episode> episodes = getNewEpisodeList(true, c);
        for(Episode e : episodes) {
            if(!EpisodeQueue.contains(e)) {
                EpisodeQueue.add(e);
            }
        }
        return episodes.size() > 0;
    }

    public List<String> getLocalEpisodeNames() {
        List<String> files = new ArrayList<>();
        for(File file : getLocalEpisodeFiles()) files.add(file.getAbsolutePath());
        return files;
    }

    public List<File> getLocalEpisodeFiles() {
        List<File> files = new ArrayList<>();
        for(File f : this.getLocation().listFiles()) files.add(f);
        return files;
    }

    public List<Episode> getNewEpisodeList(boolean doMakeToast, Context c) {
        try {
            List<Episode> newEpisodes = new ArrayList<Episode>();
            List<File> oldFiles = this.getLocalEpisodeFiles();
            Boolean newEpisodeFound = false;
            RssFeed feed = RssReader.read(new URL(this.Url));
            List<RssItem> entries = feed.getRssItems();
            Log.i("RSS Reader", feed.getTitle());
            Log.i("RSS Reader", feed.getLink());
            Log.i("RSS Reader", feed.getDescription());
            if(entries.size() <Math.abs(Limit)) Limit = entries.size();
            if(Limit > 0) {
                entries = entries.subList(0,Limit);
            }
            else if(Limit < 0) {
                entries = entries.subList(entries.size() + Limit, entries.size());
            }
            else if(Limit == 0) {
                entries = new ArrayList<RssItem>();
            }
            List<String> newFiles = new ArrayList<String>();
            String filename = "";
            for(RssItem item : entries) {
                //RssItem item = entries.get(i);
                Log.i("RSS Reader", item.getTitle());
                Log.i("RSS Reader", "XXXXX\tContent: "+item.getContent());
                Log.i("RSS Reader", "XXXXX\tDate:"+item.getPubDate().toString());
                Log.i("RSS Reader", "XXXXX\tLink: "+item.getLink());
                Log.i("RSS Reader", "XXXXX\tEnclosures: ");
                for(RssEnclosure enc : item.getEnclosures()) {
                    Log.i("RSS Reader", "     \t" + enc.toString());
                    String encType = enc.getType();
                    String encUrl = enc.getValue();
                    filename = getFileName(encUrl);
                    String extension = getFileExtension(encUrl);

                    String NamingType = Setting_Naming.getSavedValue(context);

                    try {
                        String titleName = item.getTitle() + extension;
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                        String datename = sdf.format(item.getPubDate()) + extension;

                        if (NamingType.equals("Default")) {
                            //Do nothing, the filename is currently correct
                        } else if (NamingType.equals("Title")) {
                            filename = titleName;
                        } else if (NamingType.equals("Publish Date")) {
                            filename = datename;
                        }
                    }
                    catch (Exception e) {
                        Log.d("Podcast|Naming", "Failed to change episode name", e);
                    }

                    newFiles.add(filename);
                    String qname = enc.getQName();
                    if(qname.equals("url")) {
                        Episode e = new Episode(item.getTitle(), filename, encUrl, this, item.getPubDate());
                        if(!e.exists(c) && !containsUrl(e.url)) {
                            newEpisodeFound = true;
                            newEpisodes.add(e);
                        }
                    }
                }
            }
            Boolean deletedFile = false;
            for(File f : oldFiles) {
                if(!newFiles.contains(f.getName()) && !PodcastApplication.isStarred(f.getName())) {
                    f.delete();
                    if(doMakeToast) makeToast("Deleted old file " + f.getName());
                    deletedFile = true;
                }
            }
            if(deletedFile && doMakeToast) refreshUI();
            return newEpisodes;
        } catch (Exception exp) {
            Log.e("Podcast", exp.getMessage(), exp);
            if(doMakeToast) makeToast("Error " + exp.getMessage() + " occurred. Please try again.");
            return new ArrayList<Episode>();
        }
    }

    public void delete() {
        try {
            for (File f : ExistingEpisodes()) {
                if (f.exists()) f.delete();
            }
            File folder = getLocation();
            if (folder.exists()) folder.delete();
        }
        catch (Exception e) {
            Log.e("Podcast", e.getMessage(), e);
            makeToast("Error deleting: " + e.getMessage());
        }
    }

    void threadedEnqueue(final Context c) {
        makeToast("Checking for " + this.Name + "...");
        final Podcast thisPodcast = this;
        Thread queuer = new Thread(){
          public void run() {
              if(!enqueueEpisodes(c)) {
                  makeToast("No new episodes.");
              }
              else {
                  setHighlighted(thisPodcast);
              }
          }
        };
        queuer.start();
    }

    public File getLocation() {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        File PodcastDirectory = new File(storageDir + "/GetPodcasts/" + this.Name);
        if(!PodcastDirectory.exists()) {
            PodcastDirectory.mkdirs();
        }
        return PodcastDirectory;
    }

    public void tryCreateDirectory() {
        File location = getLocation();
        if(!location.exists()) {
            location.mkdirs();
        }
    }

    public static String getFileName(String url) {
        String[] split = url.split("/");
        String name = split[split.length-1];
        return name;
    }

    public static String getFileExtension(String url) {
        String[] split = url.split("/");
        String name = split[split.length-1];
        String[] cutExtension = name.split("\\.");
        String extension = "." + cutExtension[cutExtension.length-1];
        return extension;
    }

    public void setProgressInitial(final int max, final String Message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setInitialProgress(max, Message);
            }
        });
    }

    public void setProgress(final int progress) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgress(progress);
            }
        });
    }

    public void setProgressInfo(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.finishProgress(message);
            }
        });
    }

    public void setProgressDismiss() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setGone();
            }
        });
    }

    public static void setSummaryActivity(SummaryActivity sa) {
        summaryActivity = sa;
    }

    public static void setHandler(Handler h) {
        handler = h;
    }

    public static void setContext(Context c) {
        context = c;
    }

    public static void setProgressUnit(ProgressUnit pd) {
        progressDialog = pd;
    }

    public static File saveFile = new File(Environment.getExternalStorageDirectory() + "/GetPodcasts/" + "config.txt");
    public static void savePodcasts(List<Podcast> podcasts) {
        if(!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter writer = new PrintWriter(saveFile);
            for(Podcast pod : podcasts) {
                StringBuilder sb = new StringBuilder(pod.Name);
                sb.append(",");
                sb.append(pod.Url);
                sb.append(",");
                sb.append(pod.Limit);
                sb.append("\n");
                writer.write(sb.toString());
            }
            writer.close();
        }
        catch (IOException e) {
            makeToast("Failed to save.");
        }
    }

    //public Podcast(String name, String url, int limit) {
    public static List<Podcast> getPodcasts() {
        try {
            Scanner sc = new Scanner(saveFile);
            List<Podcast> podcasts = new ArrayList<Podcast>();
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] pieces = line.split(",");
                podcasts.add(new Podcast(pieces[0], pieces[1], Integer.parseInt(pieces[2])));
            }
            return podcasts;
        }
        catch (IOException e) {
            makeToast("No save file found.");
            return new ArrayList<Podcast>();
        }
    }

    public View getView() {
        TextView textview = new TextView(this.context);
        textview.setText(Name);
        return textview;
    }

    public File getNewestEpisode() {
        File newest = null;
        for(File e :ExistingEpisodes()) {
            if(newest == null || e.lastModified() > newest.lastModified()) newest = e;
        }
        return newest;
    }

    public Date getNewestEpisodeDate() {
        File newestEpisode = getNewestEpisode();
        if(newestEpisode == null) return new Date(0);
        return new Date(newestEpisode.lastModified());
    }

    @Override
    public int compareTo(Podcast another) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String sortType = sp.getString(new Setting_Sorting().getKey(), context.getString(R.string.DEFAULT_SORTING));

        switch (sortType) {
            case "Alphabetical":
                return this.Name.compareTo(another.Name);
            default:
            case "Most Recent":
                if(this.getNewestEpisodeDate().before(another.getNewestEpisodeDate())) return 1;
                return -1;
        }
    }


    public static void playEpisodeFromFile(File f) {playEpisodeFromFile(null, f);}
    public static void playEpisodeFromFile(Activity parentActivity, File file) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        if(parentActivity != null) startActivity(parentActivity, intent, Bundle.EMPTY);
        Log.i("Podcast|playfile", "No context or activity.");
    }

    public void playNewestEpisode() {playNewestEpisode(null);}
    public void playNewestEpisode(Activity parentActivity) {
        File file = getNewestEpisode();
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        if(context != null) context.startActivity(intent);
        else if(parentActivity != null) startActivity(parentActivity, intent, Bundle.EMPTY);
        Log.i("Podcast|playNewest", "No context or activity.");
    }
}
