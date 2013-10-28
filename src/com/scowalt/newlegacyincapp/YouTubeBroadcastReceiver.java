package com.scowalt.newlegacyincapp;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
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

public class YouTubeBroadcastReceiver extends BroadcastReceiver {
	private static String TAG = "YouTubeBroadcastReceiver";
	private static final int MID = 124;

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, "onReceive() called");
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final boolean notify = prefs.getBoolean("youtube_notification", true);
		Log.d(TAG, "Notify = " + notify);
		if (!notify)
			return;

		final String previousVideoID = prefs.getString("_youtube_id", null);
		final String previousVideoDate = prefs.getString("_youtube_date", null);
		Log.d(TAG, "Previous video id = " + previousVideoID);
		Log.d(TAG, "Previous video date = " + previousVideoDate);
		final DateFormat df = new SimpleDateFormat(YouTubeParser.DATE_FORMAT);
		try {
			final Date previousDate;
			if (previousVideoDate == null)
				previousDate = getEarlyDate();
			else
				previousDate = df.parse(previousVideoDate);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						JSONObject json = YouTubeParser.getYouTubeList(context);
						JSONObject latest = YouTubeParser.getLatestVideo(json);
						String videoID = YouTubeParser.getVideoID(latest);
						Date date = YouTubeParser.getVideoPublishedDate(latest);
						Log.d(TAG, "Latest video id = " + videoID);
						Log.d(TAG, "Latest video date = " + date.toString());
						if (!(previousVideoID == null)
								&& !videoID.equals(previousVideoID)
								&& date.after(previousDate)) {
							String title = YouTubeParser.getVideoTitle(latest);
							serveNotification(context, title, videoID);
							Editor editor = prefs.edit();
							editor.putString("_youtube_id", videoID);
							editor.putString("_youtube_date", df.format(date));
							editor.commit();
						} else {
							Log.d(TAG, "No new video");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		} catch (ParseException e) {
			Log.e(TAG, "onReceive() ParseException");
			e.printStackTrace();
		}
	}

	private Date getEarlyDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(1970, 01, 01);
		return cal.getTime();
	}

	private void serveNotification(Context context, String title, String id) {
		Log.d(TAG, "New video release!");
		// http://stackoverflow.com/a/10184570/1222411
		Intent notificationIntent = MainActivity
				.youTubeVideoIntent(context, id);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.youtube_notification)
				.setContentTitle(
						"New " + MainActivity.YOUTUBE_USERNAME + " video!")
				.setContentText(title).setContentIntent(contentIntent)
				.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(MID, mBuilder.build());
	}
}
