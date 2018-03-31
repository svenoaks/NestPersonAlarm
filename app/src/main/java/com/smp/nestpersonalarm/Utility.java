package com.smp.nestpersonalarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by steve on 3/30/18.
 */

public class Utility {
    public static final int NOTIFICATION_ID = 478;
    public static boolean isTurnedOn(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("ALARM_TURNED_ON", false);
    }
    public static void setTurnedOn(Context context, boolean value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("ALARM_TURNED_ON", value);
        edit.apply();
    }

    public static boolean isDndOverride(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("DND_OVERRIDE_ON", false);
    }
    public static void setDndOverride(Context context, boolean value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("DND_OVERRIDE_ON", value);
        edit.apply();
    }
}
