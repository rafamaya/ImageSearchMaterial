package com.rafamaya.imagesearch.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;

import com.rafamaya.imagesearch.R;

public class SplashScreenActivity  extends FragmentActivity{
	
	private static final int DELAY = 1000;

	public SplashScreenActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_splash_screen);
		
		//Wait 1 additonal second on the splash screen
		doInit(DELAY);
	}
	
	protected void startNextActivity() {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, null, "");
        ActivityCompat.startActivity(this, new Intent(this, MainSearchActivity.class),
                options.toBundle());

		this.finish();
	}

	protected void doInit(int delay) {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				startNextActivity();
			}
		}, delay);
	}
	
}
