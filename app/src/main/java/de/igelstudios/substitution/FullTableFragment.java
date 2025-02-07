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
        return binding.getRoot();

    }


    public void update(){
        List<RequestedCourses.Course> courses = MainActivity.getInstance().COURSES.allCourses;

        TableLayout table = MainActivity.getInstance().findViewById(R.id.sub_table);
        table.removeAllViews();


        for (int i = 0;i < courses.size();i++) {
            RequestedCourses.Course cours = courses.get(i);
            TableRow row = new TableRow(this.getContext());
            TextView view = new TextView(this.getContext());
            view.setText(cours.teacher + "  ");
            if(cours.selected)view.setTextColor(0xFF00FF00);
            row.addView(view);
            view.setOnTouchListener(new Listener(i));
            view = new TextView(this.getContext());
            view.setText(cours.subject);
            if(cours.selected)view.setTextColor(0xFF00FF00);
            view.setOnTouchListener(new Listener(i));
            row.addView(view);
            table.addView(row);
        }

        /*for (int i = 0; i < 11; i++) {
            TableRow row = new TableRow(this.getContext());

            TextView text = new TextView(this.getContext());
            text.setText(" " + (i + 1) + "  ");
            text.setTextColor(MainActivity.textColor.toArgb());
            row.addView(text);

            for (int j = 0; j < 5; j++) {
                text = new TextView(this.getContext());
                //boolean first = false;
                StringBuilder buffer = new StringBuilder();
                List<Pair<Integer,Integer>> colors = new ArrayList<>();
                int charID = 0;
                for (RequestedCourses.LessonCourse course : courses.get(i).get(j)) {
                    buffer.append(course).append("\n");
                    if(course.selected)colors.add(new Pair<>(charID,charID + course.toString().length()));
                    charID += course.toString().length() + 1;
                }
                SpannableString span = new SpannableString(buffer.toString());
                for (Pair<Integer, Integer> color : colors) {
                    span.setSpan(new ForegroundColorSpan(0xFF00FF00),color.first,color.second,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                text.setText(span);

                text.setOnTouchListener(new Listener(j,i));

                row.addView(text);
            }
            table.addView(row);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().settings = true;
        MainActivity.getInstance().getBinding().fab.hide();

        update();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.getInstance().getBinding().fab.show();
    }
}
