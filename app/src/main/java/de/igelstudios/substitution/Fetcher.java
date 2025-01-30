package de.igelstudios.substitution;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Fetcher extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;

    public Fetcher(@Nullable Context context, @Nullable String name) {
        super(context, name,null,DB_VERSION);
    }

    public List<Substitution> fetch(){
        List<Substitution> change = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try(Cursor cr = db.rawQuery("SELECT * FROM Substitution",null)){
            List<Substitution> remote = this.fetchRemote();
            List<Substitution> known = new ArrayList<>();

            if(cr.moveToFirst()){
                while (cr.moveToNext()){
                    known.add(new Substitution(cr.getInt(1),cr.getString(2),cr.getString(3),cr.getString(4)
                            ,cr.getString(5),cr.getString(6),cr.getString(7)));
                }
            }

            for (Substitution substitution : remote) {
                if(!known.contains(substitution))change.add(substitution);
            }


            for (Substitution substitution : known) {
                if(!remote.contains(substitution))remove(substitution,db);
                change.add(new Substitution(substitution.lesson, substitution.teacher, substitution.course_new,"","Findet stat","", substitution.date));
            }

            return change;
        }

    }

    private void remove(Substitution substitution,SQLiteDatabase db) {
        Cursor cr = db.rawQuery("DELETE FROM Substitution WHERE Lesson = ? AND Teacher = ?",new String[]{String.valueOf(substitution.lesson),substitution.teacher});
        cr.close();
    }

    private List<Substitution> fetchRemote() {
        return List.of(new Substitution(1,"Stz","B_1_3","","Studierzeit","B-006","28.1.2025"));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Substitution (lesson INTEGER,teacher TEXT,course_new TEXT,teacher_new TEXT,info TEXT,room TEXT,date TEXT,PRIMARY KEY(lesson,teacher));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE FROM Substitution");
    }
}
