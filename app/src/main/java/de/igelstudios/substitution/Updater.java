package de.igelstudios.substitution;

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

import android.net.Uri;

public class Updater {
    private Context context;

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
        new Thread(() -> {
            try {
                if(checkVersion){
                    String version = makeHttpsRequest("version");
                    int idx = version.indexOf('.');
                    int major = Integer.parseInt(version.substring(0,idx));
                    int minor = Integer.parseInt(version.substring(idx + 1,version.indexOf(idx + 1,'.')));
                    if(major <= Config.get().getMajor() && minor <= Config.get().getMinor())return;
                }

                File apkFile = new File(context.getCacheDir(), "app_update.apk");

                URL url = new URL("https://leafrinari-clan.dynv6.net:4442/download");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
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
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    private String makeHttpsRequest(String path) {
        String result = "";
        try {
            URL url = new URL("https://leafrinari-clan.dynv6.net:4442/" + path);

            // Open connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            try(OutputStream os = urlConnection.getOutputStream()) {
                String json = "[\"" + Config.get().getName() + "\",\"" + Config.get().getLast_name() + "\",\"" + Config.get().getBirth_date() + "\",\"" + Config.get().getKey() + "\"]";
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                result = stringBuilder.toString();
            } else {
                result = "Error: " + statusCode;
            }

            urlConnection.disconnect();
        } catch (Exception e) {
            result = "Exception: " + e.getMessage();
        }
        return result;
    }
}
