package de.igelstudios.substitution;

import static android.app.Activity.RESULT_OK;
import static androidx.core.app.ActivityCompat.startActivityForResult;

import static com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.Reflection.getPackageName;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

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
    private static final String gitURL = "https://gyp-info-lk-24-26.github.io/Substitution_android/";
    public Updater(Context context){
        this.context = context;
    }

    public void update(){
        downloadAndInstallApk(true);
    }

    public void updateForce(){
        downloadAndInstallApk(false);
    }

    public void downloadAndInstallApk(boolean checkVersion) {

        if(checkVersion){
            List<String> data = makeHttpsRequest("docs/version");
            if(data.size() < 8)return;

            int version = Integer.parseInt(data.get(1));
            if(version > Config.get().getCurrentBuildNumber())install();

            if(!Config.get().installPreRelease())return;
            version = Integer.parseInt(data.get(3));
            if(version > Config.get().getCurrentBuildNumber())install();

            if(!Config.get().installBeta())return;
            version = Integer.parseInt(data.get(5));
            if(version > Config.get().getCurrentBuildNumber())install();

            if(!Config.get().installAlpha())return;
            version = Integer.parseInt(data.get(7));
            if(version > Config.get().getCurrentBuildNumber())install();
        }

    }

    private void install() {
        new Thread(() -> {
            try {


                File apkFile = new File(context.getCacheDir(), "app_update.apk");

                String name = (MainActivity.isDebug ? "app/build/outputs/apk/debug/app-debug.apk" : "app/release/app-release.apk");

                URL url = new URL(/*"https://gyp-info-lk-24-26.github.io/Substitution_android/app-release.apk"*/gitURL + name);
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

                // Install APK
                installApk(apkFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void installApk(File apkFile) {
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

        context.startActivity(intent);
    }

    private List<String> makeHttpsRequest(String path) {
        List<String> result = new ArrayList<>();
        try {
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
                    result.add(line);
                }
            } else {
                result = List.of("Error: " + statusCode);
            }

            urlConnection.disconnect();
        } catch (Exception e) {
            return List.of("Exception: " + e.getMessage());
        }
        return result;
    }
}
