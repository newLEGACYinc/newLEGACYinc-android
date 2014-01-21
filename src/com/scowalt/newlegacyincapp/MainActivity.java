package com.scowalt.newlegacyincapp;

import java.io.IOException;
import java.util.Date;

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
import twitter4j.URLEntity;
import twitter4j.conf.ConfigurationBuilder;

import com.google.android.youtube.player.YouTubeIntents;
import com.scowalt.newlegacyincapp.Constants.Reddit;
import com.scowalt.newlegacyincapp.Constants.Steam;
import com.scowalt.newlegacyincapp.Constants.Stream;
import com.scowalt.newlegacyincapp.Constants.Tumblr;
import com.scowalt.newlegacyincapp.Constants.YouTube;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "Main";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupSocialMediaButtons();

		Alarms.register(this);
	}

	protected void onResume() {
		super.onResume();

		refreshScreen(this);
	}

	private void updateLatestYouTube(final Context c) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final TextView description = (TextView) findViewById(R.id.youtube_description);
				try {
					JSONObject json = YouTubeParser.getYouTubeList(c);
					JSONObject latest = YouTubeParser.getLatestVideo(json);
					final String videoID = YouTubeParser.getVideoID(latest);
					final String titleText = YouTubeParser
							.getVideoTitle(latest);
					final Bitmap thumbnailImage = YouTubeParser
							.getVideoThumbNail(latest);
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							drawLatestYouTubeVideo(c, videoID, titleText,
									thumbnailImage);
						}

						private void drawLatestYouTubeVideo(final Context c,
								final String videoID, final String titleText,
								final Bitmap thumbnailImage) {
							ImageView thumbnail = (ImageView) findViewById(R.id.youtube_preview);
							OnClickListener l = new AnimatedOnClickListener() {
								@Override
								public void onClick(View v) {
									this.onClick(v, c);
									startActivity(youTubeVideoIntent(c, videoID));
								}
							};

							description.setText(titleText);
							thumbnail.setImageBitmap(thumbnailImage);

							LinearLayout layout = (LinearLayout) findViewById(R.id.youtube_preview_description_layout);
							layout.setOnClickListener(l);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					MainActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							description
									.setText("Unable to retrieve YouTube information");
						}
					});
				}
			}
		}).start();
	}

	/**
	 * Updates the latest tweet on the main activity screen
	 * 
	 * @param c
	 *            Context
	 */
	private void updateLatestTweet(final Context c) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Status latest = getLatestTweet(c);
				if (latest == null) {
					drawUnableToRetrieveTweet();
				} else {
					drawTweet(latest, c);
					popupPoll(c, latest);
				}
			}

			private void drawTweet(final Status latest, final Context c) {
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView tweet = (TextView) findViewById(R.id.tweet);
						tweet.setText(Html.fromHtml("<b>@"
								+ latest.getUser().getScreenName()
								+ ":</b><br />" + latest.getText()));
						Log.d(TAG, "Tweet id = " + latest.getId());
						tweet.setOnClickListener(new AnimatedOnClickListener() {
							@Override
							public void onClick(View v) {
								this.onClick(v, c);
								String url = "https://twitter.com/"
										+ com.scowalt.newlegacyincapp.Constants.Twitter.USERNAME
										+ "/status/" + latest.getId();
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
								.setText("Unable to retrieve Twitter information");
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
	private Status getLatestTweet(Context c) {
		Twitter twitter = setupTwitterFactory().getInstance();
		Query q = new Query("from:"
				+ com.scowalt.newlegacyincapp.Constants.Twitter.USERNAME + "");
		try {
			QueryResult result = twitter.search(q);
			if (result.getTweets().size() != 0) {
				java.util.List<Status> statuses = result.getTweets();
				for (Status status : statuses) {
					if (status.getInReplyToStatusId() == -1)
						return status;
				}
			} else {
				Log.e(TAG,
						"No statuses found for "
								+ com.scowalt.newlegacyincapp.Constants.Twitter.USERNAME);
			}
		} catch (TwitterException e) {
			Log.e(TAG, "getLatestTweet() TwitterException");
			e.printStackTrace();
		}
		return null;
	}

	private void popupPoll(Context c, Status status) {
		String TAG = "Main popupPoll()";

		// Check that tweet isn't reply or retweet
		if (status.getInReplyToStatusId() != -1 || status.isRetweet()) {
			return;
		}

		URLEntity[] urls = status.getURLEntities();
		for (URLEntity url : urls) {
			String expandedURL = url.getExpandedURL();
			String strawpoll = "strawpoll.me/";
			int index = expandedURL.indexOf(strawpoll);
			if (index != -1
					&& (index + strawpoll.length() != expandedURL.length())) {
				final SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(c);
				String previousURL = prefs.getString("_pollUrl", null);
				Date previousDate = new Date(prefs.getLong("_pollTime", 0));
				Date statusDate = status.getCreatedAt();
				Log.d(TAG,
						"previousDate < currentDate? "
								+ previousDate.before(statusDate));
				if (!expandedURL.equals(previousURL)
						&& previousDate.before(statusDate)) {
					showPollDialog(c, expandedURL);
					Editor editor = prefs.edit();
					editor.putString("_pollUrl", expandedURL);
					editor.putLong("_pollTime", statusDate.getTime());
					editor.commit();
				}
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void showPollDialog(final Context context, final String pollUrl) {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// http://stackoverflow.com/a/6631310/1222411
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				adb.setCancelable(true);
				adb.setNegativeButton("Dismiss",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				WebView wv = new WebView(context);
				wv.loadUrl(pollUrl);
				wv.getSettings().setJavaScriptEnabled(true);
				wv.setWebChromeClient(new WebChromeClient());
				wv.setWebViewClient(new WebViewClient() {
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						view.loadUrl(url);
						return false;
					}
				});
				Dialog d = adb.setView(wv).create();
				d.setTitle("Vote now!");
				d.show();
			}
		});

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

	private void updateStreamStatus(final Context context) {
		new Thread(new Runnable() {
			public void run() {
				JSONObject s;
				Constants.Stream mSource;
				try {
					s = twitchStatus();
					if (s == null) {
						s = hitboxStatus();
						mSource = Stream.HITBOX;
					} else {
						mSource = Stream.TWITCH;
					}
				} catch (Exception e) {
					Log.e(TAG, "updateStreamStatus Exception");
					s = null;
					mSource = null;
					e.printStackTrace();
				}
				final JSONObject stream = s;
				final Constants.Stream source = mSource;
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						drawStreamStatus(stream, source, context);
					}
				});
			}
		}).start();
	}

	/**
	 * NOTE: Only run in UI thread
	 * 
	 * @param stream
	 * @param source
	 * @param context
	 */
	private void drawStreamStatus(final JSONObject stream, Stream source,
			Context context) {
		final TextView tv = (TextView) findViewById(R.id.stream_status);
		final ImageView iv = (ImageView) findViewById(R.id.stream);
		if (stream == null) {
			tv.setText(Html.fromHtml("<b>" + Constants.Twitch.USERNAME
					+ " is <font color='red'>offline</font>!</b>"));
			return;
		}

		String game = "";
		try {
			if (source == Stream.TWITCH) {
				iv.setImageResource(R.drawable.twitch_logo);
				game = stream.get("game").toString();
				setupTwitchClickableLayout(context);
			} else if (source == Stream.HITBOX) {
				iv.setImageResource(R.drawable.hitbox_logo);
				game = stream.getString("category_name");
				setupHitboxclickableLayout(context);
			}
		} catch (JSONException e) {
			Log.e(TAG, "drawTwitchStatusText() JSONException");
			e.printStackTrace();
		}
		tv.setText(Html.fromHtml("<b>" + Constants.Twitch.USERNAME
				+ " is <font color='green'>online</font>!</b><br/>Playing: "
				+ game));

		// TODO Update picture if Hitbox
	}

	/**
	 * 
	 * @return JSONObject containing hitbox channel data or null for an offline
	 *         channel
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject hitboxStatus() throws IOException, JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpget = new HttpGet(Constants.Hitbox.requestUrl);
		HttpResponse response = null;

		try {
			response = httpclient.execute(httpget, localContext);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String str = EntityUtils.toString(entity);
				JSONObject json = new JSONObject(str);
				JSONArray channels = json.getJSONArray("livestream");
				for (int i = 0; i < channels.length(); i++) {
					JSONObject channel = (JSONObject) channels.get(i);
					String channelName = channel.getString("media_user_name");
					if (channelName.equals(Constants.Hitbox.USERNAME)) {
						return channel;
					}
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "ClientProtocol Exception error");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return JSONObject containing information about the stream that's online,
	 *         or null for an offline stream
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject twitchStatus() throws IOException, JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpget = new HttpGet("https://api.twitch.tv/kraken/streams/"
				+ Constants.Twitch.USERNAME + "?client_id="
				+ Constants.Twitch.CLIENT_ID);
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
			Log.e(TAG, "twitchStatus() ClientProtocolException");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Makes all of the social media buttons on the screen clickable
	 */
	private void setupSocialMediaButtons() {
		setupYoutubeButton(this);

		setupFacebookButton(this);

		setupTumblrButton(this);

		setupTwitterButton(this);

		setupSteamButton(this);

		setupRedditButton(this);
	}

	private void setupRedditButton(final Context c) {
		ImageView reddit = (ImageView) findViewById(R.id.reddit);
		reddit.setOnClickListener(new AnimatedOnClickListener() {
			@Override
			public void onClick(View v) {
				this.onClick(v, c);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(Reddit.URL)));
			}
		});
	}

	private void setupSteamButton(final Context c) {
		ImageView steam = (ImageView) findViewById(R.id.steam);
		steam.setOnClickListener(new AnimatedOnClickListener() {
			@Override
			public void onClick(View v) {
				this.onClick(v, c);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(Steam.GROUP_URL)));
			}
		});
	}

	private void setupYoutubeButton(final Context c) {
		ImageView youtube = (ImageView) findViewById(R.id.youtube_icon);
		youtube.setOnClickListener(new AnimatedOnClickListener() {
			@Override
			public void onClick(View v) {
				this.onClick(v, c);
				try {
					Intent intent = YouTubeIntents.createUserIntent(c,
							YouTube.USERNAME);
					startActivity(intent);
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("http://www.youtube.com/user/"
									+ YouTube.USERNAME)));
				}
			}
		});
	}

	private void setupTwitchClickableLayout(final Context c) {
		LinearLayout stream = (LinearLayout) findViewById(R.id.stream_layout);
		stream.setOnClickListener(new AnimatedOnClickListener() {
			public void onClick(View v) {
				this.onClick(v, c);
				Log.d(TAG, "Twitch onClick() called");
				Intent browserIntent = twitchIntent();
				startActivity(browserIntent);
			}
		});
	}

	private void setupHitboxclickableLayout(final Context c) {
		LinearLayout stream = (LinearLayout) findViewById(R.id.stream_layout);
		stream.setOnClickListener(new AnimatedOnClickListener() {
			public void onClick(View v) {
				this.onClick(v, c);
				Log.d(TAG, "Twitch onClick() called");
				Intent browserIntent = hitboxIntent();
				startActivity(browserIntent);
			}
		});
	}

	public static Intent twitchIntent() {
		return new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.twitch.tv/"
				+ Constants.Twitch.USERNAME + "/popout/"));
	}

	public static Intent hitboxIntent() {
		// TODO Auto-generated method stub
		return new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.Hitbox.url));
	}

	private void setupFacebookButton(final Context c) {
		ImageView facebook = (ImageView) findViewById(R.id.facebook);
		facebook.setOnClickListener(new AnimatedOnClickListener() {
			@Override
			public void onClick(View v) {
				this.onClick(v, c);
				Intent facebookIntent = getOpenFacebookIntent(c);
				Log.v(TAG, "Got facebook intent");
				startActivity(facebookIntent);
			}
		});
	}

	private void setupTumblrButton(final Context c) {
		ImageView tumblr = (ImageView) findViewById(R.id.tumblr);
		tumblr.setOnClickListener(new AnimatedOnClickListener() {
			@Override
			public void onClick(View v) {
				this.onClick(v, c);
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://" + Tumblr.USERNAME + ".tumblr.com/"));
				startActivity(browserIntent);
			}
		});
	}

	private void setupTwitterButton(final Context c) {
		ImageView nlTwitter = (ImageView) findViewById(R.id.twitter_image);
		nlTwitter.setOnClickListener(new AnimatedOnClickListener() {
			@Override
			public void onClick(View v) {
				this.onClick(v, c);
				// http://stackoverflow.com/a/18695465/1222411
				try {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("twitter://user?screen_name="
									+ com.scowalt.newlegacyincapp.Constants.Twitter.USERNAME)));
				} catch (Exception e) {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("https://twitter.com/"
									+ com.scowalt.newlegacyincapp.Constants.Twitter.USERNAME)));
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
					Uri.parse("https://www.facebook.com/"
							+ Constants.Facebook.USERNAME));
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
		case R.id.refresh:
			Toast.makeText(this, "Loading latest data...", Toast.LENGTH_SHORT)
					.show();
			refreshScreen(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshScreen(Context context) {
		Log.d(TAG, "Refreshing screen...");

		updateStreamStatus(context);

		updateLatestTweet(context);

		updateLatestYouTube(context);
	}

	private void openSettings(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		startActivity(intent);
	}

	public static Intent youTubeVideoIntent(Context c, String id) {
		if (YouTubeIntents.canResolvePlayVideoIntent(c)) {
			return YouTubeIntents.createPlayVideoIntent(c, id);
		}
		return new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://www.youtube.com/watch?v=" + id));
	}
}
