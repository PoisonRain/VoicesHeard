package biz.zacneubert.raspbert.getpodcast.Settings;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Created by zacneubert on 11/27/15.
 */
public class Setting_Root_Folder_View implements View.OnClickListener {
    View rootView;
    EditText folder_edit;
    Button save_button;
    Context c;

    public Setting_Root_Folder_View(LayoutInflater inflater, Context c) {
        rootView = inflater.inflate(R.layout.setting_root_folder_layout, null, false);
        this.c = c;
        folder_edit = (EditText) rootView.findViewById(R.id.setting_root_folder_edit);
        folder_edit.setText(Setting_Root_Folder.getSavedValue(c));
        folder_edit.setOnClickListener(this);
        save_button = (Button) rootView.findViewById(R.id.setting_root_folder_save_button);
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        EditText e = (EditText) v;
        Setting_Root_Folder.setSavedValue(c, e.getText().toString());
    }

    public View getView() {
        return rootView;
    }
}
