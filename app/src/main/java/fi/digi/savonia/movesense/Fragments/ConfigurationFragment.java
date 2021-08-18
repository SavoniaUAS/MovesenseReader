package fi.digi.savonia.movesense.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.prefs.Preferences;

import androidx.fragment.app.Fragment;
import fi.digi.savonia.movesense.R;
import fi.digi.savonia.movesense.Tools.OnSingleClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ConfigurationFragment extends Fragment implements TextWatcher {

    private OnFragmentInteractionListener mListener;

    private EditText sendInterval;
    private EditText measurementNote;
    private EditText objectName;
    private EditText samiWritekey;
    private Button confirmSami;

    private final String writekey_key = "sami_writekey";
    private final String object_key = "sami_object";
    private final String note_key = "sami_note";
    private final String interval_key = "sami_interval";
    private final String prefs_name = "MyPrefs";

    public ConfigurationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);

        InitViews(view);

        return view;
    }

    private void InitViews(View view)
    {
        confirmSami = view.findViewById(R.id.configuration_choose_button);
        samiWritekey = view.findViewById(R.id.configuration_sami_writekey);
        objectName = view.findViewById(R.id.configuration_sami_object);
        measurementNote = view.findViewById(R.id.configuration_sami_note);
        sendInterval = view.findViewById(R.id.configuration_interval);

        confirmSami.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                SaveSelectedValues();
                mListener.onConfigurationConfirm(Long.valueOf(sendInterval.getText().toString())*1000,samiWritekey.getText().toString(),objectName.getText().toString(),measurementNote.getText().toString());
            }
        });
        confirmSami.setEnabled(false);

        samiWritekey.addTextChangedListener(this);
        objectName.addTextChangedListener(this);
        measurementNote.addTextChangedListener(this);
        sendInterval.addTextChangedListener(this);

        LoadSavedValues();

    }

    private void CheckRequirements()
    {
        if(!samiWritekey.getText().toString().equals("") && !objectName.getText().toString().equals("") && !measurementNote.getText().toString().equals("") && !sendInterval.getText().toString().equals(""))
        {
            confirmSami.setEnabled(true);
        }
        else
        {
            confirmSami.setEnabled(false);
        }
    }

    private void LoadSavedValues()
    {
        SharedPreferences settings = getContext().getSharedPreferences(prefs_name,Context.MODE_PRIVATE);
        sendInterval.setText(String.valueOf(settings.getInt(interval_key,120)));
        measurementNote.setText(settings.getString(note_key,""));
        objectName.setText(settings.getString(object_key,""));
        samiWritekey.setText(settings.getString(writekey_key,""));

    }

    private boolean SaveSelectedValues()
    {
        SharedPreferences settings = getContext().getSharedPreferences(prefs_name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(interval_key,Integer.valueOf(sendInterval.getText().toString()));
        editor.putString(note_key,measurementNote.getText().toString());
        editor.putString(object_key,objectName.getText().toString());
        editor.putString(writekey_key,samiWritekey.getText().toString());
        return editor.commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        CheckRequirements();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onConfigurationConfirm(long intervalMS, String writekey, String object, String note);
    }
}