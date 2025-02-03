package de.igelstudios.substitution;

import android.content.Context;

import androidx.preference.PreferenceManager;

public class Config {
    private static String name;
    private static String last_name;
    private static String birth_date;
    private static Config instance;
    private Context context;

    public Config(Context context){
        instance = this;
        this.context = context;
    }

    public static Config get(){
        return instance;
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
