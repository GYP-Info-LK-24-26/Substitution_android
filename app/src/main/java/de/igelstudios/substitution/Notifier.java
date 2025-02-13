package de.igelstudios.substitution;

import static android.icu.number.NumberRangeFormatter.with;
import static androidx.core.content.ContextCompat.getSystemService;

import static de.igelstudios.substitution.MainActivity.getInstance;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class Notifier {
    private NotificationChannel channel;
    private static final String CHANNEL_ID = "IGSN_CHANNEL";
    private Context context;

    public Notifier(Context context){
        this.context = context;
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        CharSequence name = "Substitution table";//getString(R.string.channel_name);
        String description = "Updates from the substitution table";//getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = getSystemService(context,NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void notifieChanges(List<Substitution> changes){
        FirstFragment.locked = false;
        Snackbar.make(MainActivity.getInstance().getView(),"Finished Loading", BaseTransientBottomBar.LENGTH_SHORT).show();
        //FirstFragment.instance.binding.reloadBtn.setEnabled(true);

        if(changes == null){
            if(Config.get().isDebug())notifySimple("No changes in courses");
            return;
        }
        changes = MainActivity.getInstance().COURSES.strip(changes);
        if(changes.isEmpty()){
            if(Config.get().isDebug())notifySimple("No changes in courses");
            return;
        }
        MainActivity.requestPermissions();
        MainActivity.getInstance().SUBSTITUTION_TABLE.liveData.postValue(null);

        Intent intent = new Intent(MainActivity.getInstance(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("Vertretungs Plan informationen")
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText( toText(changes)))
                .build();


        NotificationManagerCompat compat = NotificationManagerCompat.from(this.context);

        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                Build.VERSION.SDK_INT >= 32) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.POST_NOTIFICATIONS},0);
        }
        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.ACCESS_NETWORK_STATE},0);
        }

        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.INTERNET},0);
        }
        compat.notify(0/*TODO replace with actual id*/,notification);
    }

    public void notifySimple(String text){
        MainActivity.requestPermissions();

        Intent intent = new Intent(MainActivity.getInstance(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("Vertretungs Plan informationen")
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManagerCompat compat = NotificationManagerCompat.from(this.context);

        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                Build.VERSION.SDK_INT >= 32) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.POST_NOTIFICATIONS},0);
        }
        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.ACCESS_NETWORK_STATE},0);
        }

        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.INTERNET},0);
        }
        compat.notify(1/*TODO replace with actual id*/,notification);
    }

    public String toText(List<Substitution> changes){
        StringBuilder builder = new StringBuilder();
        builder.append("Es gibt neuigkeiten zu ").append(changes.size()).append(" Stunde" + (changes.size() == 1?"":"n") + "\n");
        for (Substitution change : changes) {
            builder.append(change.lesson).append(":").append(change.teacher);
            if(change.teacher_new != null && !change.teacher_new.isEmpty())
                builder.append("->").append(change.teacher_new);
            if(change.course_new != null && !change.course_new.isEmpty())
                builder.append(" mit ").append(change.course_new);
            if(change.room != null && !change.room.isEmpty())
                builder.append(" in ").append(change.room);
            builder.append(" am ").append(change.date);
            builder.append("  ").append(change.info).append('\n');
        }

        return builder.toString();
    }

    public void askUpdate(){
        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                Build.VERSION.SDK_INT >= 32) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.POST_NOTIFICATIONS},0);
        }
        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.ACCESS_NETWORK_STATE},0);
        }

        if (ActivityCompat.checkSelfPermission(getInstance().getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.INTERNET},0);
        }


        Intent intent = getInstance().UPDATER.update();
        if(intent == null)return;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("Update")
                .setContentText("Tap to update Substitution")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).build();

        NotificationManagerCompat compat = NotificationManagerCompat.from(this.context);
        compat.notify(2/*TODO replace with actual id*/,notification);
    }
}
