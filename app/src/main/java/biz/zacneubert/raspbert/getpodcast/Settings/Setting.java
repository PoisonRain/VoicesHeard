package biz.zacneubert.raspbert.getpodcast.Settings;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by zacneubert on 11/23/15.
 */
public abstract class Setting {
    //public abstract String getSavedValue(Context c);
    public static String getSavedValue() {
        return null;
    }

    public static boolean setSavedValue(Context c, String value) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(getKey(), value);
            editor.commit();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    //public abstract String getKey();
    public static String getKey() {
        return null;
    }

    public abstract View getView(Context c);
}
