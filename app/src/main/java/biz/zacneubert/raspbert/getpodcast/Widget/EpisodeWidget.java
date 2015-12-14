package biz.zacneubert.raspbert.getpodcast.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.app.PendingIntent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.zacneubert.raspbert.getpodcast.Episode;
import biz.zacneubert.raspbert.getpodcast.Podcast;
import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link EpisodeWidgetConfigureActivity EpisodeWidgetConfigureActivity}
 */
public class EpisodeWidget extends AppWidgetProvider {

    static List<Episode> episodes;
    static List<Podcast> podcasts;

    static final String ACTION_PLAY = "PlayFile";
    static final String FILE_PATH_CONST = "filepath";

    // Called when the BroadcastReceiver receives an Intent broadcast.
    // Checks to see whether the intent's action is TOAST_ACTION. If it is, the app widget
    // displays a Toast message for the current item.
    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        super.onReceive(context, intent);
    }



    public static void fetchTopTen() {
        podcasts = Podcast.getPodcasts();
        if(episodes == null) {
            episodes = new ArrayList<>();
        }
        else {
            episodes.clear();
        }
        for(Podcast p : podcasts) {
            for(File f : p.getLocalEpisodeFiles()) {
                if(!episodes.contains(f)) episodes.add(new Episode(f));
            }
        }

        Collections.sort(episodes);
        episodes = episodes.subList(0,10);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            EpisodeWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static int iid = 6; //probably
    static int intentID() {
        return iid++;
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = EpisodeWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.episode_widget);
        //views.setTextViewText(R.id.widRootLayout, widgetText);

        fetchTopTen();

        ArrayList<String> paths = new ArrayList<>();
        for(Episode e : episodes) {
            paths.add(e.file.getAbsolutePath());
        }

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("random", intentID());
        intent.putStringArrayListExtra("paths", paths);
        views.setRemoteAdapter(R.id.widEpisodeListView, intent);

        Intent playIntent = new Intent(context, WidgetActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widEpisodeListView, pi);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

