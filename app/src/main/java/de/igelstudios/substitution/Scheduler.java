package de.igelstudios.substitution;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

import java.util.Calendar;

public class Scheduler extends BroadcastReceiver {
    private static Scheduler scheduler;

    public Scheduler() {
        scheduler = this;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.requestPermissions();
        if(MainActivity.isConnectedToWiFi())MainActivity.getInstance().FETCHER.fetch(MainActivity.getInstance().NOTIFIER::notifieChanges);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, Scheduler.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, newIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        switch (calendar.get(Calendar.HOUR_OF_DAY)) {
            case 6:
                calendar.set(Calendar.HOUR_OF_DAY,7);
                calendar.set(Calendar.MINUTE,0);
                break;
            case 7:
                if(calendar.get(Calendar.MINUTE) < 10)calendar.set(Calendar.MINUTE,30);
                else{
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.HOUR_OF_DAY,8);
                }
                break;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                calendar.set(Calendar.MINUTE,0);
                calendar.add(Calendar.HOUR_OF_DAY,1);
                break;
            case 17:
            default:
                calendar.add(Calendar.DAY_OF_YEAR,1);
                calendar.set(Calendar.MINUTE,30);
                calendar.set(Calendar.HOUR_OF_DAY,6);
                calendar.set(Calendar.AM_PM,0);
                calendar.set(Calendar.HOUR,6);
                break;
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
    }

    public static Calendar getCalendarBase(){
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        return now;
    }

    public static void scheduleHelper(Calendar base,AlarmManager alarmManager,PendingIntent intent){
        if(alarmManager == null)return;
        Calendar i = (Calendar) base.clone();
        base = Calendar.getInstance();
        base.setTimeInMillis(System.currentTimeMillis());
        base.set(Calendar.HOUR_OF_DAY,16);
        base.set(Calendar.MINUTE,36);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,base.getTimeInMillis(),AlarmManager.INTERVAL_DAY,intent);
        /*Calendar j = (Calendar) i.clone();
        j.add(Calendar.MINUTE,30);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,j.getTimeInMillis(),AlarmManager.INTERVAL_DAY,intent);
        for(int k = 0;k < 10;k++){
            i.add(Calendar.HOUR_OF_DAY,1);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,((Calendar) i.clone()).getTimeInMillis(),AlarmManager.INTERVAL_DAY,intent);
        }*/
    }

    public static void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Scheduler.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //Calendar calendar = Calendar.getInstance();

        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,0,AlarmManager.INTERVAL_DAY,pendingIntent);
        //scheduleHelper(getCalendarBase(),alarmManager,pendingIntent);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() - SystemClock.elapsedRealtime());

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,5000,AlarmManager.INTERVAL_DAY,pendingIntent);
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,5000,AlarmManager.INTERVAL_DAY,pendingIntent);
        }
        /*Calendar now = Calendar.getInstance();
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
        );*/
    }
}
