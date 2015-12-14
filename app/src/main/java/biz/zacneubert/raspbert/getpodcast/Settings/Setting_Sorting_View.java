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
public class Setting_Sorting_View implements AdapterView.OnItemSelectedListener {
    Spinner Sorting_Spinner;
    View rootView;
    Context c;

    public Setting_Sorting_View(LayoutInflater inflater, Context c) {
        rootView = inflater.inflate(R.layout.setting_sorting_layout, null, false);
        this.c = c;

        Sorting_Spinner = (Spinner) rootView.findViewById(R.id.setting_sorting_spinner);
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(c, R.array.SORTING_TYPES, R.layout.simple_spinner_item);
        Sorting_Spinner.setAdapter(adapter);
        Sorting_Spinner.setOnItemSelectedListener(this);

        Sorting_Spinner.setSelection(
                adapter.getPosition(
                        new Setting_Sorting().getSavedValue(c)
                )
        );
    }

    public View getView() {
        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rootView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        String sortType = Sorting_Spinner.getSelectedItem().toString();
        //sortType = (String) parent.getItemAtPosition(position);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(new Setting_Sorting().getKey(), sortType);
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //ALMOST LIKE I'M CODING NOTHING AT ALL (nothing at all, nothing at all)
    }
}
