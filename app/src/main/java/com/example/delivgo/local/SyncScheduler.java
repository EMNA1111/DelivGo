package com.example.delivgo.local;

import android.content.Context;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SyncScheduler {

    public static void planifier(Context context) {
        Calendar now = Calendar.getInstance();
        Calendar minuit = Calendar.getInstance();
        minuit.set(Calendar.HOUR_OF_DAY, 0);
        minuit.set(Calendar.MINUTE, 0);
        minuit.set(Calendar.SECOND, 0);
        minuit.set(Calendar.MILLISECOND, 0);

        if (minuit.before(now)) {
            minuit.add(Calendar.DAY_OF_MONTH, 1);
        }

        long delai = minuit.getTimeInMillis() - now.getTimeInMillis();

        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setInitialDelay(delai, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(syncWork);
    }
}