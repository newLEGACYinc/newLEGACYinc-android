package com.scowalt.newlegacyincapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TwitchBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "TwitchBroadcastReceiver";
	private static int MID = 123;

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, "onReceive() called");
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final boolean notify = prefs.getBoolean("twitch_notification", true);
		Log.d(TAG, "notify = " + notify);
		if (!notify)
			return;

		final boolean previouslyOnline = prefs.getBoolean("previouslyOnline",
				false);
		Log.d(TAG, "previouslyOnline = " + previouslyOnline);
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject stream = MainActivity.twitchStatus();
				boolean currentlyOnline = stream != null;
				Log.d(TAG, "currentlyOnline = " + currentlyOnline);
				if (currentlyOnline && !previouslyOnline) {
					try {
						serveNotificaiton(context, stream.get("game")
								.toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (!currentlyOnline && previouslyOnline) {
					removeNotification(context);
				}
				if (currentlyOnline != previouslyOnline) {
					Editor editor = prefs.edit();
					editor.putBoolean("previouslyOnline", currentlyOnline);
					editor.commit();
					Log.d(TAG, "Online value is now: " + currentlyOnline);
				}
			}

		}).start();
	}

	private void removeNotification(Context context) {
		Log.d(TAG, "STREAM OFFLINE: Removing notification from statusbar");
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.cancel(MID);
	}

	/**
	 * https://developer.android.com/guide/topics/ui/notifiers/notifications.
	 * html#SimpleNotification
	 * 
	 * @param context
	 * @param game
	 */
	private void serveNotificaiton(Context context, String game) {
		Log.d(TAG, "STREAM ONLINE");
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.newlegacyinc_96)
				.setContentTitle("newLEGACYinc is online!")
				.setContentText("Playing: " + game);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// MID allows you to update the notification later on.
		mNotificationManager.notify(MID, mBuilder.build());
	}
}