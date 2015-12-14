package biz.zacneubert.raspbert.getpodcast.Settings;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Created by zacneubert on 11/23/15.
 */
public class Setting_Theme_View implements View.OnClickListener {
    Button blueButton;
    Button greenButton;
    Button redButton;
    Button purpleButton;

    View rootView;

    Context c;

    public Setting_Theme_View(LayoutInflater inflater, Context c) {
        rootView = inflater.inflate(R.layout.setting_theme_fragment_layout, null, false);

        this.c = c;

        blueButton = (Button) rootView.findViewById(R.id.setting_theme_blue_button);
        greenButton = (Button) rootView.findViewById(R.id.setting_theme_green_button);
        redButton = (Button) rootView.findViewById(R.id.setting_theme_red_button);
        purpleButton = (Button) rootView.findViewById(R.id.setting_theme_purple_button);

        blueButton.setOnClickListener(this);
        greenButton.setOnClickListener(this);
        redButton.setOnClickListener(this);
        purpleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        String newThemeValue = (String) ((Button) v).getText();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(v.getResources().getString(R.string.THEME_KEY), newThemeValue);
        editor.commit();
    }

    public View getView() {
        return rootView;
    }
}
