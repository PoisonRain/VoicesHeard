package biz.zacneubert.raspbert.getpodcast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivity;
import static android.support.v4.content.ContextCompat.getColor;

/**
 * Created by zacneubert on 9/18/15.
 */
public class PodcastView extends LinearLayout implements View.OnClickListener, View.OnLongClickListener, TextWatcher {

    Podcast podcast;
    Context context;

    //public static int clrLtBlue = Color.rgb(30, 60, 100);
    //public static int clrDkBlue = Color.rgb(30, 40, 70);
    //public static int clrDkDkBlue = Color.rgb(20, 25, 40);
    public static int clrGold = -1;

    public GradientDrawable grGold;
    public GradientDrawable grBlue;
    public GradientDrawable grDkGray;

    public PodcastApplication application;

    private static Activity parentActivity;
    public static void setParentActivity(Activity a) {
        parentActivity = a;
    }

    public PodcastView(Podcast p, Context c, PodcastApplication pa) {
        super(c);
        podcast = p;
        context = c;
        application = pa;

        if(clrGold < 0) clrGold = context.getColor(R.color.gold);

        grGold = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {application.highlightColor, application.highlightColor});
        grBlue = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {application.foreColor, application.foreColor});
        grDkGray = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {Color.DKGRAY, Color.DKGRAY});

        grGold.setCornerRadius(10);
        grGold.setStroke(1, Color.BLACK);

        grBlue.setCornerRadius(10);
        grBlue.setStroke(1, Color.BLACK);

        grDkGray.setCornerRadius(10);
        grDkGray.setStroke(1, Color.BLACK);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(application.foreColor);// Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(15);
        gd.setStroke(3, 0);

        if(Podcast.isHighlighted(podcast)) {
            this.setBackground(grGold);
        }
        else {
            this.setBackground(gd);
        }

        this.setPadding(15, 15, 15, 15);
        this.setOrientation(LinearLayout.VERTICAL);
        this.addView(titleView());

        //this.setBackgroundColor(Color.DKGRAY);
        View countUI = podcastCountEditView();
        this.addView(countUI);
        //countUI.getLayoutParams().height = btnHeight;

        for(View episodeView : episodeViews()) this.addView(episodeView);
        this.addView(downloadButtonView());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        toggleMinimized();
    }

    public boolean isMinimized = false;
    public boolean toggleMinimized() {
        int vis;
        /*  //OLD CODE THAT ONLY DESTROYS AND RECREATES
        if(!isMinimized)
            vis = View.GONE;
        else
            vis = View.VISIBLE;

        for(View episode : episodes) {
            episode.setVisibility(vis);
        }
        btn.setVisibility(vis);
        countUI.setVisibility(vis);
        */
        //NEW CODE THAT RADIATES LIFE AND WARMTH
        if(!isMinimized) {
            for (View episode : episodes) {
                collapse(episode);
            }
            collapse(btn);
            collapse(countUI);
            rotateUp(ArrowView);
        }
        else if (isMinimized) {
            for (View episode : episodes) {
                expand(episode);
            }
            expand(btn);
            expand(countUI);
            rotateDown(ArrowView);
        }
        isMinimized = !isMinimized;
        return isMinimized;
    }

    public static void expand(final View v) {
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (4 * targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (4 * initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void rotateUp(final View v) {
        final int startingRotation = 180;
        final int targetRotation = 0;

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.setRotation((int)(startingRotation * (1-interpolatedTime)));
            }

            @Override
            public boolean willChangeBounds() {
                return false;
            }
        };

        // 1dp/ms
        a.setDuration((int) (4 * startingRotation / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void rotateDown(final View v) {
        final int startingRotation = 0;
        final int targetRotation = 180;

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.setRotation((int) (targetRotation * (interpolatedTime)));
            }

            @Override
            public boolean willChangeBounds() {
                return false;
            }
        };

        // 1dp/ms
        a.setDuration((int) (4 * targetRotation / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public RelativeLayout titleLayout;
    public TextView titleView;
    private View titleView() {
        titleLayout = new RelativeLayout(context);
        titleView = new TextView(context);
        titleView.setText(podcast.Name);
        titleView.setTypeface(Typeface.SERIF);
        float f = titleView.getTextSize();
        titleView.setTextSize(23.0f);
        float g = titleView.getTextSize();
        titleView.setTextColor(Color.LTGRAY);
        titleLayout.setOnLongClickListener(this);
        titleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                toggleMinimized();
            }
        });
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        titleLayout.addView(titleView, titleParams);
        RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        titleLayout.addView(arrowView(), arrowParams);
        return titleLayout;
    }

    public ImageView ArrowView;
    private ImageView arrowView() {
        ArrowView = new ImageView(context);
        ArrowView.setImageResource(R.drawable.arrow);
        ArrowView.setAdjustViewBounds(true);
        ArrowView.setMaxHeight(85);
        ArrowView.setRotation(180);
        return ArrowView;
    }

    public List<View> episodes;
    public static GradientDrawable episodeGradient;
    private List<View> episodeViews() {
        episodeGradient = new GradientDrawable();
        episodeGradient.setColor(application.washColor); // Changes this drawbale to use a single color instead of a gradient
        episodeGradient.setCornerRadius(10);
        episodeGradient.setStroke(1, Color.BLACK);
        episodes = new ArrayList<View>();
        for(File f : podcast.ExistingEpisodes()) {
            Button t = new Button(context);
            t.setText(f.getName());
            t.setTag(f.getAbsolutePath());
            t.setBackground(episodeGradient);
            t.setPadding(3, 3, 3, 3);
            t.setAllCaps(false);
            t.setOnClickListener(this);
            episodes.add(t);
        }
        return episodes;
    }

    public Button btn;
    public static GradientDrawable grDownloadButton;
    private View downloadButtonView() {
        grDownloadButton = new GradientDrawable();
        grDownloadButton.setColor(application.darkColor);
        grDownloadButton.setCornerRadius(7);
        grDownloadButton.setStroke(1, Color.BLACK);
        btn = new Button(context);
        btn.setText("Download " + podcast.Name);
        btn.setOnClickListener(this);
        btn.setBackground(grDownloadButton);
        btn.setTextColor(Color.LTGRAY);
        btn.setTag(btn.getText());
        return btn;
    }

    public LinearLayout countUI;
    public ImageButton btnGetArchive;
    public ImageButton btnDeletePodcast;
    static LinearLayout.LayoutParams deletebtnLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    static float btnSize = 15f;
    static int btnHeight = 150;
    static int btnWidth = 150;
    private View podcastCountEditView() {
        deletebtnLayoutParams.leftMargin = 50;
        deletebtnLayoutParams.rightMargin = 50;

        countUI = new LinearLayout(context);
        countUI.setOrientation(LinearLayout.HORIZONTAL);
        if(Podcast.isHighlighted(podcast)) {
            countUI.setBackgroundColor(getColor(context, R.color.gold));
        }
        else {
            countUI.setBackgroundColor(application.foreColor);
        }
        countUI.setGravity(Gravity.LEFT);
        //countUI.setPadding(0, 0, 200, 0);
        final TextView countEdit = new TextView(context);
        countEdit.setText(podcast.Limit + "");
        countEdit.addTextChangedListener(this);
        countEdit.setBackgroundColor(Podcast.isHighlighted(podcast) ? clrGold : application.foreColor);
        countEdit.setTextColor(Color.LTGRAY);
        countEdit.setPadding(30, 15, 30, 15);
        countEdit.setGravity(Gravity.CENTER);
        View.OnClickListener onbtn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if(v.getTag().equals("-")) {
                    podcast.Limit--;
                }
                else {
                    podcast.Limit++;
                }
                countEdit.setText(podcast.Limit + "");
                Podcast.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        SummaryActivity.savePodcasts();
                    }
                });
                //podcast.makeToast("Saved Data.");
            }
        };
        Button btnMinus = new Button(context);

        countEdit.setTextSize(btnSize);
        btnMinus.setTag("-");
        btnMinus.setText("-");
        btnMinus.setTextSize(btnSize);
        btnMinus.setBackground(grDkGray);
        btnMinus.setOnClickListener(onbtn);
        btnMinus.setHapticFeedbackEnabled(true);
        btnMinus.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        btnMinus.setGravity(Gravity.CENTER);
        Button btnPlus = new Button(context);
        btnPlus.setTag("+");
        btnPlus.setText("+");
        btnPlus.setTextSize(btnSize);
        btnPlus.setBackground(grDkGray);
        btnPlus.setHapticFeedbackEnabled(true);
        btnPlus.setOnClickListener(onbtn);
        btnPlus.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        btnPlus.setGravity(Gravity.CENTER);

        btnDeletePodcast = new ImageButton(context);
        btnDeletePodcast.setBackground(grDkGray);
        btnDeletePodcast.setImageResource(R.drawable.trash);
        btnDeletePodcast.setScaleType(ImageView.ScaleType.FIT_XY);
        btnDeletePodcast.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                deletePodcast(podcast);
            }
        });
        btnDeletePodcast.setLayoutParams(deletebtnLayoutParams);

        btnGetArchive = new ImageButton(context);
        btnGetArchive.setBackground(grDkGray);
        btnGetArchive.setImageResource(R.drawable.archive);
        btnGetArchive.setScaleType(ImageView.ScaleType.FIT_XY);
        btnGetArchive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                changeToArchiveLayout(podcast);
            }
        });
        btnGetArchive.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        countUI.addView(btnMinus);
        countUI.addView(countEdit);
        countUI.addView(btnPlus);
        countUI.addView(btnDeletePodcast);
        countUI.addView(btnGetArchive);

        countEdit.getLayoutParams().width = btnWidth;
        btnPlus.getLayoutParams().width = btnWidth;
        btnMinus.getLayoutParams().width = btnWidth;
        btnDeletePodcast.getLayoutParams().width = btnWidth;
        btnGetArchive.getLayoutParams().width = btnWidth;

        return countUI;
    }

    public void deletePodcast(final Podcast pod) {
        final PodcastView pv = this;
        final String question = "Are you sure you want to delete " + pod.Name + "?";
        new AlertDialog.Builder(context)
                .setMessage(question)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        pod.delete();
                        PodcastApplication._allPodcasts.remove(pod);
                        Podcast.savePodcasts(PodcastApplication._allPodcasts);
                        PodcastApplication._allPodcasts = null;
                        SummaryActivity.secondaryLayout.removeView(pv);
                        Podcast.makeToast("Deleted.");
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void changeToArchiveLayout(final Podcast podcast) {
        //WILL EVENTUALLY PULL UP AN ARCHIVE VIEW FOR podcast
        final Intent i = new Intent(context, ArchiveActivity.class);
        i.putExtra("podcastName", podcast.Name);
        i.putExtra("podcastUrl", podcast.Url);
        i.putExtra("podcastLimit", podcast.Limit);

        podcast.handler.post(new Runnable() {
            @Override
            public void run() {
                podcast.summaryActivity.startActivity(i);
            }
        });
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        final View V=v;
        if(v.getTag().toString().startsWith("Download")) {
            podcast.threadedEnqueue(getContext());
            podcast.priority++;
        }
        else {
            podcast.priority++;
            podcast.handler.post(new Runnable() {
                @Override
                public void run() {
                    String location = (String) V.getTag();
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(location);
                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                    startActivity(parentActivity, intent, Bundle.EMPTY);
                }
            });
        }
        //worker handles actual downloads
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            int limit = Integer.parseInt(s.toString());
            podcast.Limit = limit;
        }
        catch (Exception e) {}
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onLongClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        podcast.playNewestEpisode();
        return true;
    }
}
