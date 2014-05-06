package com.marvell.tv.epg.app;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.marvell.tv.epg.EventDescriptor;
import com.marvell.tv.epg.app.EPGStore.ScheduledEvent;

public class BootReceiver extends BroadcastReceiver {
    private static final boolean DBG = true;
    private static final String TAG = "EPGBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            installScheduledEvents(context);
        }
    }

    /**
     * install valid scheduled events after system boot up
     *
     * @return the number of events newly installed
     */
    private int installScheduledEvents(Context context) {
        if (DBG) {
            Log.d(TAG, "installScheduledEvents() in");
        }
        int installedNum = 0;
        // TODO:
        if (context == null) {
            return installedNum;
        }
        ContentResolver resolver = context.getContentResolver();
        ArrayList<ScheduledEvent> events = EPGStore.getScheduledEvents(resolver, true, null, null);
        if (events == null) {
            return installedNum;
        } else {
            for (ScheduledEvent event : events) {
                    EventDescriptor eventDescriptor = EPGStore.getEvent(resolver, event.channelNum, event.startTime);
                    if (eventDescriptor == null) {
                        continue;
                    }
                    long startTime = event.startTime;
                    long endTime = eventDescriptor.getEndTime();
                    String channelNum = event.channelNum;
                    int scheType = event.scheType;
                    String title = eventDescriptor.getTitle();
                    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    Intent schedualIntent = new Intent(EPGMain.ACTION_SCHEDUAL_EVENT);
                    schedualIntent.putExtra(EPGMain.KEY_EXTRA_SCHEDUAL_TYPE, scheType);
                    schedualIntent.putExtra(EPGMain.KEY_EXTRA_SCHEDUAL_START_TIME, startTime);
                    schedualIntent.putExtra(EPGMain.KEY_EXTRA_SCHEDUAL_END_TIME, endTime);
                    schedualIntent.putExtra(EPGMain.KEY_EXTRA_SCHEDUAL_CHANNEL, channelNum);
                    schedualIntent.putExtra(EPGMain.KEY_EXTRA_SCHEDUAL_EVENT_TITLE, title);

                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, schedualIntent, 0);
                    am.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                            EPGMain.SCHEDUAL_DELAY, pi);
                    installedNum++;

                    if (DBG) {
                        String schedualTypeString = null;
                        if (scheType == EPGMain.SCHEDUAL_TYPE_REMIND) {
                            schedualTypeString = "Remind program";
                        } else if (scheType == EPGMain.SCHEDUAL_TYPE_RECORD) {
                            schedualTypeString = "Record program";
                        }

                        String playRangle = null;
                        String startTimeString = DateFormat.getTimeInstance().format(new Date(startTime * 1000));
                        String endTimeString = DateFormat.getTimeInstance().format(new Date(endTime * 1000));
                        playRangle = startTimeString + "-" + endTimeString;
                        Toast.makeText(context, "Registerred " + schedualTypeString + " \"" + title + "\" @ channel " + channelNum + " [" + playRangle + "] :)", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        return installedNum;
    }
}
