package de.igelstudios.substitution;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FireBaseService extends FirebaseMessagingService {

    private static String token;

    static {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();

                        // Log and toast
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("substitution_info").addOnCompleteListener(task -> {
            if(!task.isSuccessful()) Toast.makeText(MainActivity.getInstance(),"Could not request updates",Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        PowerManager.WakeLock wakeLock = getSystemService(PowerManager.class).newWakeLock(PARTIAL_WAKE_LOCK, "substitution:messageReceived");
        try {
            wakeLock.acquire(100);
            if (!message.getData().isEmpty()) {
                List<Substitution> substitutions = new ArrayList<>();
                message.getData().forEach((k,v) -> {
                    String[] keySplit = k.split(",");
                    String[] valSplit = v.split(",");
                    if(keySplit.length == 3 && valSplit.length == 4){
                        substitutions.add(new Substitution(Integer.parseInt(keySplit[1]),keySplit[2],valSplit[1],valSplit[0],valSplit[3],valSplit[2],keySplit[0]));
                    }
                });

                MainActivity.getInstance().FETCHER.updateTable(substitutions);
            }
        } finally {
                wakeLock.release();
        }

    }

    public static String getToken() {
        return token;
    }

    @Override
    public void onNewToken(@NonNull String token) {
        FireBaseService.token = token;
    }
}
