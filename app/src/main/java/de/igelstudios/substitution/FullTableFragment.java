package de.igelstudios.substitution;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import de.igelstudios.substitution.databinding.FragmentTableBinding;

public class FullTableFragment extends Fragment {
    public class Listener implements View.OnTouchListener{
        private int line = -1;

        public Listener(int line){
            this.line = line;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && v instanceof TextView) {
                TextView textView = (TextView) v;

                if(MainActivity.getInstance().COURSES.allCourses.size() > line) {
                    MainActivity.getInstance().COURSES.toggle(MainActivity.getInstance().COURSES.allCourses.get(line));
                    FullTableFragment.this.update();
                }
                v.performClick();
                return true;
            }
            return false;
        }
    }

    private FragmentTableBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentTableBinding.inflate(inflater, container, false);

        binding.selAll.setOnClickListener(v -> {
            for (RequestedCourses.Course course : MainActivity.getInstance().COURSES.allCourses) {
                if(!course.selected){
                    MainActivity.getInstance().COURSES.add(course.id);
                    course.selected = true;
                }
            }

            for (RequestedCourses.LessonCourse lesson : MainActivity.getInstance().COURSES.lessons) {
                if(!MainActivity.getInstance().COURSES.selectedCourses.contains(lesson))MainActivity.getInstance().COURSES.selectedCourses.add(lesson);
            }
            FullTableFragment.this.update();
        });

        binding.selNone.setOnClickListener(v -> {
            for (RequestedCourses.Course course : MainActivity.getInstance().COURSES.allCourses) {
                if(course.selected){
                    MainActivity.getInstance().COURSES.remove(course.id);
                    course.selected = false;
                }
            }
            MainActivity.getInstance().COURSES.selectedCourses.clear();
            FullTableFragment.this.update();
        });
        return binding.getRoot();

    }


    public void update(){
        List<RequestedCourses.Course> courses = MainActivity.getInstance().COURSES.allCourses;

        TableLayout table = MainActivity.getInstance().findViewById(R.id.sub_table);
        table.removeAllViews();

        String lastSubject = "";
        for (int i = 0;i < courses.size();i++) {
            RequestedCourses.Course cours = courses.get(i);

            TableRow row = new TableRow(this.getContext());
            if(!lastSubject.equalsIgnoreCase(cours.subject.substring(1,cours.subject.length() -
                    (Character.isDigit(cours.subject.charAt(cours.subject.length() - 1))?1:0)))) {
                lastSubject = cours.subject.substring(1,cours.subject.length() -
                        (Character.isDigit(cours.subject.charAt(cours.subject.length() - 1))?1:0));
                TextView view = new TextView(this.getContext());
                view.setText("");
                row.addView(view);
                table.addView(row);
                row = new TableRow(this.getContext());
            }
                TextView view = new TextView(this.getContext());
                view.setText(cours.teacher + "  ");
                if (cours.selected) view.setTextColor(0xFF00FF00);
                row.addView(view);
                view.setOnTouchListener(new Listener(i));
                view = new TextView(this.getContext());
                view.setText(cours.subject);
                if (cours.selected) view.setTextColor(0xFF00FF00);
                view.setOnTouchListener(new Listener(i));
                row.addView(view);
            table.addView(row);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().fullTable = true;
        MainActivity.getInstance().getBinding().fab.hide();

        update();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.getInstance().fullTable = false;
        MainActivity.getInstance().getBinding().fab.show();
    }
}
