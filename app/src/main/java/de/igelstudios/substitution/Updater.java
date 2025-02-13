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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

        List<String> data = makeHttpsRequest("docs/version");
        if(!data.isEmpty() && data.get(0).charAt(0) == 'E')MainActivity.getInstance().NOTIFIER.notifySimple("Error during updating");
        if(data.size() < 8){
            future.complete(null);
            return future;
        }

        int version = Integer.parseInt(data.get(1));
        if(version > Config.get().getCurrentBuildNumber())return install(version,installFile);

        if(!Config.get().installPreRelease()){
            future.complete(null);
            return future;
        }
        version = Integer.parseInt(data.get(3));
        if(version > Config.get().getCurrentBuildNumber())return install(version,installFile);

        if(!Config.get().installBeta()){
            future.complete(null);
            return future;
        }
        version = Integer.parseInt(data.get(5));
        if(version > Config.get().getCurrentBuildNumber())return install(version,installFile);

        if(!Config.get().installAlpha()){
            future.complete(null);
            return future;
        }
        version = Integer.parseInt(data.get(7));
        if(version > Config.get().getCurrentBuildNumber())return install(version,installFile);

        future.complete(null);
        return future;
    }

    private CompletableFuture<Intent> install(int version,boolean installFile) {
        CompletableFuture<Intent> future = new CompletableFuture<>();
        new Thread(() -> {
            try {
                MainActivity.IS_LOADING.postValue(true);
                File apkFile = new File(context.getCacheDir(), "app_update.apk");

                String name = (MainActivity.isDebug ? "docs/app-debug-" + version + ".apk" : "docs/app-release-" + version + ".apk");

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

                MainActivity.IS_LOADING.postValue(false);
                future.complete(installApk(apkFile,installFile));
            } catch (Exception e) {
                MainActivity.IS_LOADING.postValue(false);
                future.completeExceptionally(e);
            }
        }).start();

        return future;
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

    private List<String> makeHttpsRequest(String path) {
        List<String> result = new ArrayList<>();
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
                    result.add(line);
                }
            } else {
                result = List.of("Error: " + statusCode);
            }

            urlConnection.disconnect();
        } catch (Exception e) {
            MainActivity.IS_LOADING.postValue(false);
            return List.of("Exception: " + e.getMessage());
        }
        MainActivity.IS_LOADING.postValue(false);
        return result;
    }
}
