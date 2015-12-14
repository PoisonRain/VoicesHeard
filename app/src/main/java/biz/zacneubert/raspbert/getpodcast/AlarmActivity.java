package biz.zacneubert.raspbert.getpodcast;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import biz.zacneubert.raspbert.getpodcast.AutoDownload.AutoDownloadControl;

/**
 * Created by zacneubert on 9/22/15.
 */
public class AlarmActivity extends AppCompatActivity {
    public static TimePicker autoDownloadTimePicker;
    public static Button btnAutoDownloadSave;

    public static int autoHour;
    public static int autoMinute;

    public static AutoDownloadControl downloadControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        autoDownloadTimePicker = (TimePicker) findViewById(R.id.timePicker);
        btnAutoDownloadSave = (Button) findViewById(R.id.btnAutoDownloadSave);
        btnAutoDownloadSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                setAutoDownloadTime();
                finish();
            }
        });

        Button cancel = (Button) findViewById(R.id.btnCancelOld);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadControl != null) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    downloadControl.cancel();
                }
                finish();
            }
        });
    }

    public void setAutoDownloadTime() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            autoHour = autoDownloadTimePicker.getHour();
            autoMinute = autoDownloadTimePicker.getMinute();
        } else {
            autoHour = autoDownloadTimePicker.getCurrentHour();
            autoMinute = autoDownloadTimePicker.getCurrentMinute();
        }
        downloadControl = new AutoDownloadControl();
        downloadControl.setAlarm(autoHour, autoMinute, getApplicationContext());
        //Toast.makeText(getApplicationContext(), "Setting download time to " + autoHour + ":" + autoMinute, Toast.LENGTH_LONG).show();
    }
}
