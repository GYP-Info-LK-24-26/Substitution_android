package de.igelstudios.substitution;

import static android.icu.number.NumberRangeFormatter.with;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
        changes = MainActivity.getInstance().COURSES.strip(changes);
        if(changes.isEmpty())return;
        MainActivity.requestPermissions();
        MainActivity.getInstance().SUBSTITUTION_TABLE.updateShownTable();
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("Vertretungs Plan informationen")
                //.setContentText("Es gibt neuigkeiten zu " + changes.size() + " stunden")
                //.setLargeIcon(emailObject.getSenderAvatar())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText( toText(changes)))
                .build();


        NotificationManagerCompat compat = NotificationManagerCompat.from(this.context);
        compat.notify(0/*TODO replace with actual id*/,notification);
    }

    public void notifySimple(String text){
        MainActivity.requestPermissions();
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("Vertretungs Plan informationen")
                .setContentText(text)
                .build();
        NotificationManagerCompat compat = NotificationManagerCompat.from(this.context);
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
            builder.append("  ").append(change.info).append('\n');
        }

        return builder.toString();
    }
}
