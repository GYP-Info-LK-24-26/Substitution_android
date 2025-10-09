package de.igelstudios.substitution;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.igelstudios.substitution.databinding.FragmentFirstBinding;
import de.igelstudios.substitution.databinding.FragmentInfotableBinding;
import de.igelstudios.substitution.databinding.FragmentTableBinding;
import kotlin.Triple;

public class InfoTable extends Fragment {
    public static boolean locked;
    public static class Listener implements View.OnTouchListener{
        public int day,lesson;

        public Listener(int day,int lesson){
            this.day = day;
            this.lesson = lesson;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(locked)return true;
            int y = (int) event.getY();

            int lineNumber = ((TextView) v).getLayout().getLineForVertical(y);
            if(lineNumber >= MainActivity.getInstance().COURSES.selectedCoursesTable.get(day).get(lesson).size())return true;

            RequestedCourses.LessonCourse course = MainActivity.getInstance().COURSES.selectedCoursesTable.get(day).get(lesson).get(lineNumber);
            List<Substitution> substitutions = MainActivity.getInstance().FETCHER.fetchLocal();
            for (Substitution substitution : substitutions) {
                if(substitution.lesson != lesson || substitution.getDayId() != day || !substitution.teacher.equals(course.teacher))continue;
                MainActivity.getInstance().NOTIFIER.showSubstitutionPopUp(substitution);
                locked = true;
            }
            v.performClick();
            return true;
        }
    }
    public FragmentInfotableBinding binding;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentInfotableBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().infoTable = true;
        if(MainActivity.getInstance().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) vertical();
        else horizontal();
    }

    public void vertical(){
        List<Substitution> subs = MainActivity.getInstance().COURSES.strip(MainActivity.getInstance().FETCHER.fetchLocal());
        TableLayout table = MainActivity.getInstance().findViewById(R.id.sub_table);
        table.removeAllViews();
        /*if(subs.isEmpty()){
            TextView text = new TextView(getContext());
            text.setTextColor(MainActivity.textColor.toArgb());
            text.setText("Keine vertretungen");
            table.addView(text);
            return;
        }*/
        subs.sort((first,second) -> {
            if(first.date.equals(second.date)){
                if(first.lesson == second.lesson)return first.teacher.compareTo(second.teacher);
                return Integer.compare(first.lesson,second.lesson);
            }
            return Util.dateFromString(first.date).compareTo(Util.dateFromString(second.date));
        });


        LocalDateTime time = LocalDateTime.now();
        int dayID = time.getDayOfWeek().ordinal();
        if(time.getHour() >= 18)dayID++;
        if(dayID >= 5)dayID = 0;
        //TODO why the fuck did i think it would be a good idea to make the first shown day the one with the first substitution
        /*if(!subs.isEmpty()) {
            Substitution change = subs.get(0);
            dayID = Util.dateFromString(change.date).getDayOfWeek().ordinal(); //now.get(Calendar.DAY_OF_WEEK);
        }*/

        int current = 0;
        List<List<RequestedCourses.LessonCourse>> courses = MainActivity.getInstance().COURSES.selectedCoursesTable.get(dayID);
        for (int i = 0; i < courses.size(); i++) {
            StringBuilder builder = new StringBuilder();
            List<Triple<Integer,Integer, ForegroundColorSpan>> spans = new ArrayList<>();
            int charCount = 0;
            boolean first = false;
            for (RequestedCourses.LessonCourse course : courses.get(i)) {
                if(!subs.isEmpty()) {
                    //while (current < subs.size() - 1 && subs.get(current).lesson <= i) current++;
                    //while (current < subs.size() - 1 && subs.get(current).lesson == i + 1 && !subs.get(current).teacher.equals(course.teacher))current++;
                    if (current < subs.size() && subs.get(current).lesson == i && subs.get(current).teacher.equals(course.teacher)) {
                        int color = Util.isCanceled(subs.get(current)) ? 0xFFFF0000 : 0xFFFFFF00;
                        spans.add(new Triple<>(charCount, charCount + 3, new ForegroundColorSpan(color)));
                        current++;
                    }
                }
                if(first)builder.append('\n');
                builder.append(course.teacher);
                charCount += 4;
                first = true;
            }
            SpannableString span = new SpannableString(builder.toString());
            for (Triple<Integer, Integer, ForegroundColorSpan> spanColor : spans) {
                span.setSpan(spanColor.component3(),spanColor.component1(),spanColor.component2(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextView view = new TextView(getContext());
            view.setText(span);
            view.setTextColor(MainActivity.textColor.toArgb());
            view.setOnTouchListener(new Listener(dayID,i));
            TableRow row = new TableRow(getContext());
            if(Config.get().showLessonNumbers()) {
                TextView lessonText = new TextView(getContext());
                lessonText.setText((i + 1) + ".");
                lessonText.setTextColor(MainActivity.textColor.toArgb());
                row.addView(lessonText);
            }
            row.addView(view);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            table.addView(row);
        }
    }

    public void horizontal(){
        List<Substitution> subs = MainActivity.getInstance().COURSES.strip(MainActivity.getInstance().FETCHER.fetchLocal());
        TableLayout table = MainActivity.getInstance().findViewById(R.id.sub_table);
        subs.sort((first,second) -> {
            if(first.lesson == second.lesson){
                if(first.date.equals(second.date))return first.teacher.compareTo(second.teacher);
                return Integer.compare(Util.dateFromString(first.date).getDayOfWeek().ordinal(),Util.dateFromString(second.date).getDayOfWeek().ordinal());
            }
            return Integer.compare(first.lesson,second.lesson);
        });

        int current = 0;
        List<List<List<RequestedCourses.LessonCourse>>> courses = MainActivity.getInstance().COURSES.selectedCoursesTable;
        for (int i = 0; i < 11; i++) {
            TableRow row = new TableRow(getContext());
            if(Config.get().showLessonNumbers()) {
                TextView lessonText = new TextView(getContext());
                lessonText.setText((i + 1) + ".");
                lessonText.setTextColor(MainActivity.textColor.toArgb());
                row.addView(lessonText);
            }
            for (int j = 0; j < 5; j++) {
                StringBuilder builder = new StringBuilder();
                List<Triple<Integer, Integer, ForegroundColorSpan>> spans = new ArrayList<>();
                int charCount = 0;
                boolean first = false;
                for (RequestedCourses.LessonCourse course : courses.get(j).get(i)) {
                    if(!subs.isEmpty() && current < subs.size() && j == Util.dateFromString(subs.get(current).date).getDayOfWeek().ordinal()) {
                        //while (current < subs.size() - 1 && subs.get(current).lesson <= i) current++;
                        //while (current < subs.size() - 1 && subs.get(current).lesson == i + 1 && !subs.get(current).teacher.equals(course.teacher)) current++;
                        if (current < subs.size() && subs.get(current).lesson == i && subs.get(current).teacher.equals(course.teacher) && subs.get(current).getDayId() == j) {
                            int color = Util.isCanceled(subs.get(current)) ? 0xFFFF0000 : 0xFFFFFF00;
                            spans.add(new Triple<>(charCount, charCount + 3, new ForegroundColorSpan(color)));
                            current++;
                        }
                    }
                    if(first)builder.append('\n');
                    builder.append(course.teacher).append("  ");
                    charCount += 4;
                    first = true;
                }
                SpannableString span = new SpannableString(builder.toString());
                for (Triple<Integer, Integer, ForegroundColorSpan> spanColor : spans) {
                    span.setSpan(spanColor.component3(), spanColor.component1(), spanColor.component2(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                TextView view = new TextView(getContext());
                view.setText(span);
                view.setTextColor(MainActivity.textColor.toArgb());
                view.setOnTouchListener(new Listener(j, i));
                row.addView(view);
            }
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            table.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.getInstance().infoTable = false;
        binding = null;
    }
}
