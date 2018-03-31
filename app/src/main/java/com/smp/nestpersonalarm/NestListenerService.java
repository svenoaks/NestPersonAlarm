package com.smp.nestpersonalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;
import static com.smp.nestpersonalarm.Utility.NOTIFICATION_ID;
import static com.smp.nestpersonalarm.Utility.isDndOverride;
import static com.smp.nestpersonalarm.Utility.isTurnedOn;

/**
 * Created by steve on 3/30/18.
 */

public class NestListenerService extends NotificationListenerService {
    public static final String ACTION_STOP = "nest stop alarm";

    private SoundPlayer player;

    private static final class ApplicationPackageNames {
        public static final String NEST_PACK_NAME = "com.nest.android";
    }

    public static final class InterceptedNotificationCode {
        public static final int NEST_CODE = 1;
        public static final int OTHER_NOTIFICATIONS_CODE = 4;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        registerCommandReceiver();
    }

    @Override
    public void onListenerDisconnected() {
        unregisterReceiver(commandReceiver);
        super.onListenerDisconnected();
    }

    private void registerCommandReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STOP);
        commandReceiver = new CommandReceiver();
        registerReceiver(commandReceiver, intentFilter);
    }
    private CommandReceiver commandReceiver;

    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ACTION_STOP) {
                if (player != null) {
                    player.stop();
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NestListenerService.this);
                    notificationManager.cancel(NOTIFICATION_ID);
                }
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode == InterceptedNotificationCode.NEST_CODE && isTurnedOn(this) && isPersonAlarm(sbn)){ //&& isPersonAlarm(sbn)) {
            if (player != null) {
                player.stop();
            }
            player = new SoundPlayer(this);
            if (isDndOverride(this)) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
            volumeMax();
            player.start();
            createNotification();
        }
    }

    private boolean isPersonAlarm(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;
        String title = (String) extras.get(Notification.EXTRA_TEXT);
        return title.toLowerCase().contains("person") || title.toLowerCase().contains("someone");

    }

    private void createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent stopIntent = new Intent();
        stopIntent.setAction(ACTION_STOP);
        stopIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        PendingIntent stopPendingIntent =
                PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "NEST PERSON ALARM")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stop_grey_600_24dp, getString(R.string.stop), stopPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void volumeMax() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), FLAG_REMOVE_SOUND_AND_VIBRATE);
    }


    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if(packageName.equals(ApplicationPackageNames.NEST_PACK_NAME)) {
            return(InterceptedNotificationCode.NEST_CODE);
        } else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }

    }
}
