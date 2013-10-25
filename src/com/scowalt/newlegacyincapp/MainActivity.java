package com.scowalt.newlegacyincapp;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "newLEGACYinc";
	private static final String TWITCH_CLIENT_ID = "kvshv6jgxb43x9p3uz5q4josja9xsub";
	private static final String TWITCH_USERNAME = "newLEGACYinc";
	private static final long TWITCH_ALARM_INTERVAL_MINUTES = 15;
	private static final String YOUTUBE_USERNAME = "newLEGACYinc";
	private static final String STEAM_GROUP_URL = "http://steamcommunity.com/groups/newLEGACYinc";
	private static final int REQUEST_CODE = 0;
	private static final String REDDIT_URL = "http://i.reddit.com/r/newlegacyinc";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupSocialMediaButtons();

		registerTwitchAlarm(this);

		updateTwitchStatus();

		updateLatestTweet();

		updateLatestYouTube();
	}

	private void updateLatestYouTube() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				HttpGet httpget = new HttpGet(
						"http://gdata.youtube.com/feeds/api/users/"
								+ YOUTUBE_USERNAME + "/uploads?alt=json");
				HttpResponse response = null;

				try {
					response = httpclient.execute(httpget, localContext);

					HttpEntity entity = response.getEntity();
					if (entity != null) {
						String str = EntityUtils.toString(entity);
						JSONObject json = new JSONObject(str);
						JSONObject feeds = (JSONObject) json.get("feed");
						JSONArray entries = (JSONArray) feeds.get("entry");
						JSONObject latest = (JSONObject) entries.get(0);
						JSONObject title = (JSONObject) latest.get("title");
						final String titleText = title.get("$t").toString();
						Log.d(TAG, "titleText = " + titleText);
						JSONObject mediaGroup = (JSONObject) latest
								.get("media$group");
						JSONArray thumbnails = (JSONArray) mediaGroup
								.get("media$thumbnail");
						JSONObject firstThumbnail = (JSONObject) thumbnails
								.get(0);
						String thumbnailUrlString = firstThumbnail.getString(
								"url").toString();
						Log.d(TAG, "thumbnailUrlString = " + thumbnailUrlString);
						URL thumbnailUrl = new URL(thumbnailUrlString);
						final Bitmap thumbnailImage = BitmapFactory
								.decodeStream(thumbnailUrl.openConnection()
										.getInputStream());
						MainActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								((TextView) findViewById(R.id.youtube_description))
										.setText(titleText);
								((ImageView) findViewById(R.id.youtube_preview))
										.setImageBitmap(thumbnailImage);
							}
						});
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

			}
		}).start();
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
						Log.d(TAG, "Tweet id = " + latest.getId());
						tweet.setOnClickListener(new OnClickListener() {
							// http://wiki.akosma.com/IPhone_URL_Schemes#Twitter
							@Override
							public void onClick(View v) {
								String url = "https://twitter.com/newLEGACYinc/status/"
										+ latest.getId();
								Intent i = new Intent(Intent.ACTION_VIEW);
								i.setData(Uri.parse(url));
								startActivity(i);
							}
						});
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
		Query q = new Query("from:newLEGACYinc");
		try {
			QueryResult result = twitter.search(q);
			if (result.getTweets().size() != 0) {
				java.util.List<Status> statuses = result.getTweets();
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

		long interval = TWITCH_ALARM_INTERVAL_MINUTES * 60 * 1000;

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
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						drawTwitchStatusText(stream);
					}
				});
			}
		}).start();
	}

	/**
	 * NOTE: Only run in UI thread
	 * 
	 * @param stream
	 */
	private void drawTwitchStatusText(final JSONObject stream) {
		final TextView tv = (TextView) findViewById(R.id.twitch_status);
		if (stream == null) {
			tv.setText(Html
					.fromHtml("<b>newLEGACYinc is <font color='red'>offline</font>!</b>"));
			return;
		}

		String game = "";
		try {
			game = stream.get("game").toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tv.setText(Html
				.fromHtml("<b>newLEGACYinc is <font color='green'>online</font>!</b><br/>Playing: "
						+ game));
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

		setupTwitchClickableLayout();

		setupFacebookButton(this);

		setupTumblrButton();

		setupTwitterButton();

		setupSteamButton();

		setupRedditButton();
	}

	private void setupRedditButton() {
		ImageView reddit = (ImageView) findViewById(R.id.reddit);
		reddit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(REDDIT_URL)));
			}
		});
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
				try {
					Intent intent = YouTubeIntents.createUserIntent(c,
							YOUTUBE_USERNAME);
					startActivity(intent);
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("http://www.youtube.com/user/"
									+ YOUTUBE_USERNAME)));
				}
			}
		});
	}

	private void setupTwitchClickableLayout() {
		LinearLayout twitch = (LinearLayout) findViewById(R.id.twitch_layout);
		twitch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Twitch onClick() called");
				Intent browserIntent = twitchIntent();
				startActivity(browserIntent);
			}
		});
	}

	protected static Intent twitchIntent() {
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
						.parse("http://newLEGACYinc.tumblr.com/"));
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
									+ "newLEGACYinc")));
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("https://twitter.com/" + "newLEGACYinc")));
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
					Uri.parse("https://www.facebook.com/newLEGACYinc"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			openSettings(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openSettings(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		startActivity(intent);
	}
}
