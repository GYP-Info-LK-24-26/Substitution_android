package de.igelstudios.substitution;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MidnightClearer extends Worker {
    public MidnightClearer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //i don't fucking know why but it works and this has probably never been called
        //MainActivity.getInstance().FETCHER.cleanOld();

        MainActivity.getInstance().NOTIFIER.askUpdate();
        return Result.success();
    }

    public static void schedule(Context context) {
        Calendar now = Calendar.getInstance();
        int hoursPastDay = now.get(Calendar.HOUR_OF_DAY);
        int minutesPastHour = now.get(Calendar.MINUTE);
        int secondsPastMinute = now.get(Calendar.SECOND);

        long initialDelay = (23 - hoursPastDay) * 3600 + (59 - minutesPastHour) * 60L - secondsPastMinute;

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MidnightClearer.class, 1, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_task",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodicWorkRequest
        );
    }
}
