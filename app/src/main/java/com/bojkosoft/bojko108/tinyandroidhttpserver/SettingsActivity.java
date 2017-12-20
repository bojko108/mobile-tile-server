package com.bojkosoft.bojko108.tinyandroidhttpserver;

import android.app.DialogFragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.bojkosoft.bojko108.tinyandroidhttpserver.utils.PromptDialogFragment;

public class SettingsActivity extends PreferenceActivity implements PromptDialogFragment.PromptDialogListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // restore default settings when "Reset" is clicked
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        // close this activity so the settings get updated
        this.finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // do nothing with settings
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
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

            if (preference.getKey().equals(getString(R.string.settings_reset_button))) {
                PromptDialogFragment dialog = new PromptDialogFragment();
                dialog.setContent(getString(R.string.restore_defaults));
                dialog.show(getFragmentManager(), "resetpreferences");
                resultHandled = true;
            }

            return resultHandled;
        }
    }
}
