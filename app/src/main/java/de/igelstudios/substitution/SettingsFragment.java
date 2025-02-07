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
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                MainActivity.getInstance().COURSES.fetchAndAdd();
                return true;
            }
        });

        Preference updateBTN = findPreference("update");
        updateBTN.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
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

        MainActivity.getInstance().FETCHER.fetch(MainActivity.getInstance().NOTIFIER::notifieChanges);
    }
}
