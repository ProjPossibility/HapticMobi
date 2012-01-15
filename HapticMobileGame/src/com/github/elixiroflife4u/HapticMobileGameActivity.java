package com.github.elixiroflife4u;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class HapticMobileGameActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.splash);
    	Thread countdownSplash = new Thread() {
    		@Override 
    		public void run() {
    			try {
					sleep(2000);
				} catch (InterruptedException e) {
					// do nothing
				}
    			Intent intent = new Intent(HapticMobileGameActivity.this, Menu.class);
    	         startActivity(intent);      
    	         finish();
    		};
    	};
    	countdownSplash.start();
    }
}
