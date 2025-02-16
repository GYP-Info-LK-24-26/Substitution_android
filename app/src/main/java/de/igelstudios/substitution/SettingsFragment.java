package de.igelstudios.substitution;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

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

        CheckBoxPreference preference = findPreference("debug");
        PreferenceCategory category = findPreference("debug_category");

        if(preference.isChecked()){
            getPreferenceScreen().addPreference(category);
        }else{
            getPreferenceScreen().removePreference(category);
        }

        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            if((boolean) newValue){
                getPreferenceScreen().addPreference(category);
            }else{
                getPreferenceScreen().removePreference(category);
            }
            return true;
        });

        //CheckBoxPreference alpha = findPreference("alpha");
        //CheckBoxPreference beta = findPreference("beta");
        CheckBoxPreference preRelease = findPreference("pre_release");
        /*if(alpha.isChecked()){
            beta.setChecked(true);
            beta.setEnabled(false);
        }

        alpha.setOnPreferenceChangeListener((p,v) -> {
            if((boolean) v){
                beta.setChecked(true);
                beta.setEnabled(false);
                preRelease.setChecked(true);
                preRelease.setEnabled(false);
            }else{
                beta.setChecked(false);
                beta.setEnabled(true);
                preRelease.setChecked(false);
                preRelease.setEnabled(true);
            }

            return true;
        });

        if(beta.isChecked()){
            preRelease.setChecked(true);
            preRelease.setEnabled(false);
        }

        beta.setOnPreferenceChangeListener((p,v) -> {
            if((boolean) v){
                preRelease.setChecked(true);
                preRelease.setEnabled(false);
            }else{
                preRelease.setChecked(false);
                preRelease.setEnabled(true);
            }

            return true;
        });*/

        Preference version = findPreference("build_number");
        version.setTitle("Version: " + Config.get().getCurrentBuildNumber());
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity.getInstance().settings = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.getInstance().settings = false;

        MainActivity.getInstance().FETCHER.fetch(MainActivity.getInstance().NOTIFIER::notifieChanges);
        MainActivity.getInstance().COURSES.load();
    }
}
