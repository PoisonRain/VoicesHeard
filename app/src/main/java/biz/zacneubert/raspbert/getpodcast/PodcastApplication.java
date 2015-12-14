package biz.zacneubert.raspbert.getpodcast;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.zacneubert.raspbert.getpodcast.Settings.Setting_Theme;

/**
 * Created by zacneubert on 11/23/15.
 */
public class PodcastApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LoadSettings();
    }

    public static List<Podcast> _allPodcasts = null;
    public static List<Podcast> allPodcasts() {
        return allPodcasts(false);
    }
    public static List<Podcast> allPodcasts(Boolean fromSource) {
        if(_allPodcasts == null || fromSource) _allPodcasts = Podcast.getPodcasts();
        Collections.sort(_allPodcasts);
        return _allPodcasts;
    }

    private static Set<String> _starredEpisodes = null;
    public static Set<String> starredEpisodes() {
        return starredEpisodes(false);
    }
    public static Set<String> starredEpisodes(Boolean fromSource) {
        if(_starredEpisodes == null || fromSource) {
            _starredEpisodes = getStarredEpisodes();
        }
        return _starredEpisodes;
    }
    public static Set<String> getStarredEpisodes() {
        Set<String> starred = new HashSet<>();
        String allStarred = sharedPreferences.getString("STARRED_EPISODES", "");
        String[] splitStarred = allStarred.split(",");
        for(String star : splitStarred) {
            starred.add(star);
        }
        return starred;
    }
    public static void addStarredEpisode(String filename) {
        _starredEpisodes.add(filename);
        setStarredEpisodes();
    }
    public static void removeStarredEpisode(String filename) {
        _starredEpisodes.remove(filename);
        setStarredEpisodes();
    }
    public static boolean toggleStarredEpisode(String filename) {
        if(isStarred(filename)) {
            removeStarredEpisode(filename);
            return false;
        }
        else {
            addStarredEpisode(filename);
            return true;
        }
    }
    public static boolean isStarred(String filename) {
        return _starredEpisodes.contains(filename);
    }
    public static void setStarredEpisodes() {
        StringBuilder sb = new StringBuilder();
        String sep = ",";
        for(String star : _starredEpisodes) {
            sb.append(star);
            sb.append(sep);
        }
        if(sb.length() > 0)
            sb.deleteCharAt(sb.length()-1);
        String allStarred = sb.toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("STARRED_EPISODES", allStarred);
        editor.commit();
    }



    public int foreColor;
    public int backColor;
    public int washColor;
    public int darkColor;
    public int highlightColor;

    public static SharedPreferences sharedPreferences;

    public void LoadSettings() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /*
        THEME SECTION
        */
        //String key = getResources().getString(R.string.THEME_KEY);
        //String theme = sharedPreferences.getString(key, getResources().getString(R.string.DEFAULT_THEME));

        String theme = Setting_Theme.getSavedValue(this);

        highlightColor = ContextCompat.getColor(this, R.color.gold);

        switch (theme) {
            default:
            case "BLUE":
                foreColor = ContextCompat.getColor(this, R.color.dkBlue);
                washColor = ContextCompat.getColor(this, R.color.ltBlue);
                darkColor = ContextCompat.getColor(this, R.color.dkdkBlue);
                break;
            case "RED":
                foreColor = ContextCompat.getColor(this, R.color.RED);
                washColor = ContextCompat.getColor(this, R.color.WASHED_RED);
                darkColor = ContextCompat.getColor(this, R.color.DARK_RED);
                break;
            case "GREEN":
                foreColor = ContextCompat.getColor(this, R.color.GREEN);
                washColor = ContextCompat.getColor(this, R.color.WASHED_GREEN);
                darkColor = ContextCompat.getColor(this, R.color.DARK_GREEN);
                break;
            case "PURPLE":
                foreColor = ContextCompat.getColor(this, R.color.PURPLE);
                washColor = ContextCompat.getColor(this, R.color.WASHED_PURPLE);
                darkColor = ContextCompat.getColor(this, R.color.DARK_PURPLE);
                break;
        }
        backColor = darkColor;

        _starredEpisodes = starredEpisodes();
    }
}
