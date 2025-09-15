package de.igelstudios.substitution;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat{
    private boolean dirty = false;
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

        Preference loadAllButton = findPreference("load_all_table");
        loadAllButton.setOnPreferenceClickListener((preference) -> {
            MainActivity.getInstance().COURSES.load(true,true);
            return true;
        });

        Preference updateBTN = findPreference("update");
        updateBTN.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                MainActivity.getInstance().UPDATER.updateForce();
                return true;
            }
        });

        findPreference("cpyLogs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                Logger.get().toClipBoard();
                return true;
            }
        });

        findPreference("log_show").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                NavHostFragment.findNavController(SettingsFragment.this).navigate(R.id.action_settingsFragment_to_fragmentLog_view);
                //Util.showPopUp(Logger.get().read());
                return true;
            }
        });

        findPreference("first_name").setOnPreferenceChangeListener((preference, newValue) -> {
            dirty = true;
            return true;
        });

        findPreference("last_name").setOnPreferenceChangeListener((preference, newValue) -> {
            dirty = true;
            return true;
        });

        findPreference("birth_date").setOnPreferenceChangeListener((preference, newValue) -> {
            dirty = true;
            return true;
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

        if(dirty) {
            MainActivity.getInstance().FETCHER.fetch();
            dirty = false;
        }
        MainActivity.getInstance().COURSES.load();
    }
}
