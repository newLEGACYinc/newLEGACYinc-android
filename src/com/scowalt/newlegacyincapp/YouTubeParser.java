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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * This class digs through the YouTube JSON so you don't have to!!!
 * 
 * @author srwalte2
 * 
 */
public class YouTubeParser {
	private static String TAG = "YouTubeParser";

	/**
	 * Returns a list of newLEGACYinc's YouTube videos in the YouTube JSON
	 * format
	 * 
	 * @param c
	 * @return
	 */
	public static JSONObject getYouTubeList(final Context c) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpget = new HttpGet(
				"http://gdata.youtube.com/feeds/api/users/"
						+ MainActivity.YOUTUBE_USERNAME + "/uploads?alt=json");
		HttpResponse response = null;

		try {
			response = httpclient.execute(httpget, localContext);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String str = EntityUtils.toString(entity);
				return new JSONObject(str);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e(TAG, "getYouTubeList() ClientProtocolException");
		} catch (IOException e) {
			Log.e(TAG, "getYouTubeList() IOException");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(TAG, "getYouTubeList() JSONException");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param json
	 *            YouTubeList
	 * @return JSON containing information about only the latest video
	 * @throws JSONException
	 */
	public static JSONObject getLatestVideo(JSONObject json)
			throws JSONException {
		JSONObject feeds = (JSONObject) json.get("feed");
		JSONArray entries = (JSONArray) feeds.get("entry");
		JSONObject latest = (JSONObject) entries.get(0);
		return latest;
	}

	public static String getVideoID(JSONObject video) throws JSONException {
		String idURL = ((JSONObject) video.get("id")).get("$t").toString();
		return idURL.substring(idURL.lastIndexOf("/") + 1);
	}

	public static String getVideoTitle(JSONObject video) throws JSONException {
		JSONObject title = (JSONObject) video.get("title");
		return title.get("$t").toString();
	}

	public static Bitmap getVideoThumbNail(JSONObject video)
			throws JSONException, IOException {
		JSONObject mediaGroup = (JSONObject) video.get("media$group");
		JSONArray thumbnails = (JSONArray) mediaGroup.get("media$thumbnail");
		JSONObject firstThumbnail = (JSONObject) thumbnails.get(0);
		String thumbnailUrlString = firstThumbnail.getString("url").toString();
		URL thumbnailUrl = new URL(thumbnailUrlString);
		return BitmapFactory.decodeStream(thumbnailUrl.openConnection()
				.getInputStream());
	}
}
