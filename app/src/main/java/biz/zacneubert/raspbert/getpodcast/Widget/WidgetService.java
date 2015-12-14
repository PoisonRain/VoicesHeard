package biz.zacneubert.raspbert.getpodcast.Widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Created by zacneubert on 11/6/15.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetListFactory(this.getApplicationContext(), intent);
    }

    class WidgetListFactory implements RemoteViewsService.RemoteViewsFactory {
        Intent intent;
        Context context;
        List<String> paths;

        public WidgetListFactory(Context c, Intent i) {
            this.intent = i;
            this.context = c;

            paths = intent.getStringArrayListExtra("paths");
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rviews = new RemoteViews(context.getPackageName(), R.layout.widget_button);

            String path = paths.get(position);
            String[] splitPath = path.split("/");
            String name = splitPath[splitPath.length-1];
            rviews.setTextViewText(R.id.widget_button_button, name);

            Intent playOnClick = new Intent(context, WidgetActivity.class);
            playOnClick.setAction(EpisodeWidget.ACTION_PLAY);
            playOnClick.putExtra(EpisodeWidget.FILE_PATH_CONST, path);
            rviews.setOnClickFillInIntent(R.id.widget_button_button, playOnClick);

            return rviews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(context.getPackageName(), R.id.widget_button_button);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
