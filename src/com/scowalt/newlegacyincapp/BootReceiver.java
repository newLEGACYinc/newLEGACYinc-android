package com.scowalt.newlegacyincapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * http://stackoverflow.com/a/13512226/1222411
 * @author scowa_000
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BOOT", "Detected boot");
		MainActivity.registerTwitchAlarm(context);
		MainActivity.registerYouTubeAlarm(context);
	}
}