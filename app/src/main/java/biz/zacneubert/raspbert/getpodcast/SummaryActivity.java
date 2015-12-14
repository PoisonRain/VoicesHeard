package biz.zacneubert.raspbert.getpodcast;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import biz.zacneubert.raspbert.getpodcast.AutoDownload.AutoDownloadService;
import biz.zacneubert.raspbert.getpodcast.Pebble.PebbleControl;
import biz.zacneubert.raspbert.getpodcast.Settings.Settings_List_Activity;

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener {
    static PodcastApplication application;

    static Button btnDownload;
    static ProgressUnit progressbar;
    static Thread worker;
    static LinearLayout secondaryLayout;
    static Handler progresshandler = new Handler();
    static Button btnAddPodcast;
    static SwipeRefreshLayout refreshLayout;

    public static int clrDkGray = Color.DKGRAY;
    public static int clrDkDkDkBlue = Color.rgb(0,0,15);
    public static GradientDrawable grDkGray = new GradientDrawable();

    public static void savePodcasts() {
        Podcast.savePodcasts(application.allPodcasts());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        PebbleControl.initialize(this, getApplicationContext());

        application = (PodcastApplication) getApplication();

        RelativeLayout masterLayout = (RelativeLayout) findViewById(R.id.masterLayout);
        masterLayout.setBackgroundColor(Color.rgb(0, 0, 15));

        refreshLayout = new SwipeRefreshLayout(this);
        refreshLayout.setOnRefreshListener(this);

        ScrollView scrollView = new ScrollView(this);

        refreshLayout.addView(scrollView);
        masterLayout.addView(refreshLayout);
        scrollView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

        secondaryLayout = new LinearLayout(this);
        secondaryLayout.setOrientation(LinearLayout.VERTICAL);



        scrollView.addView(secondaryLayout);

        grDkGray.setColor(clrDkGray);
        grDkGray.setCornerRadius(20);
        grDkGray.setStroke(15, clrDkDkDkBlue);

        btnDownload = (Button) findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(this);
        btnDownload.setBackground(grDkGray);
        btnDownload.setTextColor(Color.LTGRAY);
        masterLayout.removeView(btnDownload);
        secondaryLayout.addView(btnDownload);
        progressbar = new ProgressUnit(this);

        Podcast.setContext(this);
        Podcast.setHandler(progresshandler);
        Podcast.setProgressUnit(progressbar);
        Podcast.setSummaryActivity(this);
        PodcastView.setParentActivity(this);

        secondaryLayout.addView(progressbar);

        for(Podcast p : application.allPodcasts()) {
            secondaryLayout.addView(
                    new PodcastView(p, this, application)
            );
        }

        btnAddPodcast = (Button) findViewById(R.id.btnAddPodcast);
        btnAddPodcast.setOnClickListener(this);
        btnAddPodcast.setBackground(grDkGray);
        btnAddPodcast.setTextColor(Color.LTGRAY);
        masterLayout.removeView(btnAddPodcast);
        secondaryLayout.addView(btnAddPodcast);

        if(worker == null) {
            worker = new Thread() {
                public void run() {
                    Podcast.ConstantDownload(getApplicationContext());
                }
            };
            worker.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            worker.interrupt();
            worker = null;
        }
        catch (Exception e) {
            //probably not worth chasing
        }
    }

    public static void refreshUI() {
        application.LoadSettings();
        secondaryLayout.removeAllViews();
        secondaryLayout.addView(btnDownload);
        secondaryLayout.addView(progressbar);
        for(Podcast p : application.allPodcasts(true)) { //True retrieves list from source instead of using old list
            secondaryLayout.addView(
                    new PodcastView(p, secondaryLayout.getContext(), application)
            );
        }
        secondaryLayout.addView(btnAddPodcast);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //final Intent i = new Intent(getApplicationContext(), AlarmActivity.class);
            //startActivity(i);
            final Intent i = new Intent(getApplicationContext(), Settings_List_Activity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if(v.getId() == btnDownload.getId()) {
            //Podcast.triggerPodcastSet(allPodcasts(), true);
            //refreshUI();
            Intent i=new Intent(this, AutoDownloadService.class);
            i.putExtra("hasContext", true);
            startService(i);
            Log.i("Alarming", "Started Service?");
        }
        if(v.getId() == btnAddPodcast.getId()) {
            final Intent i = new Intent(getApplicationContext(), AddPodcastActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);
        refreshUI();
        refreshLayout.setRefreshing(false);
    }
}
