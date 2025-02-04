package de.igelstudios.substitution;

import android.content.Context;

import androidx.preference.PreferenceManager;

public class Config {
    private static Config instance;
    private Context context;

    public Config(Context context){
        instance = this;
        this.context = context;
    }

    public static Config get(){
        return instance;
    }

    public String getKey(){
        return "7YHgB+.'AtBdWB&";
    }

    public String getName() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString("first_name","");
    }

    public String getBirth_date() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString("birth_date","");
    }

    public String getLast_name() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString("last_name","");
    }
}
