package de.igelstudios.substitution;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Scheduler extends Worker {
    private static Scheduler scheduler;

    public Scheduler(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        scheduler = this;
    }

    @NonNull
    @Override
    public Result doWork() {
        MainActivity.requestPermissions();
        /*ConnectivityManager cm = (ConnectivityManager)this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;*/
        //if(MainActivity.isConnectedToWiFi(this.getApplicationContext()))
            MainActivity.getInstance().NOTIFIER.notifieChanges(MainActivity.getInstance().FETCHER.fetch());
        return Result.success();
    }

    public static void schedule(Context context) {
        Calendar now = Calendar.getInstance();
        int minutesPastHour = now.get(Calendar.MINUTE);
        int secondsPastMinute = now.get(Calendar.SECOND);

        long initialDelay = (60 - minutesPastHour) * 60L - secondsPastMinute;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi network
                .build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(Scheduler.class, 1, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "hourly_task",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodicWorkRequest
        );

        /*AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Scheduler.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        /*long currentTime = System.currentTimeMillis();
        long nextHour = (currentTime / 3600000 + 1) * 3600000;*//*
        long nextHour = System.currentTimeMillis() + 5000;

        // Schedule the task every hour (on the hour)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextHour, pendingIntent);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextHour, 60000/*AlarmManager.INTERVAL_HOUR*//*, pendingIntent);*/
    }
}
