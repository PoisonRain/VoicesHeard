package biz.zacneubert.raspbert.getpodcast;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biz.zacneubert.raspbert.getpodcast.RSS.RssEnclosure;
import biz.zacneubert.raspbert.getpodcast.RSS.RssFeed;
import biz.zacneubert.raspbert.getpodcast.RSS.RssItem;
import biz.zacneubert.raspbert.getpodcast.RSS.RssReader;
import biz.zacneubert.raspbert.getpodcast.Settings.Setting_Naming;

/**
 * Created by zacneubert on 9/21/15.
 */
public class ArchiveActivity extends AppCompatActivity implements View.OnClickListener {
    public TextView titleView;
    public LinearLayout masterLayout;
    public LinearLayout archiveLayout;
    public Podcast podcast;
    public static Handler archiveLoadHandler;

    public PodcastApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive_activity);

        application = (PodcastApplication) getApplication();

        titleView = (TextView) findViewById(R.id.archiveTitleText);
        titleView.setBackgroundColor(application.foreColor);
        masterLayout = (LinearLayout) findViewById(R.id.archiveMaster);
        masterLayout.setBackgroundColor(application.foreColor);
        archiveLayout = (LinearLayout) findViewById(R.id.archiveEpisodesLayout);
        archiveLayout.setBackgroundColor(application.foreColor);

        Intent i = getIntent();
        String podName = i.getStringExtra("podcastName");
        String podUrl = i.getStringExtra("podcastUrl");

        podcast = new Podcast(podName, podUrl, 0);

        titleView.setText(podcast.Name);

        archiveLoadHandler = new Handler();
        Thread addArchiveThread = new Thread() {
            public void run() {
                getArchives();
            }
        };
        addArchiveThread.start();
    }

    public void getArchives() {
        try {
            RssFeed feed = RssReader.read(new URL(podcast.Url));
            List<RssItem> entries = feed.getRssItems();
            Log.i("RSS Reader", feed.getTitle());
            Log.i("RSS Reader", feed.getLink());
            Log.i("RSS Reader", feed.getDescription());
            List<View> archiveViewList = new ArrayList<View>();
            for(RssItem entry : entries) {
                final View archView = new ArchiveEpisodeView(entry, getApplicationContext());
                archiveLoadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        archiveLayout.addView(archView);
                    }
                });
            }
        } catch (Exception exp) {
            Log.e("Podcast", exp.getMessage(), exp);
        }
    }

    public class ArchiveEpisodeView extends LinearLayout {
        public TextView titleView;
        public TextView dateView;
        public Button btnPlay;
        public ImageButton btnStar;
        public Date publishDate;
        public String Link;
        public TextView descriptionView;

        public LinearLayout btnLayout;

        String filename;

        public PodcastApplication application;

        public ArchiveEpisodeView(final RssItem item, final Context context) {
            super(context);

            this.setOrientation(LinearLayout.VERTICAL);

            titleView = new TextView(context);
            dateView = new TextView(context);
            btnPlay = new Button(context);
            btnStar = new ImageButton(context);
            descriptionView = new TextView(context);

            btnLayout = new LinearLayout(context);

            application = (PodcastApplication) getApplication();
            this.setBackgroundColor(application.foreColor);

            publishDate = item.getPubDate();

            Log.i("RSS Reader", "XXXXX\tContent: " + item.getContent());
            Log.i("RSS Reader", "XXXXX\tLink: " + item.getLink());
            Log.i("RSS Reader", "XXXXX\tEnclosures: ");
            for(RssEnclosure enc : item.getEnclosures()) {
                Log.i("RSS Reader", "     \t" + enc.toString());
                String encType = enc.getType();
                String encUrl = enc.getValue();
                String qname = enc.getQName();
                if(qname.equals("url")) {
                    Link = encUrl;
                }
            }

            titleView.setText(item.getTitle());
            titleView.setTextSize(25f);
            dateView.setText(publishDate.toString());
            dateView.setBackgroundColor(Color.rgb(99, 99, 99));
            dateView.setTextColor(Color.rgb(33, 33, 33));
            if(item.getDescription() != null)
                descriptionView.setText(Html.fromHtml(item.getDescription()));

            btnLayout.setOrientation(HORIZONTAL);

            btnPlay.setText("Play " + item.getTitle());
            btnPlay.setTransformationMethod(null);
            btnPlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    playArchive();
                }
            });
            btnPlay.setMaxWidth(850);

            filename = Podcast.getFileName(Link);
            String extension = Podcast.getFileExtension(Link);
            String NamingType = Setting_Naming.getSavedValue(context);
            String titleName = item.getTitle() + extension;
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
            String datename = sdf.format(item.getPubDate()) + extension;

            if(PodcastApplication.isStarred(filename)) {
                btnStar.setImageResource(R.drawable.starfullsmall);
            }
            else {
                btnStar.setImageResource(R.drawable.staremptysmall);
            }
            btnStar.setMaxWidth(60);
            btnStar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    Episode e = new Episode(item.getTitle(), filename, Link, podcast, item.getPubDate());
                    if (PodcastApplication.toggleStarredEpisode(filename)) {
                        btnStar.setImageResource(R.drawable.starfullsmall);
                        if (!Podcast.EpisodeQueue.contains(e)) {
                            Podcast.EpisodeQueue.add(e);
                        }
                    } else {
                        btnStar.setImageResource(R.drawable.staremptysmall);
                        e.delete(context);
                    }
                }
            });

            this.addView(titleView);
            this.addView(dateView);
            this.addView(descriptionView);
            btnLayout.addView(btnPlay);
            btnLayout.addView(btnStar);
            this.addView(btnLayout);
        }

        public void playArchive() {
            String filename = Podcast.getFileName(Link);
            Log.i("Podcast|Archive", "File name: " + filename);
            File localFile = null;
            for(File f : podcast.getLocalEpisodeFiles()) {
                Log.i("Podcast|Archive", "\tLocal file: "+f.getName());
                if(f.getName().equals(filename)) {
                    localFile = f;
                    break;
                }
            }

            Uri myUri;
            if(localFile == null) {
                myUri = Uri.parse(Link);
                Log.i("Podcast|Archive", "Using archive Link");
            }
            else {
                myUri = Uri.fromFile(localFile);
                Log.i("Podcast|Archive", "Using Local file");
            }
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(myUri, "audio/*");
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
