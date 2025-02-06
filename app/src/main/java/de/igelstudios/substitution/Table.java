package de.igelstudios.substitution;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

public class Table {
    private Context context;

    public Table(Context context){
        this.context = context;
    }

    private void update(TableLayout table,List<Substitution> substitutions){
        table.removeAllViews();

        TableRow header = new TableRow(context);
        TextView tv = new TextView(context);
        tv.setText("     Std.  ");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);
        tv = new TextView(context);
        tv.setText("Betrift  ");
        tv.setTextColor(MainActivity.textColor.toArgb());
        header.addView(tv);
        tv = new TextView(context);
        tv.setTextColor(MainActivity.textColor.toArgb());
        tv.setText("Vertretung  ");
        header.addView(tv);
        tv = new TextView(context);
        tv.setTextColor(MainActivity.textColor.toArgb());
        tv.setText("Kurs  ");
        header.addView(tv);
        tv = new TextView(context);
        tv.setTextColor(MainActivity.textColor.toArgb());
        tv.setText("Raum  ");
        header.addView(tv);
        tv = new TextView(context);
        tv.setTextColor(MainActivity.textColor.toArgb());
        tv.setText("Info  ");
        header.addView(tv);
        table.addView(header);

        for (Substitution change : substitutions) {
            TableRow row = new TableRow(context);

            TextView text = new TextView(context);
            text.setText("     " + change.lesson + "  ");
            text.setTextColor(MainActivity.textColor.toArgb());
            row.addView(text);
            text = new TextView(context);
            text.setText(change.teacher + "  ");
            text.setTextColor(MainActivity.textColor.toArgb());
            row.addView(text);
            text = new TextView(context);
            text.setTextColor(MainActivity.textColor.toArgb());
            text.setText(change.teacher_new + "  ");
            row.addView(text);
            text = new TextView(context);
            text.setTextColor(MainActivity.textColor.toArgb());
            text.setText(change.course_new + "  ");
            row.addView(text);
            text = new TextView(context);
            text.setTextColor(MainActivity.textColor.toArgb());
            text.setText(change.room + "  ");
            row.addView(text);
            text = new TextView(context);
            text.setTextColor(MainActivity.textColor.toArgb());
            text.setText(change.info + "  ");
            row.addView(text);
            table.addView(row);

        }
    }

    public void updateShownTable(){
        update(MainActivity.getInstance().findViewById(R.id.sub_table),MainActivity.getInstance().COURSES.strip(MainActivity.getInstance().FETCHER.fetchLocal()));
    }

    public void updateWholeTable(){
        update(MainActivity.getInstance().findViewById(R.id.sub_table),MainActivity.getInstance().FETCHER.fetchLocal());
    }
}
