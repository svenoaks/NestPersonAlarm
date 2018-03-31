package com.smp.nestpersonalarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
import static com.smp.nestpersonalarm.NestListenerService.ACTION_STOP;
import static com.smp.nestpersonalarm.Utility.isDndOverride;
import static com.smp.nestpersonalarm.Utility.isTurnedOn;
import static com.smp.nestpersonalarm.Utility.setDndOverride;
import static com.smp.nestpersonalarm.Utility.setTurnedOn;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
        createNotificationChannel();
    }

    public void onStopButton(View view) {
        Intent stopIntent = new Intent();
        stopIntent.setAction(ACTION_STOP);
        sendBroadcast(stopIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel("NEST PERSON ALARM", name, importance);
            mChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    public void setupUI() {
        ((SwitchCompat)findViewById(R.id.on_switch)).setChecked(isTurnedOn(this));
        ((SwitchCompat)findViewById(R.id.dnd_switch)).setChecked(isDndOverride(this));
        ((SwitchCompat)findViewById(R.id.dnd_switch)).setEnabled(isTurnedOn(this));

        ((SwitchCompat)findViewById(R.id.on_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(!isNotificationServiceEnabled()){
                        buildNotificationServiceAlertDialog().show();
                    }
                }
                setTurnedOn(MainActivity.this, isChecked);
                ((SwitchCompat)findViewById(R.id.dnd_switch)).setEnabled(isTurnedOn(MainActivity.this));
            }
        });
        ((SwitchCompat)findViewById(R.id.dnd_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isDndOverrideAccessGranted()) {
                    buildDndServiceAlertDialog().show();
                }
                setDndOverride(MainActivity.this, isChecked);
            }
        });
    }

    private AlertDialog buildDndServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.dnd_access);
        alertDialogBuilder.setMessage(R.string.dnd_access_explanation);
        alertDialogBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivity(intent);
                    }
                });
        alertDialogBuilder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

    private boolean isDndOverrideAccessGranted() {
       return ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).isNotificationPolicyAccessGranted();
    }


    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

}
