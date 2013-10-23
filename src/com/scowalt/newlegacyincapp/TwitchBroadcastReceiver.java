package com.scowalt.newlegacyincapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TwitchBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "TwitchBroadcastReceiver";
	private static boolean previouslyOnline = false;
	private static int MID = 123;

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, "onReceive() called");
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject stream = MainActivity.twitchStatus();
				boolean currentlyOnline = stream != null;
				if (currentlyOnline && !previouslyOnline) {
					try {
						serveNotificaiton(context, stream.get("game")
								.toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (currentlyOnline != previouslyOnline) {
					previouslyOnline = currentlyOnline;
				}
			}
		}).start();
	}

	private void serveNotificaiton(Context context, String game) {
		Log.d(TAG, "STREAM ONLINE");
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.newlegacyinc_96)
				.setContentTitle("newLegacyInc is online!")
				.setContentText("Playing: " + game);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(MID, mBuilder.build());
	}
}