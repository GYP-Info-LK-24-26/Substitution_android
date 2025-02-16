package de.igelstudios.substitution;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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
        tv.setText(/*"     "*/"Std.  ");
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

        if(substitutions.isEmpty()){
            TableRow row = new TableRow(context);
            TextView text = new TextView(context);
            text.setText(/*"     "*/"---------");
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
            text.setText("Nichts");
            text.setTextColor(MainActivity.textColor.toArgb());
            row.addView(text);
            table.addView(row);
        }

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
                text.setText(/*"     "*/"---------");
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
            text.setText(/*"     " + */change.lesson + "  ");
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

        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        MainActivity.getInstance().getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        table.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        double width = table.getMeasuredWidth();*/
        applySizeChanges(table);
        System.out.println("test");
    }

    ///the table size is multiplied by this to prevent the table from overflowing to the sides
    private static final double TABLE_MAGIC = 1.0532420183881735509245913817721884697675704956055d;

    public void applySizeChanges(TableLayout table){
        table.setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        MainActivity.getInstance().getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        table.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        double width = table.getMeasuredWidth() * TABLE_MAGIC;
        /*int[] sizes = new int[2];
        table.getLocationOnScreen(sizes);*/
        double dif = displayMetrics.widthPixels / width;
        if(dif >= 1.0d)return;
        for (int i = 0; i < table.getChildCount(); i++) {
            if(table.getChildAt(i) instanceof TableRow){
                for (int j = 0; j < ((TableRow) table.getChildAt(i)).getChildCount(); j++) {
                    if(!(((TableRow) table.getChildAt(i)).getChildAt(j) instanceof TextView))continue;
                    TextView view = (TextView) ((TableRow) table.getChildAt(i)).getChildAt(j);
                    view.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float) (view.getTextSize() * dif));
                    //view.setTextSize();
                }
            }
        }
    }

    public int getMaxTableRowWidth(TableLayout tableLayout) {
        int maxWidth = 0;

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int rowWidth = child.getMeasuredWidth();
                maxWidth = Math.max(maxWidth, rowWidth);
            }
        }

        return maxWidth;
    }

    public void updateShownTable(){
        update(MainActivity.getInstance().findViewById(R.id.sub_table),MainActivity.getInstance().COURSES.strip(MainActivity.getInstance().FETCHER.fetchLocal()));
    }

    public void updateWholeTable(){
        update(MainActivity.getInstance().findViewById(R.id.sub_table),MainActivity.getInstance().FETCHER.fetchLocal());
    }
}
