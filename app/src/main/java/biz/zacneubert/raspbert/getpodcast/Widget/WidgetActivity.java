package biz.zacneubert.raspbert.getpodcast.Widget;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by zacneubert on 11/7/15.
 */
public class WidgetActivity extends Activity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        String path=getIntent().getStringExtra(EpisodeWidget.FILE_PATH_CONST);
        if (path==null) {
            finish();
        }
        Toast.makeText(this, path, Toast.LENGTH_LONG).show();

        finish();
    }

}
