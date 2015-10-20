package com.locname.distribution;

/**
 * Created by Mostafa on 9/6/2015.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.SystemClock;
public class GpsTrackerBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);



            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                     10000, // 60000 = 1 minute,
                    pendingIntent);

            //alarmManager.cancel(pendingIntent);


        // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
        // REPEAT_TIME, pending);
    }
}