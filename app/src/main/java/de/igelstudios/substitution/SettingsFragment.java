package de.igelstudios.substitution;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference button = findPreference("load_table");
        button.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                MainActivity.getInstance().COURSES.fetchAndAdd();
                return true;
            }
        });

        Preference updateBTN = findPreference("update");
        updateBTN.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                MainActivity.getInstance().UPDATER.updateForce();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity.getInstance().settings = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        MainActivity.getInstance().NOTIFIER.notifieChanges(MainActivity.getInstance().FETCHER.fetch());
    }
}
