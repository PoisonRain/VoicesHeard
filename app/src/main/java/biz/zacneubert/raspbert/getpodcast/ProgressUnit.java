package biz.zacneubert.raspbert.getpodcast;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by zacneubert on 9/20/15.
 */
public class ProgressUnit extends LinearLayout {
    public ProgressBar progressBar;
    public TextView textView;
    public String message;
    public int max = 0;
    public static GradientDrawable grDkGray = new GradientDrawable();

    public ProgressUnit(Context c) {
        super(c);
        textView = new TextView(c);
        textView.setTextColor(Color.LTGRAY);
        textView.setPadding(15, 15, 15, 15);
        progressBar = new ProgressBar(c,  null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(false);
        progressBar.setPadding(15, 15, 15, 15);

        this.setOrientation(LinearLayout.VERTICAL);
        grDkGray.setColor(SummaryActivity.clrDkGray);
        grDkGray.setCornerRadius(20);
        grDkGray.setStroke(15, SummaryActivity.clrDkDkDkBlue);
        this.setBackground(grDkGray);
        this.setPadding(15, 15, 15, 15);

        this.addView(textView);
        this.addView(progressBar);
        this.setVisibility(GONE);
    }

    public void setText(String s) {
        textView.setText(s);
    }


    public void setInitialProgress(int max, String message) {
        this.message = message;
        setText(message + " - 0%");
        progressBar.setMax(max);
        this.max = max;
        progressBar.setProgress(0);
        //this.setBackground(SummaryActivity.grDkGray);
        this.setVisibility(VISIBLE);
    }

    public double getPercent(int cur, int max) {
        cur/=(1024);
        max/=(1024);
        double p = Math.floor((((new Double(cur))/(new Double(max)))*1000d))/10d;
        return p;
    }
    public void setProgress(int i) {
        double percent = getPercent(i, progressBar.getMax());
        if(max > 0)
            setText(message + " " + percent + "%");
        else {
            setText(message + " Bad response - cannot determine progress.");
        }
        progressBar.setProgress(i);
    }

    public void setGone() {
        this.setVisibility(GONE);
    }

    public void finishProgress(String Message) {
        progressBar.setProgress(progressBar.getMax());
        textView.setText(Message);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.setVisibility(GONE);
    }
}
