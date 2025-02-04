package de.igelstudios.substitution;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RequestedCourses extends SQLiteOpenHelper {
    private static final List<Course> COURSE_LIST = List.of(new Course(1,"Stz",1,46),new Course(1,"Snd",1,2),
            new Course(1,"Kon",1,15),new Course(1,"Kir",2,39),new Course(1,"Phi",2,49),
            new Course(1,"HrA",2,35),new Course(1,"Ruf",2,38),new Course(1,"For",3,57),
            new Course(1,"Dil",3,21),new Course(1,"Kne",3,62),new Course(1,"Ken",4,43),
            new Course(1,"Rau",4,59),new Course(1,"Tho",4,25),new Course(1,"Nes",4,37),
            new Course(1,"Hau",4,44),new Course(1,"Phi",5,49),new Course(1,"Ruf",5,38),
            new Course(1,"HrA",5,35),new Course(1,"Kir",5,39),new Course(2,"Kon",1,15),
            new Course(2,"Stz",1,46),new Course(2,"Snd",1,2),new Course(2,"Ruf",2,38),
            new Course(2,"Kir",2,39),new Course(2,"HrA",2,35),new Course(2,"Phi",2,49),
            new Course(2,"Kop",3,61),new Course(2,"Eib",3,14),new Course(2,"Omr",3,22),
            new Course(2,"Sil",3,13),new Course(2,"Cia",3,27),new Course(2,"Nes",4,37),
            new Course(2,"Ken",4,43),new Course(2,"Rau",4,59),new Course(2,"Tho",4,25),
            new Course(2,"Hau",4,44),new Course(2,"Kop",5,61),new Course(2,"Eib",5,14),
            new Course(2,"Sil",5,13),new Course(2,"Omr",5,22),new Course(2,"Cia",5,27),
            new Course(3,"Omr",1,22),new Course(3,"Sil",1,13),new Course(3,"Cia",1,27),
            new Course(3,"Eib",1,14),new Course(3,"Kop",1,61),new Course(3,"Ken",2,43),
            new Course(3,"Rau",2,59),new Course(3,"Nes",2,37),new Course(3,"Tho",2,25),
            new Course(3,"Hau",2,44),new Course(3,"Wdl",3,40),new Course(3,"Wag",3,63),
            new Course(3,"Lan",3,12),new Course(3,"Pal",3,18),new Course(3,"For",4,57),
            new Course(3,"Ruf",4,38),new Course(3,"Kne",4,62),new Course(3,"Wag",4,63),
            new Course(3,"Knz",4,19),new Course(3,"HoM",4,51),new Course(3,"Dil",4,21),
            new Course(3,"Fag",5,34),new Course(3,"Rem",5,53),new Course(3,"Hoh",5,50),
            new Course(3,"Zeh",5,24),new Course(3,"Hub",5,41),new Course(4,"Eib",1,14),
            new Course(4,"Sil",1,13),new Course(4,"Omr",1,22),new Course(4,"Kop",1,61),
            new Course(4,"Cia",1,27),new Course(4,"Rau",2,59),new Course(4,"Nes",2,37),
            new Course(4,"Ken",2,43),new Course(4,"Tho",2,25),new Course(4,"Hau",2,44),
            new Course(4,"Pal",3,18),new Course(4,"Wdl",3,40),new Course(4,"Wag",3,63),
            new Course(4,"Lan",3,12),new Course(4,"Knz",4,19),new Course(4,"For",4,57),
            new Course(4,"Kne",4,62),new Course(4,"Dil",4,21),new Course(4,"HoM",4,51),
            new Course(4,"Ruf",4,38),new Course(4,"Wag",4,63),new Course(4,"Fag",5,34),
            new Course(4,"Zeh",5,24),new Course(4,"Hoh",5,50),new Course(4,"Hub",5,41),
            new Course(4,"Rem",5,53),new Course(5,"Hei",1,7),new Course(5,"Hop",1,16),
            new Course(5,"Kla",1,8),new Course(5,"Sku",1,56),new Course(5,"Sfr",1,65),
            new Course(5,"Hau",2,31),new Course(5,"Hof",2,30),new Course(5,"Con",2,47),
            new Course(5,"DeA",2,4),new Course(5,"Lbl",2,66),new Course(5,"HoM",3,51),
            new Course(5,"Son",3,32),new Course(5,"Sus",3,1),new Course(5,"Sho",3,9),
            new Course(5,"DeS",4,54),new Course(5,"Isr",4,5),new Course(5,"Wag",4,42),
            new Course(5,"Stb",4,33),new Course(5,"For",5,57),new Course(5,"Kne",5,62),
            new Course(5,"Dil",5,21),new Course(6,"Sfr",1,65),new Course(6,"Hei",1,7),
            new Course(6,"Hop",1,16),new Course(6,"Sku",1,56),new Course(6,"Kla",1,8),
            new Course(6,"Con",2,47),new Course(6,"Hof",2,30),new Course(6,"Hau",2,31),
            new Course(6,"Lbl",2,66),new Course(6,"DeA",2,4),new Course(6,"Sho",3,9),
            new Course(6,"Sus",3,1),new Course(6,"Son",3,32),new Course(6,"HoM",3,51),
            new Course(6,"Wag",4,42),new Course(6,"Stb",4,33),new Course(6,"DeS",4,54),
            new Course(6,"Isr",4,5),new Course(6,"Dil",5,21),new Course(6,"Kne",5,62),
            new Course(6,"For",5,57),new Course(7,"Son",5,55),new Course(7,"Fis",5,28),
            new Course(8,"HrA",1,45),new Course(8,"Sus",1,17),new Course(8,"Stz",2,48),
            new Course(8,"Stb",2,29),new Course(8,"Wag",2,3),new Course(8,"Omr",2,11),
            new Course(8,"Ker",2,64),new Course(8,"Kam",2,58),new Course(8,"Sus",2,52),
            new Course(8,"Zeh",3,24),new Course(8,"Fag",3,34),new Course(8,"Hoh",3,50),
            new Course(8,"Rem",3,53),new Course(8,"Hub",3,41),new Course(8,"Ama",4,60),
            new Course(8,"Ber",4,6),new Course(8,"Son",5,55),new Course(8,"Fis",5,28),
            new Course(9,"Sus",1,17),new Course(9,"HrA",1,45),new Course(9,"Stz",2,48),
            new Course(9,"Stb",2,29),new Course(9,"Wag",2,3),new Course(9,"Ker",2,64),
            new Course(9,"Sus",2,52),new Course(9,"Omr",2,11),new Course(9,"Kam",2,58),
            new Course(9,"Stz",3,46),new Course(9,"Kon",3,15),new Course(9,"Snd",3,2),
            new Course(9,"Ama",4,60),new Course(9,"Ber",4,6),new Course(10,"Shc",2,20),
            new Course(10,"Web",3,23),new Course(10,"Kam",3,26),new Course(10,"Knz",3,19),
            new Course(10,"Dil",3,36),new Course(10,"Hof",3,10),new Course(11,"Shc",2,20),
            new Course(11,"Web",3,23),new Course(11,"Knz",3,19),new Course(11,"Hof",3,10),
            new Course(11,"Dil",3,36),new Course(11,"Kam",3,26));

    public static class Course{
        public Course(int lesson,String teacher,int day){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
        }

        public Course(int lesson,String teacher,int day,boolean selected){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
            this.selected = selected;
        }

        public Course(int lesson,String teacher,int day,int course,boolean selected){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
            this.course = course;
            this.selected = selected;
        }

        public Course(int lesson,String teacher,int day,int course){
            this.lesson = lesson;
            this.teacher = teacher;
            this.day = day;
            this.course = course;
        }
        public int lesson;
        public int day;
        public String teacher;
        public int course = -1;
        public boolean selected = false;

        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof Course){
                if(((Course) obj).lesson != this.lesson)return false;
                if(((Course) obj).day != this.day)return false;
                return  ((Course) obj).teacher.equals(this.teacher);
            }
            return false;
        }

        @NonNull
        @Override
        public String toString() {
            return /*course + "  " +*/ teacher;
        }
    }
    private static final int DB_VERSION = 1;
    private List<Course> courses;
    private boolean loaded = false;

    List<List<List<RequestedCourses.Course>>> sorted_courses = new ArrayList<>();

    public RequestedCourses(@Nullable Context context, @Nullable String name) {
        super(context, name, null, DB_VERSION);

        courses = new ArrayList<>();
    }

    public void load(){
        if(loaded)return;
        loaded = true;
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT Lesson.lessonTime,Lesson.teacher,Lesson.day,Lesson.course FROM Lesson INNER JOIN SELECTED_COURSES ON" +
                " lesson.course = SELECTED_COURSES.course",null)){
            if(cr.moveToFirst()){
                do{
                    courses.add(new Course(cr.getInt(0),cr.getString(1),cr.getInt(2),cr.getInt(3),true));
                }while (cr.moveToNext());
            }
        }

        for (int i = 0; i < 11; i++) {
            sorted_courses.add(new ArrayList<>());
            for (int j = 0; j < 5; j++) {
                sorted_courses.get(i).add(new ArrayList<>());
            }
        }

        List<Course> all_curses = new ArrayList<>();
        try(Cursor cr = db.rawQuery("SELECT Lesson.course,Lesson.teacher,Lesson.lessonTime,Lesson.day FROM Lesson",null)){
            if(cr.moveToFirst()){
                do{
                    all_curses.add(new Course(cr.getInt(2),cr.getString(1),cr.getInt(3),cr.getInt(0)));
                }while (cr.moveToNext());
            }
        }

        for (RequestedCourses.Course course : all_curses) {
            for (Course cours : courses) {
                if(cours.course == course.course){
                    course.selected = true;
                    break;
                }
            }
            sorted_courses.get(course.lesson - 1).get(course.day - 1).add(course);

        }
    }

    public List<Substitution> strip(List<Substitution> changes){
        load();
        List<Substitution> member = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        for (Substitution change : changes) {
            //now.setTime(new Date(Integer.parseInt(change.date.substring(6,10)),Integer.parseInt(change.date.substring(3,5)),Integer.parseInt(change.date.substring(0,2))));
            now.set(Integer.parseInt(change.date.substring(6,10)),Integer.parseInt(change.date.substring(3,5)),Integer.parseInt(change.date.substring(0,2)));
            int dayID = now.get(Calendar.DAY_OF_WEEK) - 1;
            for (Course course : courses) {
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
            remove(course.course);
            for (Course edit : COURSE_LIST) {
                if(course.course == edit.course) edit.selected = false;
            }

            for (List<List<Course>> sortedCours : sorted_courses) {
                for (List<Course> sortedCour : sortedCours) {
                    for (Course edit : sortedCour) {
                        if(course.course == edit.course)edit.selected = false;
                    }
                }
            }
        } else{
            add(course.course);

            for (Course edit : COURSE_LIST) {
                if(course.course == edit.course) edit.selected = true;
            }

            for (List<List<Course>> sortedCours : sorted_courses) {
                for (List<Course> sortedCour : sortedCours) {
                    for (Course edit : sortedCour) {
                        if(course.course == edit.course) edit.selected = true;
                    }
                }
            }
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

    /*public void add(int lesson,String teacher){
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT Lesson.course FROM Lesson WHERE Lesson.lessonTime = ? AND Lesson.teacher = ?",new String[]{String.valueOf(lesson),teacher})){
            if(cr.moveToFirst()){
                int id = cr.getInt(0);
                db.execSQL("INSERT INTO SELECTED_COURSES (course) VALUES (?)",new String[]{String.valueOf(id)});
            }
        }
    }

    public void remove(int lesson,String teacher){
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT Lesson.course FROM Lesson WHERE Lesson.lessonTime = ? AND Lesson.teacher = ?",new String[]{String.valueOf(lesson),teacher})){
            if(cr.moveToFirst()){
                int id = cr.getInt(0);
                db.execSQL("DELETE FROM SELECTED_COURSES WHERE course = ?",new String[]{String.valueOf(id)});
            }
        }
    }*/

    public List<Course> getFullList(){
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT course,lessonTime,teacher,day FROM Lesson;",null)) {
            List<Course> list = new ArrayList<>();
            if(cr.moveToFirst()){

                do{
                    list.add(new Course(cr.getInt(1),cr.getString(2),cr.getInt(3),cr.getInt(0)));
                }while (cr.moveToNext());
            }

            return list;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS SELECTED_COURSES (course INTEGER,PRIMARY KEY(course));");
        db.execSQL("CREATE TABLE IF NOT EXISTS Lesson (course INTEGER,lessonTime INTEGER,teacher String,day INTEGER, PRIMARY KEY(lessonTime,teacher,day));");

        for (Course course : COURSE_LIST) {
            db.execSQL("INSERT INTO Lesson (course,lessonTime,teacher,day) VALUES (?,?,?,?);",
                    new String[]{String.valueOf(course.course),String.valueOf(course.lesson),course.teacher,String.valueOf(course.day)});

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
