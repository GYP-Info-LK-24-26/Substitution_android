package de.igelstudios.substitution;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.Comparators;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class RequestedCourses extends SQLiteOpenHelper {
    @FunctionalInterface
    public static interface Empty{
        void execute(boolean value);
    }
    public void fetchAndAdd() {
        new Thread(() -> {
            try {

                String data = makeHttpsRequest("courses");
                if(data.charAt(0) == 'E'){
                    MainActivity.getInstance().NOTIFIER.notifySimple("An error occurred during the connection");
                    return;
                }else if(data.equals("69420")){
                    MainActivity.getInstance().NOTIFIER.notifySimple("Wrong credentials used");
                    return;
                }else{
                    JSONArray object = new JSONArray(data);
                    List<Integer> courseIDS = new ArrayList<>();
                    for (int i = 0; i < object.length(); i++) {
                        int sub = ((Integer) object.get(i));
                        courseIDS.add(sub);
                    }

                    writeCourses(courseIDS);

                    reload();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void loadData(Empty empty){
        new Thread(() -> {
            try {

                String data = makeHttpsRequest("table");
                if(data.charAt(0) == 'E'){
                    MainActivity.getInstance().NOTIFIER.notifySimple("An error occurred during the connection");
                    empty.execute(false);
                    return;
                }else if(data.equals("69420")){
                    MainActivity.getInstance().NOTIFIER.notifySimple("Wrong credentials used");
                    empty.execute(false);
                    return;
                }else{
                    JSONArray object = new JSONArray(data);
                    List<Course> courseIDS = new ArrayList<>();
                    for (int i = 0; i < object.length(); i++) {
                        JSONObject obj = ((JSONObject) object.get(i));
                        courseIDS.add(new Course(obj.getInt("id"),obj.getString("Teacher"),obj.getString("Subject")));
                    }

                    reloadCourses(courseIDS);

                    data = makeHttpsRequest("lessons");
                    if(data.charAt(0) == 'E'){
                        MainActivity.getInstance().NOTIFIER.notifySimple("An error occurred during the connection");
                        empty.execute(false);
                        return;
                    }else if(data.equals("69420")){
                        MainActivity.getInstance().NOTIFIER.notifySimple("Wrong credentials used");
                        empty.execute(false);
                        return;
                    }else{
                        object = new JSONArray(data);
                        List<LessonCourse> lessons = new ArrayList<>();
                        for (int i = 0; i < object.length(); i++) {
                            JSONObject obj = ((JSONObject) object.get(i));
                            lessons.add(new LessonCourse(obj.getInt("lesson"),obj.getString("teacher"),obj.getInt("day"),obj.getInt("course")));
                        }

                        reloadLessons(lessons);
                        empty.execute(true);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void reloadCourses(List<Course> courses){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM Course;");

        for (Course course : courses) {
            db.execSQL("INSERT INTO Course (id,teacher,subject) VALUES (?,?,?)",new String[]{String.valueOf(course.id),course.teacher,course.subject});
        }
    }

    private void reloadLessons(List<LessonCourse> lessons){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM Lesson;");

        for (LessonCourse lesson : lessons) {
            db.execSQL("INSERT INTO Lesson (course,lessonTime,teacher,day) VALUES (?,?,?,?)",
                    new String[]{String.valueOf(lesson.course),String.valueOf(lesson.lesson),lesson.teacher,String.valueOf(lesson.day)});
        }
    }

    private String makeHttpsRequest(String path) {
        String result = "";
        try {
            MainActivity.IS_LOADING.postValue(true);
            URL url = new URL(Config.get().getConnectionURL() + path + "/");

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

            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

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

    private void writeCourses(List<Integer> courseIDS) {
        SQLiteDatabase db = this.getReadableDatabase();
        for (Integer courseID : courseIDS) {
            db.execSQL("INSERT OR IGNORE INTO SELECTED_COURSES (course) VALUES (?)",new String[]{String.valueOf(courseID)});
        }
    }

    public static class Course{
        public String teacher;
        public String subject;
        public int id;
        public boolean selected = false;

        public Course(int id, String teacher, String subject) {
            this.id = id;
            this.teacher = teacher;
            this.subject = subject;
        }
    }

    public static class LessonCourse {
        public LessonCourse(int lesson, String teacher, int day){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
        }

        public LessonCourse(int lesson,String teacher,int day,boolean selected){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
            this.selected = selected;
        }

        public LessonCourse(int lesson,String teacher,int day,int course){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
            this.course = course;
        }

        public LessonCourse(int lesson,String teacher,int day,int course,boolean selected){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
            this.course = course;
            this.selected = selected;
        }

        public int lesson;
        public int day;
        public String teacher;
        public int course = -1;
        public boolean selected = false;

        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof LessonCourse){
                if(((LessonCourse) obj).lesson != this.lesson)return false;
                if(((LessonCourse) obj).day != this.day)return false;
                return  ((LessonCourse) obj).teacher.equals(this.teacher);
            }
            return false;
        }

        @NonNull
        @Override
        public String toString() {
            return /*course + "  " +*/ teacher;
        }
    }
    private static final int DB_VERSION = 2;
    public List<LessonCourse> selectedCourses;
    public List<LessonCourse> lessons;
    public List<Course> allCourses;
    private boolean loaded = false;

    public RequestedCourses(@Nullable Context context, @Nullable String name) {
        super(context, name, null, DB_VERSION);

        selectedCourses = new ArrayList<>();
        allCourses = new ArrayList<>();
        lessons = new ArrayList<>();
    }

    public void reload(){
        loaded = false;
        selectedCourses = new ArrayList<>();
        allCourses = new ArrayList<>();
        lessons = new ArrayList<>();
        load();
    }

    public void load(){
        load(false);
    }

    public void load(boolean await){
        if(loaded)return;
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Empty empty = (val) -> {
            if(!val)future.complete(false);
            loaded = true;
            SQLiteDatabase db = this.getReadableDatabase();
            try(Cursor cr = db.rawQuery("SELECT Lesson.lessonTime,Lesson.teacher,Lesson.day,Lesson.course FROM Lesson INNER JOIN SELECTED_COURSES ON" +
                    " lesson.course = SELECTED_COURSES.course",null)){
                if(cr.moveToFirst()){
                    do{
                        selectedCourses.add(new LessonCourse(cr.getInt(0),cr.getString(1),cr.getInt(2),cr.getInt(3)));
                    }while (cr.moveToNext());
                }
            }

            try(Cursor cr = db.rawQuery("SELECT Lesson.course,Lesson.teacher,Lesson.lessonTime,Lesson.day FROM Lesson",null)){
                if(cr.moveToFirst()){
                    do{
                        lessons.add(new LessonCourse(cr.getInt(2),cr.getString(1),cr.getInt(3),cr.getInt(0)));
                    }while (cr.moveToNext());
                }
            }

            for (LessonCourse course : lessons) {
                for (LessonCourse cours : selectedCourses) {
                    if(cours.course == course.course){
                        course.selected = true;
                        break;
                    }
                }
            }

            try(Cursor cr = db.rawQuery("SELECT id,teacher,subject FROM Course",null)) {
                if(cr.moveToFirst()){
                    do{
                        allCourses.add(new Course(cr.getInt(0),cr.getString(1),cr.getString(2)));
                    }while (cr.moveToNext());
                }
            }

            allCourses.sort((s1,s2) -> {
                if(s1.subject.substring(1).equalsIgnoreCase(s2.subject.substring(1))){
                    return s1.subject.substring(1).compareTo(s2.subject.substring(1));
                }
                return s1.subject.substring(1).compareToIgnoreCase(s2.subject.substring(1));
            });

            for (Course course : allCourses) {
                for (LessonCourse selected : selectedCourses) {
                    if(course.id == selected.course){
                        course.selected = true;
                        break;
                    }
                }
            }
            future.complete(true);
        };
        try {
            if(hasData())empty.execute(true);
            else loadData(empty);
            if(await)future.get();
        }catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasData(){
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT COUNT(*) FROM Course",null)) {
            if(cr.moveToFirst()){
                return cr.getInt(0) > 0;
            }
        }

        return false;
    }

    public List<Substitution> strip(List<Substitution> changes){
        load(true);
        List<Substitution> member = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        for (Substitution change : changes) {
            //now.setTime(new Date(Integer.parseInt(change.date.substring(6,10)),Integer.parseInt(change.date.substring(3,5)),Integer.parseInt(change.date.substring(0,2))));
            now.set(Integer.parseInt(change.date.substring(6,10)),Integer.parseInt(change.date.substring(3,5)),Integer.parseInt(change.date.substring(0,2)));
            int dayID = now.get(Calendar.DAY_OF_WEEK) - 1;
            for (LessonCourse course : selectedCourses) {
                if(change.lesson == course.lesson && change.teacher.equals(course.teacher) && dayID == course.day){
                    member.add(change);
                    break;
                }
            }
        }

        return member;
    }

    public void toggle(Course course){
        if(course.selected){
            remove(course.id);
            for (LessonCourse edit : lessons) {
                if(course.id == edit.course){
                    edit.selected = false;
                    selectedCourses.remove(edit);
                }
            }

            course.selected = false;

        } else{
            add(course.id);

            for (LessonCourse edit : lessons) {
                if(course.id == edit.course){
                    edit.selected = true;
                    selectedCourses.add(edit);
                }
            }

            course.selected = true;
        }
    }

    public void add(int course_id){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("INSERT INTO SELECTED_COURSES (course) VALUES (?);",new String[]{String.valueOf(course_id)});
    }

    public void remove(int course_id){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM SELECTED_COURSES WHERE course = ?;",new String[]{String.valueOf(course_id)});
    }

    public List<LessonCourse> getFullList(){
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT course,lessonTime,teacher,day FROM Lesson;",null)) {
            List<LessonCourse> list = new ArrayList<>();
            if(cr.moveToFirst()){

                do{
                    list.add(new LessonCourse(cr.getInt(1),cr.getString(2),cr.getInt(3),cr.getInt(0)));
                }while (cr.moveToNext());
            }

            return list;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS SELECTED_COURSES (course INTEGER,PRIMARY KEY(course));");
        db.execSQL("CREATE TABLE IF NOT EXISTS Lesson (course INTEGER,lessonTime INTEGER,teacher String,day INTEGER, PRIMARY KEY(lessonTime,teacher,day));");
        db.execSQL("CREATE TABLE IF NOT EXISTS Course (id INTEGER,teacher TEXT,subject TEXT, PRIMARY KEY(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2) db.execSQL("CREATE TABLE IF NOT EXISTS Course (id INTEGER,teacher TEXT,subject TEXT, PRIMARY KEY(id))");
    }
}
