package biz.zacneubert.raspbert.getpodcast.Settings;

import android.content.Context;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import biz.zacneubert.raspbert.getpodcast.AlarmActivity;
import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Created by zacneubert on 11/23/15.
 */
public class Setting_Alarm_View implements View.OnClickListener {
    Button button;

    View rootView;

    Context c;

    public Setting_Alarm_View(LayoutInflater inflater, Context c) {
        rootView = inflater.inflate(R.layout.setting_alarm_layout, null, false);

        this.c = c;

        button = (Button) rootView.findViewById(R.id.setting_alarm_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        final Intent i = new Intent(v.getContext(), AlarmActivity.class);
        v.getContext().startActivity(i);
    }

    public View getView() {
        return rootView;
    }
}
