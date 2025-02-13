package de.igelstudios.substitution;

import android.content.Context;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.Objects;

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

    public int getPort(){
        if(!isDebug())return 4442;
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.context).getString("port","4442"));
    }

    public String getURL(){
        if(!isDebug())return "https://leafrinari-clan.dynv6.net";
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString("url","https://leafrinari-clan.dynv6.net");
    }

    public String getConnectionURL(){
        return getURL() + ":" + getPort() + "/";
    }

    public boolean isDebug(){
        return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("debug",false);
    }

    public boolean installPreRelease(){
        return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("pre_release",false);
    }

    public boolean installBeta(){
        return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("beta",false);
    }

    public boolean installAlpha(){
        return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("alpha",false);
    }

    public int getCurrentBuildNumber(){
        try {
            String val = new String(Util.read(Objects.requireNonNull(this.getClass().getClassLoader()).getResourceAsStream("data.txt")));
            return Integer.parseInt(val);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
