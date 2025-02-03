package de.igelstudios.substitution;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Scheduler extends Worker{
    private static Scheduler scheduler;

    public Scheduler(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        scheduler = this;
    }

    @NonNull
    @Override
    public Result doWork() {
        MainActivity.requestPermissions();
        ConnectivityManager cm = (ConnectivityManager)this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if(isConnected && isWiFi) MainActivity.getInstance().NOTIFIER.notifieChanges(MainActivity.getInstance().FETCHER.fetch());
        return Result.success();
    }

    public static void schedule(Context context) {
        Calendar now = Calendar.getInstance();
        int minutesPastHour = now.get(Calendar.MINUTE);
        int secondsPastMinute = now.get(Calendar.SECOND);

        long initialDelay = (60 - minutesPastHour) * 60L - secondsPastMinute;

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(Scheduler.class, 1, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "hourly_task",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodicWorkRequest
        );
    }

    public static Scheduler getInstance(){
        return scheduler;
    }
}
