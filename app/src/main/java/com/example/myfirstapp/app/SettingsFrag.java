/**
 * Created by tommyhu on 8/4/2014.
 */

package com.example.myfirstapp.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFrag extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private OnSettingsInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource

        addPreferencesFromResource(R.xml.fragment_settings);

//        PreferenceScreen screen = (PreferenceScreen)findPreference(getResources().getString(R.string.pref_buttonController_subscreen_key));
//        EditTextPreference pref_button = new EditTextPreference(getActivity());
//        pref_button.setTitle("This is a button");
//        pref_button.setSummary("This is the summary of the button");
//        screen.addPreference(pref_button);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
        setHasOptionsMenu(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSettingsInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSettingsInteractionListener");
        }
    }

    public interface OnSettingsInteractionListener {
        public void onNormalIntervalChanged(int new_interval);
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.pref_sending_frequency_key))) {
            EditTextPreference frequency_pref = (EditTextPreference) findPreference(key);
            // Set summary to be the user-description for the selected value
            frequency_pref.setSummary(sharedPreferences.getString(key, "") + " ms");
            mListener.onNormalIntervalChanged(Integer.valueOf(sharedPreferences.getString(key, "100")));
        }     }



}
