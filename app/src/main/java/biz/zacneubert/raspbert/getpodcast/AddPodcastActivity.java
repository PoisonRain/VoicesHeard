package biz.zacneubert.raspbert.getpodcast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import biz.zacneubert.raspbert.getpodcast.RSS.RssFeed;
import biz.zacneubert.raspbert.getpodcast.RSS.RssItem;
import biz.zacneubert.raspbert.getpodcast.RSS.RssReader;

/**
 * Created by zacneubert on 9/22/15.
 */
public class AddPodcastActivity extends AppCompatActivity implements View.OnClickListener {
    public static EditText NameEdit;
    public static EditText UrlEdit;
    public static EditText LimitEdit;

    public static LinearLayout masterLayout;
    public static LinearLayout UrlLayout;
    public static LinearLayout NameLayout;

    public static ProgressBar waitingForUrlBar;
    public static ProgressBar waitingForNameBar;

    public static Thread CheckUrl;
    public static Handler CheckUrlHandler = new Handler();

    public static Button btnSaveNewPodcast;

    public static ImageButton btnGoRssSearch;

    public static PodcastApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addpodcast_activity);

        application = (PodcastApplication) getApplication();

        masterLayout = (LinearLayout) findViewById(R.id.addPodcastMasterLayout);
        masterLayout.setBackgroundColor(application.foreColor);

        NameEdit = (EditText) findViewById(R.id.TitleEditView);
        UrlEdit = (EditText) findViewById(R.id.UrlEditView);
        LimitEdit = (EditText) findViewById(R.id.LimitEditView);

        UrlLayout = (LinearLayout) findViewById(R.id.addPodcastUrlLayout);
        NameLayout = (LinearLayout) findViewById(R.id.addPodcastNameLayout);

        waitingForNameBar = (ProgressBar) findViewById(R.id.waitingForNameBar);
        waitingForUrlBar = (ProgressBar) findViewById(R.id.waitingForUrlBar);

        btnSaveNewPodcast = (Button) findViewById(R.id.saveNewPodcastButton);
        btnSaveNewPodcast.setOnClickListener(this);

        btnGoRssSearch = (ImageButton) findViewById(R.id.btnRssSearch);
        btnGoRssSearch.setOnClickListener(this);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        String k = "android.intent.extra.TEXT";
        String urlKey = "url";
        if(b != null && (b.containsKey(k) || b.containsKey(urlKey))) {
            try {
                String v;
                if(b.containsKey(k))
                    v = b.getString(k);
                else
                    v = b.getString(urlKey);
                final URL feedUri = new URL(v);
                waitingForUrlBar = new ProgressBar(this);
                waitingForNameBar = new ProgressBar(this);

                CheckUrl = new Thread() {
                    @Override
                    public void run() {
                        checkUrl(feedUri);
                    }
                };
                CheckUrl.start();
            }
            catch (MalformedURLException e) {
                //can't use this, so just leave
                finish();
            }
        }
    }

    public void checkUrl(final URL url) {
        try {
            RssFeed feed = RssReader.read(url);
            ArrayList<RssItem> items = feed.getRssItems();
            CheckUrlHandler.post(new Runnable() {
                @Override
                public void run() {
                    UrlEdit.setText(url.toString());
                    UrlLayout.removeView(waitingForUrlBar);
                }
            });
            final String title = feed.getTitle();
            CheckUrlHandler.post(new Runnable() {
                @Override
                public void run() {
                    NameEdit.setText(title);
                    NameLayout.removeView(waitingForNameBar);
                }
            });
        }
        catch (Exception e) {
            CheckUrlHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Invalid URL.", Toast.LENGTH_LONG).show();
                }
            });
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if(v.getId() == btnSaveNewPodcast.getId()) {
            String limit = LimitEdit.getText().toString();
            Podcast p = new Podcast(
                    NameEdit.getText().toString(),
                    UrlEdit.getText().toString(),
                    Integer.parseInt(limit)
            );

            List<Podcast> podcastList = Podcast.getPodcasts();
            podcastList.add(p);
            Podcast.savePodcasts(podcastList);
            p.threadedEnqueue(getApplicationContext());
            //final Intent i = new Intent(getApplicationContext(), SummaryActivity.class);
            //startActivity(i);
            finish();
        }
        else if(v.getId() == btnGoRssSearch.getId()) {
            //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.RSSFeedSearchEngine)));
            //startActivity(browserIntent);
            //finish();

            String query = NameEdit.getText().toString();
            Intent SearchResultIntent = new Intent(getApplicationContext(), SearchResultActivity.class);
            SearchResultIntent.putExtra("query", query);
            startActivity(SearchResultIntent);
            finish();
        }
    }
}
