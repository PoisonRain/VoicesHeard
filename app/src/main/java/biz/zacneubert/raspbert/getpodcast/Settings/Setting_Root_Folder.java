package biz.zacneubert.raspbert.getpodcast.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;

import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Created by zacneubert on 11/27/15.
 */
public class Setting_Root_Folder extends Setting {
    public static String getSavedValue(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getString(getKey(), c.getResources().getString(R.string.PodcastFolder));
    }

    public static String getKey() {
        return "SETTING_ROOT_FOLDER";
    }

    @Override
    public View getView(Context c) {
        LayoutInflater inflater = LayoutInflater.from(c);
        return new Setting_Root_Folder_View(inflater, c).getView();
    }
}
