package de.igelstudios.substitution;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Fetcher extends SQLiteOpenHelper {
    private final Context context;
    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
    };
    private static final int DB_VERSION = 1;

    public Fetcher(@Nullable Context context, @Nullable String name) {
        super(context, name,null,DB_VERSION);
        this.context = context;
    }

    public List<Substitution> fetch(){
        List<Substitution> change = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT * FROM Substitution",null)){
            List<Substitution> remote = this.fetchRemote();
            if(remote == null)return new ArrayList<>();
            List<Substitution> known = new ArrayList<>();

            if(cr.moveToFirst()){
                do{
                    known.add(new Substitution(cr.getInt(0),cr.getString(1),cr.getString(2),cr.getString(3)
                            ,cr.getString(4),cr.getString(5),cr.getString(6)));
                }while (cr.moveToNext());
            }

            for (Substitution substitution : remote) {
                if(!contains(known,substitution))change.add(substitution);
            }

            for (Substitution substitution : change) {
                add(substitution,db);
            }

            for (Substitution substitution : known) {
                if(!contains(remote,substitution)) {
                    remove(substitution, db);
                    change.add(new Substitution(substitution.lesson, substitution.teacher, substitution.course_new, "", "Findet stat", "", substitution.date));
                }
            }

            return change;
        }

    }

    public List<Substitution> fetchLocal(){
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT * FROM Substitution",null)){
            List<Substitution> known = new ArrayList<>();

            if(cr.moveToFirst()){
                do{
                    known.add(new Substitution(cr.getInt(0),cr.getString(1),cr.getString(2),cr.getString(3)
                            ,cr.getString(4),cr.getString(5),cr.getString(6)));
                }while (cr.moveToNext());
            }

            return known;
        }
    }

    public void cleanOld(){
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String str = cal.getTime().getDay() + "." + cal.getTime().getMonth() + "." + cal.getTime().getYear();
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM Substitiution WHERE date = ?",new String[]{str});
    }

    private boolean contains(List<Substitution> list,Substitution current){
        for (Substitution substitution : list) {
            if(substitution.lesson == current.lesson && substitution.teacher.equals(current.teacher))return true;
        }
        return false;
    }

    private void add(Substitution substitution, SQLiteDatabase db) {
        db.execSQL("INSERT INTO Substitution (lesson,teacher,course_new,teacher_new,info,room,date) VALUES (?,?,?,?,?,?,?)",new String[]{
                String.valueOf(substitution.lesson),substitution.teacher, substitution.course_new,substitution.teacher_new,substitution.info,substitution.room,substitution.date
        });
    }

    private void remove(Substitution substitution,SQLiteDatabase db) {
        db.execSQL("DELETE FROM Substitution WHERE Lesson = ? AND Teacher = ?",new String[]{String.valueOf(substitution.lesson),substitution.teacher});
    }

    private List<Substitution> fetchRemote() {
        MainActivity.requestPermissions();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if(!isConnected || !isWiFi)return new ArrayList<>();

        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(() -> {
            try {

                String result = makeHttpsRequest();
                future.complete(result);

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).start();

        try {
            List<Substitution> subs = new ArrayList<>();
            String data = future.get();
            if(data.charAt(0) == 'E'){
                MainActivity.getInstance().NOTIFIER.notifySimple("An error occurred during the connection");
                return null;
            }else if(data.equals("69420")){
                MainActivity.getInstance().NOTIFIER.notifySimple("Wrong credentials used");
                return null;
            }else{
                JSONArray object = new JSONArray(data);
                for (int i = 0; i < object.length(); i++) {
                    JSONObject sub = ((JSONObject) object.get(i));
                    subs.add(new Substitution(sub.getInt("lesson"),sub.getString("teacher"),sub.getString("course_new"),sub.getString("teacher_new"),
                            sub.getString("info"),sub.getString("room"),sub.getString("date")));
                }
            }

            return subs;
        } catch (ExecutionException | InterruptedException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Substitution (lesson INTEGER,teacher TEXT,course_new TEXT,teacher_new TEXT,info TEXT,room TEXT,date TEXT,PRIMARY KEY(lesson,teacher));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE FROM Substitution");
    }

    private String makeHttpsRequest() {
        String result = "";
        try {
            URL url = new URL("https://leafrinari-clan.dynv6.net:4442");

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
