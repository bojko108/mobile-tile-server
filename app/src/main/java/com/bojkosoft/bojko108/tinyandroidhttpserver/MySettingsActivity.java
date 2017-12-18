package com.bojkosoft.bojko108.tinyandroidhttpserver;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class MySettingsActivity extends PreferenceActivity implements PromptDialogFragment.PromptDialogListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // restore default preferences
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        this.finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // do nothing with preferences
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private static final int DIRECTORY_SELECT_CODE = 108;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            findPreference(getString(R.string.settings_reset_button)).setOnPreferenceClickListener(this);
            findPreference(getString(R.string.rootpath)).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {

            boolean resultHandled = false;

            if (preference.getKey() == getString(R.string.rootpath)) {
                /*

                 NOT IMPLEMENTED YET

                try {
                    Intent intent = new Intent(this.getContext(), ExplorerActivity.class);
                    this.startActivityForResult(intent, DIRECTORY_SELECT_CODE);
                    // we will handle the result in this.onActivityResut()
                    resultHandled = true;
                } catch (ActivityNotFoundException ex) {
                    // no activity for selecting a directory, value will be set manually
                    ex.printStackTrace();
                    resultHandled = false;
                }
                */
            }
            if (preference.getKey() == getString(R.string.settings_reset_button)) {
                PromptDialogFragment dialog = new PromptDialogFragment();
                dialog.setContent(getString(R.string.restore_defaults));
                dialog.show(getFragmentManager(), "resetpreferences");
                resultHandled = true;
            }

            return resultHandled;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            // Don't forget to check requestCode before continuing your job
            if (requestCode == DIRECTORY_SELECT_CODE) {
                // Do your job
                if (data.hasExtra("DIRECTORY_SELECTED")) {
                    String newPath = data.getStringExtra(ExplorerActivity.DIRECTORY);
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();
                    prefs.putString(getString(R.string.rootpath), newPath);
                    prefs.commit();
                }
            }
        }
    }
}
