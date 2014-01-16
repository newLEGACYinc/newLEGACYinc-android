package com.scowalt.newlegacyincapp;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
	private static final int MID = 123;

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, "onReceive() called");
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final boolean notify = prefs.getBoolean("twitch_notification", true);
		Log.d(TAG, "notify = " + notify);
		if (!notify)
			return;

		final boolean previouslyOnline = prefs.getBoolean(
				"_twitch_previouslyOnline", false);
		Log.d(TAG, "previouslyOnline = " + previouslyOnline);
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject stream;
				try {
					stream = MainActivity.twitchStatus();
				} catch (IOException e1) {
					Log.e(TAG, "Can't get twitch.tv status, won't do anything");
					e1.printStackTrace();
					return;
				}
				boolean currentlyOnline = stream != null;
				Log.d(TAG, "currentlyOnline = " + currentlyOnline);
				if (currentlyOnline && !previouslyOnline) {
					try {
						serveNotificaiton(context, stream.get("game")
								.toString());
					} catch (JSONException e) {
						Log.e(TAG, "JSONException");
						e.printStackTrace();
					}
				} else if (!currentlyOnline && previouslyOnline) {
					removeNotification(context);
				}
				if (currentlyOnline != previouslyOnline) {
					Editor editor = prefs.edit();
					editor.putBoolean("_twitch_previouslyOnline",
							currentlyOnline);
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
	 * 
	 * @param context
	 * @param game
	 */
	private void serveNotificaiton(Context context, String game) {
		Log.d(TAG, "STREAM ONLINE");
		// http://stackoverflow.com/a/10184570/1222411
		Intent notificationIntent = MainActivity.twitchIntent();
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		game = game.replaceAll("&#39;", "\'");

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.twitch_notification)
				.setContentTitle(Constants.Twitch.USERNAME + " is online!")
				.setContentText("Playing: " + game)
				.setContentIntent(contentIntent).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(MID, mBuilder.build());
	}
}