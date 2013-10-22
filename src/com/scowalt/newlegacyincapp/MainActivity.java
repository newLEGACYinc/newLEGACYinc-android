package com.scowalt.newlegacyincapp;

import com.google.android.youtube.player.YouTubeIntents;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "newLegacyInc";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setSelected(true); // needed for marquee to work

		final Context c = this;

		ImageView youtube = (ImageView) findViewById(R.id.youtube);
		youtube.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = YouTubeIntents.createUserIntent(c,
						"newLEGACYinc");
				startActivity(intent);
			}
		});

		ImageView twitch = (ImageView) findViewById(R.id.twitch);
		twitch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.twitch.tv/newlegacyinc/popout/"));
				startActivity(browserIntent);
			}
		});

		ImageView facebook = (ImageView) findViewById(R.id.facebook);
		facebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "Facebook has been clicked");
				Intent facebookIntent = getOpenFacebookIntent(c);
				Log.v(TAG, "Got facebook intent");
				startActivity(facebookIntent);
			}
		});

		ImageView tumblr = (ImageView) findViewById(R.id.tumblr);
		tumblr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://newlegacyinc.tumblr.com/"));
				startActivity(browserIntent);
			}
		});

		ImageView nlTwitter = (ImageView) findViewById(R.id.twitter);
		nlTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// http://stackoverflow.com/a/18695465/1222411
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("twitter://user?screen_name="
									+ "newLegacyInc")));
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("https://twitter.com/" + "newLegacyInc")));
				}
			}
		});
	}

	/**
	 * http://stackoverflow.com/a/10213314/1222411
	 * 
	 * @param context
	 * @return
	 */
	private static Intent getOpenFacebookIntent(Context context) {
		try {
			context.getPackageManager()
					.getPackageInfo("com.facebook.katana", 0);
			return new Intent(Intent.ACTION_VIEW,
					Uri.parse("fb://profile/100002106849705"));
		} catch (Exception e) {
			return new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://www.facebook.com/newlegacyinc"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
