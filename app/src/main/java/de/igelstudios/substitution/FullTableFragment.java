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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.igelstudios.substitution.databinding.FragmentFirstBinding;
import de.igelstudios.substitution.databinding.FragmentTableBinding;

public class FullTableFragment extends Fragment {
    public static class Listener implements View.OnTouchListener{
        private int day,lesson;

        public Listener(int day,int lesson){
            this.day = day;
            this.lesson = lesson;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && v instanceof TextView) {
                TextView textView = (TextView) v;

                // Get the touch Y position relative to the TextView
                int y = (int) event.getY();

                // Convert the touch position to a line number
                int lineNumber = textView.getLayout().getLineForVertical(y);

                MainActivity.getInstance().COURSES.toggle(day,lesson,lineNumber);
                v.performClick();
                return true; // Consume the event
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

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().settings = true;
        MainActivity.getInstance().getBinding().fab.hide();
        List<List<List<RequestedCourses.Course>>> courses = MainActivity.getInstance().COURSES.sorted_courses;

        /*for (int i = 0; i < 11; i++) {
            courses.add(new ArrayList<>());
            for (int j = 0; j < 5; j++) {
                courses.get(i).add(new ArrayList<>());
            }
        }

        List<RequestedCourses.Course> list = MainActivity.getInstance().COURSES.getFullList();
        for (RequestedCourses.Course course : list) {
            courses.get(course.lesson - 1).get(course.day - 1).add(course);
        }*/

        TableLayout table = MainActivity.getInstance().findViewById(R.id.sub_table);
        table.removeAllViews();
        TableRow header = new TableRow(this.getContext());
        TextView tv = new TextView(this.getContext());
        tv.setText("");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);

        tv = new TextView(this.getContext());
        tv.setText("Montag  ");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);
        tv = new TextView(this.getContext());
        tv.setText("Dienstag  ");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);
        tv = new TextView(this.getContext());
        tv.setText("Mitwoch  ");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);
        tv = new TextView(this.getContext());
        tv.setText("Donnerstag  ");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);
        tv = new TextView(this.getContext());
        tv.setText("Freitag  ");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);
        table.addView(header);

        for (int i = 0; i < 11; i++) {
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
                for (RequestedCourses.Course course : courses.get(i).get(j)) {
                    buffer/*.append(first?"\n":"")*/.append(course).append("\n");
                    if(course.selected)colors.add(new Pair<>(charID,charID + course.toString().length()));
                    charID += course.toString().length() + 1;
                    //first = true;
                }
                SpannableString span = new SpannableString(buffer.toString());
                for (Pair<Integer, Integer> color : colors) {
                    span.setSpan(new ForegroundColorSpan(0x00FF00),color.first,color.second,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                text.setText(buffer.toString());
                text.setTextColor(MainActivity.textColor.toArgb());

                text.setOnTouchListener(new Listener(j,i));

                row.addView(text);
            }
            table.addView(row);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.getInstance().getBinding().fab.show();
    }
}
