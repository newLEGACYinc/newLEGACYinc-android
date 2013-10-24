package com.scowalt.newlegacyincapp;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.android.youtube.player.YouTubeIntents;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {

	static final String PREFS_NAME = "nlPrefs";
	private static final String TAG = "newLegacyInc";
	private static final String TWITCH_CLIENT_ID = "kvshv6jgxb43x9p3uz5q4josja9xsub";
	private static final String TWITCH_USERNAME = "newLegacyInc";
	private static final String YOUTUBE_USERNAME = "newLEGACYinc";
	private static final String STEAM_GROUP_URL = "http://steamcommunity.com/groups/newLEGACYinc";
	private static final int REQUEST_CODE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupSocialMediaButtons();

		updateTwitchStatus();

		registerTwitchAlarm(this);

		updateLatestTweet();
	}

	private void updateLatestTweet() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Status latest = getLatestTweet();
				if (latest == null) {
					drawUnableToRetrieveTweet();
				} else {
					drawTweet(latest);
				}
			}

			private void drawTweet(final Status latest) {
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView tweet = (TextView) findViewById(R.id.tweet);
						tweet.setText(Html.fromHtml("<b>@"
								+ latest.getUser().getScreenName()
								+ ":</b><br />" + latest.getText()));

					}
				});
			}

			private void drawUnableToRetrieveTweet() {
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						((TextView) findViewById(R.id.tweet))
								.setText("Unable to retrieve twitter information");
					}
				});
			}
		}).start();
	}

	/**
	 * Returns latest tweet that isn't a reply to another tweet
	 * 
	 * @return
	 */
	private Status getLatestTweet() {
		Twitter twitter = setupTwitterFactory().getInstance();
		Query q = new Query("from:newlegacyinc");
		try {
			QueryResult result = twitter.search(q);
			if (result.getTweets().size() != 0) {
				List<Status> statuses = result.getTweets();
				Status latest = null;
				for (Status status : statuses) {
					if (status.getInReplyToStatusId() == -1)
						return status;
				}
				return latest;
			}
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * http://stackoverflow.com/a/16155107/1222411
	 * 
	 * @param context
	 */
	private void registerTwitchAlarm(Context context) {
		Intent i = new Intent(context, TwitchBroadcastReceiver.class);

		PendingIntent sender = PendingIntent.getBroadcast(context,
				REQUEST_CODE, i, 0);

		long firstTime = SystemClock.elapsedRealtime();
		firstTime += 3 * 1000;// start 3 seconds after first register.

		long interval = 20 * 60 * 1000; // 20 minute interval

		AlarmManager am = (AlarmManager) context
				.getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
				interval, sender);
	}

	private TwitterFactory setupTwitterFactory() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("7mO9mYc9bKfKJjEE0lZnQ")
				.setOAuthConsumerSecret(
						"Q8rhqjLDweZybwI4dr5fl8dOJEtmQxWLdehD8xEPCE")
				.setOAuthAccessToken(
						"256291657-Yvacx98m6NWk34BQLhrknI5xDFgLoLoqZ39nMAYQ")
				.setOAuthAccessTokenSecret(
						"2cI2neRWiqK07IBUrigiJ0FgZ85EYnLtZFHQfKO9N5Rxo");
		return new TwitterFactory(cb.build());
	}

	private void updateTwitchStatus() {
		new Thread(new Runnable() {
			public void run() {
				final JSONObject stream = twitchStatus();

				if (!(stream == null)) {
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							drawTwitchStatusText(stream);
						}
					});
				}
			}
		}).start();
	}

	/**
	 * NOTE: Only run in UI thread
	 * 
	 * @param stream
	 */
	private void drawTwitchStatusText(final JSONObject stream) {
		String game = "";
		try {
			game = stream.get("game").toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final LinearLayout all = (LinearLayout) findViewById(R.id.all);
		final TextView tv = new TextView(MainActivity.this);
		LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 3);
		tv.setLayoutParams(params);
		tv.setGravity(Gravity.CENTER);
		tv.setText("newLEGACYInc is live! Playing " + game);
		tv.setSingleLine();
		tv.setTextSize(20);
		tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
		tv.setMarqueeRepeatLimit(-1);
		tv.setSelected(true);
		all.addView(tv, 1);
	}

	/**
	 * @return JSONObject containing information about the stream that's online,
	 *         or null for an offline stream
	 */
	public static JSONObject twitchStatus() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpget = new HttpGet("https://api.twitch.tv/kraken/streams/"
				+ TWITCH_USERNAME + "?client_id=" + TWITCH_CLIENT_ID);
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpget, localContext);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String str = EntityUtils.toString(entity);
				JSONObject json = new JSONObject(str);
				final Object stream = json.get("stream");

				Log.v(TAG, stream.toString());
				if (stream.toString().equals("null"))
					return null;
				return (JSONObject) stream;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Makes all of the social media buttons on the screen clickable
	 */
	private void setupSocialMediaButtons() {
		setupYoutubeButton(this);

		setupTwitchButton();

		setupFacebookButton(this);

		setupTumblrButton();

		setupTwitterButton();

		setupSteamButton();
	}

	private void setupSteamButton() {
		ImageView steam = (ImageView) findViewById(R.id.steam);
		steam.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(STEAM_GROUP_URL)));
			}
		});
	}

	private void setupYoutubeButton(final Context c) {
		ImageView youtube = (ImageView) findViewById(R.id.youtube);
		youtube.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = YouTubeIntents.createUserIntent(c,
						YOUTUBE_USERNAME);
				startActivity(intent);
			}
		});
	}

	private void setupTwitchButton() {
		ImageView twitch = (ImageView) findViewById(R.id.twitch);
		twitch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent browserIntent = twitchIntent();
				startActivity(browserIntent);
			}
		});
	}

	private Intent twitchIntent() {
		return new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.twitch.tv/"
				+ TWITCH_USERNAME + "/popout/"));
	}

	private void setupFacebookButton(final Context c) {
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
	}

	private void setupTumblrButton() {
		ImageView tumblr = (ImageView) findViewById(R.id.tumblr);
		tumblr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://newlegacyinc.tumblr.com/"));
				startActivity(browserIntent);
			}
		});
	}

	private void setupTwitterButton() {
		ImageView nlTwitter = (ImageView) findViewById(R.id.twitter_image);
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
