package com.scowalt.newlegacyincapp;

import com.google.android.youtube.player.YouTubeIntents;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
