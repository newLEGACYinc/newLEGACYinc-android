package com.scowalt.newlegacyincapp;

import com.scowalt.newlegacyincapp.receivers.TwitchBroadcastReceiver;
import com.scowalt.newlegacyincapp.receivers.YouTubeBroadcastReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class Alarms {
	private static final String TAG = "Alarms";

	public static void register(Context c) {
		registerTwitchAlarm(c);
		registerYouTubeAlarm(c);
	}
	
	/**
	 * http://stackoverflow.com/a/16155107/1222411
	 * 
	 * @param context
	 */
	private static void registerTwitchAlarm(Context context) {
		Intent i = new Intent(context, TwitchBroadcastReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(context,
				Constants.Twitch.REQUEST_CODE, i, 0);

		long firstTime = SystemClock.elapsedRealtime();
		firstTime += 3 * 1000;// start 3 seconds after first register.

		long interval = Constants.Twitch.ALARM_INTERVAL_MINUTES * 60 * 1000;

		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
				interval, sender);
	}

	private static void registerYouTubeAlarm(Context context) {
		Log.d(TAG, "Registering YouTube alarm...");
		Intent i = new Intent(context, YouTubeBroadcastReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(context,
				Constants.YouTube.REQUEST_CODE, i, 0);

		long firstTime = SystemClock.elapsedRealtime();
		firstTime += 3 * 1000;// start 3 seconds after first register.

		long interval = Constants.YouTube.ALARM_INTERVAL_MINUTES * 60 * 1000;

		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
				interval, sender);
	}

}
