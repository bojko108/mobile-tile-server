package com.bojkosoft.bojko108.mobiletileserver;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, EditTextPreference.OnBindEditTextListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            findPreference(getString(R.string.settings_reset_button)).setOnPreferenceClickListener(this);
            findPreference(getString(R.string.rootpath)).setOnPreferenceClickListener(this);

            ((EditTextPreference) getPreferenceManager().findPreference(getResources().getString(R.string.serverport))).setOnBindEditTextListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(getString(R.string.settings_reset_button))) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("Reset preferences")
                        .setMessage(getString(R.string.restore_defaults))
                        .setNegativeButton(getString(R.string.dialog_no), null)
                        .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // restore default settings when "Reset" is clicked
                                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().clear().commit();
                                PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, true);
                                // close this activity so the settings get updated
                                getActivity().finish();
                            }
                        })
                        .create();

                builder.show();
            }

            return true;
        }

        @Override
        public void onBindEditText(@NonNull EditText editText) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }
}
