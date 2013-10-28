package com.scowalt.newlegacyincapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		final Context c = this;

		Preference about = (Preference) findPreference("about_preference");
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// http://stackoverflow.com/a/6631310/1222411
				AlertDialog.Builder adb = new AlertDialog.Builder(c);
				adb.setCancelable(true);
				adb.setNegativeButton("Dismiss", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				TextView tv = new TextView(c);
				// http://stackoverflow.com/a/2000784/1222411
				final SpannableString s = new SpannableString(Html
						.fromHtml("<b>Created by: </b>Scott Walters<br />"
								+ "<b>Website: </b>scowalt.com"));
				Linkify.addLinks(s, Linkify.WEB_URLS);
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				tv.setText(s);
				tv.setTextSize(16);
				tv.setPadding(16, 16, 16, 16);
				Dialog d = adb.setView(tv).create();
				d.setTitle("About");
				d.show();
				return false;
			}
		});
	}
}
