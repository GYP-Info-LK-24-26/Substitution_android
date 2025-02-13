package de.igelstudios.substitution;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class Table {
    private Context context;
    public MutableLiveData<List<Substitution>> liveData = new MutableLiveData<>();

    public Table(Context context){
        this.context = context;
        liveData.observe(MainActivity.getInstance(),(data) -> this.updateShownTable());
    }

    private void update(TableLayout table,List<Substitution> substitutions){
        substitutions.sort((first,second) -> {
            if(first.date.equals(second.date))return Integer.compare(first.lesson,second.lesson);
            return Util.dateFromString(first.date).compareTo(Util.dateFromString(second.date));
        });
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

        String date = "";
        boolean first = false;
        for (Substitution change : substitutions) {
            if(!change.date.equals(date)){
                date = change.date;
                TableRow row;
                TextView text;
                if(first) {
                    row = new TableRow(context);
                    text = new TextView(context);
                    text.setText(" ");
                    row.addView(text);
                    table.addView(row);
                }
                first = true;
                row = new TableRow(context);
                text = new TextView(context);
                text.setText("     ---------");
                text.setTextColor(MainActivity.textColor.toArgb());
                row.addView(text);
                text = new TextView(context);
                text.setText("------------");
                text.setTextColor(MainActivity.textColor.toArgb());
                row.addView(text);
                text = new TextView(context);
                text.setTextColor(MainActivity.textColor.toArgb());
                text.setText("-------------------");
                row.addView(text);
                text = new TextView(context);
                text.setTextColor(MainActivity.textColor.toArgb());
                text.setText("------------");
                row.addView(text);
                text = new TextView(context);
                text.setTextColor(MainActivity.textColor.toArgb());
                text.setText("----------");
                row.addView(text);
                text = new TextView(context);
                text.setText(date);
                text.setTextColor(MainActivity.textColor.toArgb());
                row.addView(text);
                table.addView(row);
            }
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
