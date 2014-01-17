package com.scowalt.newlegacyincapp.receivers;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.scowalt.newlegacyincapp.Constants;
import com.scowalt.newlegacyincapp.MainActivity;
import com.scowalt.newlegacyincapp.R;

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

public class HitboxBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "HitboxBroadcastReceiver";

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, "onReceive() called");
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final boolean notify = prefs.getBoolean("hitbox_notification", true);
		Log.d(TAG, "notify = " + notify);
		if (!notify)
			return;

		final boolean previouslyOnline = prefs.getBoolean(
				"_hitbox_previouslyOnline", false);
		Log.d(TAG, "previouslyOnline = " + previouslyOnline);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject channel = MainActivity.hitboxStatus();
					final boolean currentlyOnline = channel != null;
					Log.d(TAG, "currentlyOnline = " + currentlyOnline);
					if (currentlyOnline && !previouslyOnline) {
						serveNotification(context, channel);
					} else if (!currentlyOnline && previouslyOnline) {
						removeNotification(context);
					}
					if (currentlyOnline != previouslyOnline) {
						Editor editor = prefs.edit();
						editor.putBoolean("_hitbox_previouslyOnline",
								currentlyOnline);
						editor.commit();
						Log.d(TAG, "Online value is now: " + currentlyOnline);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void serveNotification(Context context, JSONObject channel)
			throws JSONException {
		Log.d(TAG, "STREAM ONLINE");

		Intent notificationIntext = MainActivity.hitboxIntent();
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntext, PendingIntent.FLAG_CANCEL_CURRENT);

		final String game = channel.getString("category_name");

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.newlegacyinc_small)
				.setContentTitle("newLEGACYinc is online!")
				.setContentText("Playing: " + game)
				.setContentIntent(contentIntent).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(Constants.Hitbox.MID, mBuilder.build());
	}

	private void removeNotification(Context context) {
		// TODO Auto-generated method stub
		Log.d(TAG, "STREAM OFFLINE: Removing notification");
		NotificationManager mNotificationMananger = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationMananger.cancel(Constants.Hitbox.MID);
	}
}
