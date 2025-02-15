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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

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
    private static final int DB_VERSION = 3;

    public Fetcher(@Nullable Context context, @Nullable String name) {
        super(context, name,null,DB_VERSION);
        this.context = context;
    }

    public void fetch(Consumer<List<Substitution>> consumer){
        this.fetchRemote(new Consumer<List<Substitution>>() {
            @Override
            public void accept(List<Substitution> remote) {
                List<Substitution> change = new ArrayList<>();
                SQLiteDatabase db = Fetcher.this.getReadableDatabase();
                try(Cursor cr = db.rawQuery("SELECT * FROM Substitution",null)) {

                    if (remote == null){
                        consumer.accept(null);
                        return;
                    }
                    List<Substitution> known = new ArrayList<>();

                    if (cr.moveToFirst()) {
                        do {
                            known.add(new Substitution(cr.getInt(0), cr.getString(1), cr.getString(2), cr.getString(3)
                                    , cr.getString(4), cr.getString(5), cr.getLong(6)));
                        } while (cr.moveToNext());
                    }

                    for (Substitution substitution : remote) {
                        if (!contains(known, substitution)) change.add(substitution);
                    }

                    for (Substitution substitution : change) {
                        add(substitution, db);
                    }

                    for (Substitution substitution : known) {
                        if (!contains(remote, substitution)) {
                            remove(substitution, db);
                            change.add(new Substitution(substitution.lesson, substitution.teacher, substitution.course_new, "", "Findet statt", "", substitution.getTime()));
                        }
                    }

                    consumer.accept(change);
                }
            }
        });
    }

    public List<Substitution> fetchLocal(){
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT * FROM Substitution",null)){
            List<Substitution> known = new ArrayList<>();

            if(cr.moveToFirst()){
                do{
                    known.add(new Substitution(cr.getInt(0),cr.getString(1),cr.getString(2),cr.getString(3)
                            ,cr.getString(4),cr.getString(5),cr.getLong(6)));
                }while (cr.moveToNext());
            }

            return known;
        }
    }

    public void cleanOld(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM Substitution WHERE date < ?",new String[]{String.valueOf(LocalDate.now().toEpochDay())});
    }

    private boolean contains(List<Substitution> list,Substitution current){
        for (Substitution substitution : list) {
            if(substitution.lesson == current.lesson && substitution.teacher.equals(current.teacher) && substitution.date.equals(current.date))return true;
        }
        return false;
    }

    private void add(Substitution substitution, SQLiteDatabase db) {
        db.execSQL("INSERT INTO Substitution (lesson,teacher,course_new,teacher_new,info,room,date) VALUES (?,?,?,?,?,?,?)",new String[]{
                String.valueOf(substitution.lesson),substitution.teacher, substitution.course_new,substitution.teacher_new,substitution.info,substitution.room, String.valueOf(substitution.getTime())
        });
    }

    private void remove(Substitution substitution,SQLiteDatabase db) {
        db.execSQL("DELETE FROM Substitution WHERE Lesson = ? AND Teacher = ?",new String[]{String.valueOf(substitution.lesson),substitution.teacher});
    }

    private void fetchRemote(Consumer<List<Substitution>> consumer) {
        MainActivity.requestPermissions();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(!isConnected){
            consumer.accept(null);
            return;
        }
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if(!isWiFi){
            consumer.accept(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            try {

                String data = makeHttpsRequest();
                List<Substitution> subs = new ArrayList<>();
                if(data.charAt(0) == 'E'){
                    MainActivity.getInstance().NOTIFIER.notifySimple("An error occurred during the connection");
                    consumer.accept(null);
                    return;
                }else if(data.equals("69420")){
                    MainActivity.getInstance().NOTIFIER.notifySimple("Wrong credentials used");
                    consumer.accept(null);
                    return;
                }else{
                    JSONArray object = new JSONArray(data);
                    for (int i = 0; i < object.length(); i++) {
                        JSONObject sub = ((JSONObject) object.get(i));
                        subs.add(new Substitution(sub.getInt("lesson"),sub.getString("teacher"),sub.getString("course_new"),sub.getString("teacher_new"),
                                sub.getString("info"),sub.getString("room"),sub.getString("date")));
                    }
                }

                consumer.accept(subs);
                //future.complete(result);

            } catch (Exception e) {
                throw new RuntimeException(e);
                //future.completeExceptionally(e);
            }
        }).start();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Substitution (lesson INTEGER,teacher TEXT,course_new TEXT,teacher_new TEXT,info TEXT,room TEXT,date INTEGER,PRIMARY KEY(lesson,teacher,date));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE FROM Substitution");
        if(oldVersion == 1 && newVersion == 2){
            db.execSQL("DROP TABLE Substitution");
            db.execSQL("CREATE TABLE Substitution (lesson INTEGER,teacher TEXT,course_new TEXT,teacher_new TEXT,info TEXT,room TEXT,date TEXT,PRIMARY KEY(lesson,teacher,date));");
        }else if(oldVersion == 2 && newVersion == 3){
            db.execSQL("DROP TABLE Substitution");
            db.execSQL("CREATE TABLE Substitution (lesson INTEGER,teacher TEXT,course_new TEXT,teacher_new TEXT,info TEXT,room TEXT,date INTEGER,PRIMARY KEY(lesson,teacher,date));");
        }else if(oldVersion == 1 && newVersion == 3){
            db.execSQL("DROP TABLE Substitution");
            db.execSQL("CREATE TABLE Substitution (lesson INTEGER,teacher TEXT,course_new TEXT,teacher_new TEXT,info TEXT,room TEXT,date INTEGER,PRIMARY KEY(lesson,teacher,date));");
        }
    }

    private String makeHttpsRequest() {
        String result = "";
        try {
            MainActivity.IS_LOADING.postValue(true);
            URL url = new URL(Config.get().getConnectionURL() + "substitution/");

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
        MainActivity.IS_LOADING.postValue(false);
        return result;
    }
}
