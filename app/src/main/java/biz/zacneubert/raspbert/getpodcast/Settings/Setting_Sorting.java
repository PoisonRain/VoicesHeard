package biz.zacneubert.raspbert.getpodcast.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by zacneubert on 11/26/15.
 */
public class Setting_Sorting extends Setting {
    public static String getSavedValue(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getString(getKey(), "ALPHABETICAL");
    }

    public static String getKey() {
        return "SETTING_SORTING";
    }

    @Override
    public View getView(Context c) {
        LayoutInflater inflater = LayoutInflater.from(c);
        return new Setting_Sorting_View(inflater, c).getView();
    }
}
