package com.scowalt.newlegacyincapp;

public final class Constants {
	public enum Stream {
		HITBOX, TWITCH
	}
	
	public class Reddit {

		static final String URL = "http://i.reddit.com/r/newlegacyinc";

	}

	public class Steam {

		static final String GROUP_URL = "http://steamcommunity.com/groups/newLEGACYinc";

	}

	public class Tumblr {

		static final String USERNAME = "newLEGACYinc";

	}

	public class Twitter {

		static final String USERNAME = "newLEGACYinc";

	}

	public final class Facebook {
		static final String USERNAME = "newLEGACYinc";
	}

	public final class Hitbox {
		static final String USERNAME = "newLEGACY";
		public static final int REQUEST_CODE = 122;
		public static final int ALARM_INTERVAL_MINUTES = 15;
		public static final int MID = 257;
		public static final String requestUrl = "http://api.hitbox.tv/media";
		public static final String url = "http://hitbox.tv/" + USERNAME;
	}

	public final class Twitch {
		static final String CLIENT_ID = "kvshv6jgxb43x9p3uz5q4josja9xsub";
		static final int ALARM_INTERVAL_MINUTES = 15;
		public static final String USERNAME = "newLEGACYinc";
		static final int REQUEST_CODE = 120;
		public static final int MID = 123;
	}

	public final class YouTube {
		static final int REQUEST_CODE = 121;
		public static final String USERNAME = "newLEGACYinc";
		static final long ALARM_INTERVAL_MINUTES = 60;
	}
}