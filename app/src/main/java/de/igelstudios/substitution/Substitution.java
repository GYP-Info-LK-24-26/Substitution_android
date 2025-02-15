package de.igelstudios.substitution;

import java.time.LocalDate;

public class Substitution {
    public final int lesson;
    public final String teacher;
    public final String course_new;
    public final String teacher_new;
    public final String info;
    public final String room;
    public final String date;

    public Substitution(int lesson, String teacher, String courseNew, String teacherNew, String info, String room, long date) {
        this.lesson = lesson;
        this.teacher = teacher;
        course_new = courseNew;
        teacher_new = teacherNew;
        this.info = info;
        this.room = room;
        this.date = Util.timeToDate(LocalDate.ofEpochDay(date));
    }

    public Substitution(int lesson, String teacher, String courseNew, String teacherNew, String info, String room, String date) {
        this.lesson = lesson;
        this.teacher = teacher;
        course_new = courseNew;
        teacher_new = teacherNew;
        this.info = info;
        this.room = room;
        this.date = date;
    }

    public int getDayId(){
        return Util.dateFromString(date).getDayOfWeek().ordinal();
    }

    public long getTime(){
        return Util.dateFromString(date).toEpochDay();
    }
}
