package biz.zacneubert.raspbert.getpodcast.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import biz.zacneubert.raspbert.getpodcast.R;

/**
 * Created by zacneubert on 11/26/15.
 */
public class Setting_Naming_View implements AdapterView.OnItemSelectedListener {
    Spinner Naming_Spinner;
    View rootView;
    Context c;

    public Setting_Naming_View(LayoutInflater inflater, Context c) {
        rootView = inflater.inflate(R.layout.setting_naming_layout, null, false);
        this.c = c;

        Naming_Spinner = (Spinner) rootView.findViewById(R.id.setting_naming_spinner);
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(c, R.array.NAMING_TYPES, R.layout.simple_spinner_item);
        Naming_Spinner.setAdapter(adapter);
        Naming_Spinner.setOnItemSelectedListener(this);

        Naming_Spinner.setSelection(
                adapter.getPosition(
                        new Setting_Naming().getSavedValue(c)
                )
        );
    }

    public View getView() {
        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rootView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        String nametype = Naming_Spinner.getSelectedItem().toString();
        //sortType = (String) parent.getItemAtPosition(position);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(new Setting_Naming().getKey(), nametype);
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //ALMOST LIKE I'M CODING NOTHING AT ALL (nothing at all, nothing at all)
    }
}
