package biz.zacneubert.raspbert.getpodcast.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by zacneubert on 11/23/15.
 */
public class Setting_Theme extends Setting {
    public static String getSavedValue(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getString(getKey(), "BLUE");
    }

    public static String getKey() {
        return "SETTING_THEME";
    }

    @Override
    public View getView(Context c) {
        LayoutInflater inflater = LayoutInflater.from(c);
        return new Setting_Theme_View(inflater, c).getView();
    }
}
