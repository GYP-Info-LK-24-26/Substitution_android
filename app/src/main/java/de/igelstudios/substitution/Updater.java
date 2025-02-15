package de.igelstudios.substitution;

import static android.app.Activity.RESULT_OK;
import static androidx.core.app.ActivityCompat.startActivityForResult;

import static com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.Reflection.getPackageName;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * version file layout:
 * release version<br>
 * release version build number<br>
 * <br>
 * pre-release version<br>
 * pre-release version build number<br>
 * <br>
 * beta version<br>
 * beta version build number<br>
 * <br>
 * alpha version<br>
 * alpha version build number<br>
 */
public class Updater {
    private Context context;
    private static final String gitURL = "https://api.github.com/";//"https://gyp-info-lk-24-26.github.io/Substitution_android/";
    public Updater(Context context){
        this.context = context;
    }

    public Intent update(){
        try {
            return downloadAndInstallApk(false).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateForce(){
        downloadAndInstallApk(true).thenAccept((intent -> {
            if(intent == null)MainActivity.getInstance().NOTIFIER.notifySimple("Keine Neue version verfügbar");
        }));
    }

    public CompletableFuture<Intent> downloadAndInstallApk(boolean installFile) {
        CompletableFuture<Intent> future = new CompletableFuture<>();
        new Thread(() -> {
            List<Version> response = getVersions();
            int i = 0;
            if(!Config.get().installPreRelease()){
                while (i < response.size() && response.get(i).preRelease)i++;
            }
            if(i >= response.size()){
                future.complete(null);
                return;
            }

            String installed = PreferenceManager.getDefaultSharedPreferences(this.context).getString("version","");
            int j = 0;
            while (j < response.size() && !response.get(j).name.equals(installed))j++;
            ///also install version if current version could not be found
            if(i <= j)future.complete(null);
            else future.complete(install(response.get(i),installFile));
        }).start();

        return future;
    }

    public void updateSavedVersion(){
        if(PreferenceManager.getDefaultSharedPreferences(this.context).contains("version"))return;
        new Thread(() -> {
            List<Version> response = getVersions();

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
            editor.putString("version",response.get(0).name);
            editor.apply();
        }).start();
    }

    private Intent install(Version version,boolean installFile) {
            try {
                MainActivity.IS_LOADING.postValue(true);
                File apkFile = new File(context.getCacheDir(), "app_update.apk");

                String name = (MainActivity.isDebug ? "app-debug.apk" : "app-release.apk");

                URL url = new URL(version.htmlURL + name);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                //connection.setDoOutput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(apkFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                connection.disconnect();

                MainActivity.IS_LOADING.postValue(false);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
                editor.putString("version",version.name);
                editor.apply();
                return installApk(apkFile,installFile);
            } catch (Exception e) {
                MainActivity.IS_LOADING.postValue(false);
                throw new RuntimeException(e);
            }
    }

    private Intent installApk(File apkFile,boolean installFile) {
        //Uri apkUri = Uri.fromFile(apkFile);
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                // Redirect to the settings page where the user can allow the app to install APKs
                Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse("package:" + context.getPackageName()));
                startActivityForResult(settingsIntent, 1234); // Request code can be anything
                return;
            }
        }*/

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(installFile)context.startActivity(intent);
        return intent;
    }

    private static class Version{
        public boolean preRelease;
        public String name,date,htmlURL;
        public Version(boolean preRelease,String name,String date,String htmlURL){
            this.preRelease = preRelease;
            this.name = name;
            this.date = date;
            this.htmlURL = htmlURL.replace("tag","download") + "/";
        }
    }

    private List<Version> getVersions(){
        try {
            String data = makeHttpsRequest("repos/GYP-Info-LK-24-26/Substitution_android/releases");
            JSONArray object = new JSONArray(data);
            List<Version> versions = new ArrayList<>(object.length());
            for (int i = 0; i < object.length(); i++) {
                JSONObject sub = ((JSONObject) object.get(i));
                versions.add(new Version(sub.getBoolean("prerelease"),sub.getString("name"),sub.getString("published_at"),
                        sub.getString("html_url")));
            }
            versions.sort(Comparator.comparing(a -> Util.versionFromString(a.name)));
            return versions;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeHttpsRequest(String path) {
        StringBuilder result = new StringBuilder();
        try {
            MainActivity.IS_LOADING.postValue(true);
            URL url = new URL(gitURL + path);

            // Open connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            /*urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);*/

            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } else {
                result.append("Error: ").append(statusCode);
            }

            urlConnection.disconnect();
        } catch (Exception e) {
            MainActivity.IS_LOADING.postValue(false);
            return "Exception: " + e.getMessage();
        }
        MainActivity.IS_LOADING.postValue(false);
        return result.toString();
    }
}
