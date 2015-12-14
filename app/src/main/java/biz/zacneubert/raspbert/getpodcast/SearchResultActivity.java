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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zacneubert on 10/10/15.
 */
public class SearchResultActivity extends AppCompatActivity {
    public static ProgressBar loadingBar;
    public static ListView resultListView;
    public static LinearLayout rootLayout;
    public static Handler searchResultHandler = new Handler();

    public static PodcastApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchresultslayout);

        Intent i = getIntent();
        String query = i.getStringExtra("query");

        application = (PodcastApplication) getApplication();

        rootLayout = (LinearLayout) this.findViewById(R.id.rootLayout);
        loadingBar = (ProgressBar) this.findViewById(R.id.SearchProgressBar);
        resultListView = (ListView) this.findViewById(R.id.searchResultListView);
        resultListView.setBackgroundColor(application.foreColor);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Log.i("resultList|ItemClick", view.toString());
                Log.i("resultList|ItemClick", position + "");
                Log.i("resultList|ItemClick", id + "");
                Log.i("resultList|ItemClick", parent.toString());
                finish();
            }
        });

        initializeResultListView(query, this);
    }

    final String baseURL =
            "https://ajax.googleapis.com/ajax/services/feed/find?" +
                    "v=1.0&q={{QUERY}}&userip=INSERT-USER-IP";
    public String buildURL(String query) {
        if(!query.contains("odcast")) query += " podcast";
        String escapedQuery=query.replace(" ", "%20");
        String finalURL = baseURL;
        finalURL = finalURL.replace("{{QUERY}}", escapedQuery);
        return finalURL;
    }

    public void doSearchQuery(final String query, final Context c) {
        try {
            Thread queryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(query);
                        URLConnection connection = url.openConnection();
                        connection.addRequestProperty("Referer", "raspbert.no-ip.biz");

                        String line;
                        StringBuilder builder = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while((line = reader.readLine()) != null) {
                            builder.append(line);
                        }

                        final JSONObject json = new JSONObject(builder.toString());
                        boolean post = searchResultHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject responseData = json.getJSONObject("responseData");
                                    JSONArray arr = responseData.getJSONArray("entries");
                                    List<JSONObject> results = new ArrayList<JSONObject>();
                                    for(int i=0; i<arr.length(); i++) {
                                        results.add(arr.getJSONObject(i));
                                        //Log.i("Podcast|JSON", result.title);
                                    }
                                    resultListView.setAdapter(new SearchResultListAdapter(c, R.layout.individualsearchresultview, results));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                finally {
                                    rootLayout.removeView(loadingBar);
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            queryThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeResultListView(String query, Context c) {
        doSearchQuery(buildURL(query), c);
    }

    public class SearchResultListAdapter extends ArrayAdapter<JSONObject> {
        public SearchResultListAdapter(Context context, int resource) {
            super(context, resource);
        }

        public SearchResultListAdapter(Context context, int resource, int textViewResourceId, JSONObject[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public SearchResultListAdapter(Context context, int resource, List<JSONObject> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.individualsearchresultview, parent, false);
            }

            JSONObject json = getItem(position);
            String title="",url="",description="";
            try {
                title = json.getString("title");
                url = json.getString("url");
                description = json.getString("contentSnippet");
            } catch (JSONException e) {
                if(title.equals("")) title = "Could not process title";
                if(description.equals("")) title = "Could not process description";
                if(url.equals("")) url = "Could not process title";
            }

            TextView titleView = (TextView) convertView.findViewById(R.id.searchTitle);
            titleView.setText(Html.fromHtml(title));
            titleView.setTextSize(24);
            titleView.setTextColor(Color.WHITE);

            TextView urlView = (TextView) convertView.findViewById(R.id.searchURL);
            urlView.setText(url);
            urlView.setTextColor(Color.BLACK);
            urlView.setBackgroundColor(getResources().getColor(R.color.ltGrey));

            TextView descriptionView = (TextView) convertView.findViewById(R.id.searchDescription);
            descriptionView.setText(Html.fromHtml(description));
            descriptionView.setTextColor(Color.WHITE);

            Button addButton = (Button) convertView.findViewById(R.id.searchAddButton);
            addButton.setText("Add " + Html.fromHtml(title));
            final String finalUrl = url;
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    Log.i("SearchResult|add", "addButton clicked");
                    Intent i = new Intent(getContext(), AddPodcastActivity.class);
                    i.putExtra("url", finalUrl);
                    v.getContext().startActivity(i);
                }
            });

            Button webButton = (Button) convertView.findViewById(R.id.searchBrowseButton);
            webButton.setText("Open in Browser");
            final String finalUrl1 = url;
            webButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    Log.i("SearchResult|web", "webButton clicked");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl1));
                    v.getContext().startActivity(browserIntent);
                }
            });


            return convertView;
        }
    }
}
