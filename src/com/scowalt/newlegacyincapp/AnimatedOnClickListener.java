package com.scowalt.newlegacyincapp;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;

public class AnimatedOnClickListener implements OnClickListener {
	private static String TAG = "AnimatedOnClickListener";

	public void onClick(View v, Context c) {
		Log.d(TAG, "Correct onClick() called");
		v.startAnimation(AnimationUtils.loadAnimation(c, R.anim.image_click));
	}

	@Override
	public void onClick(View v) {
		Log.e(TAG, "Invalid onClick() called");
	}

}
