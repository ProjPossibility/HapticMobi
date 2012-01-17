package com.github.elixiroflife4u;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;

// Entry point into app
public class HapticMobileGameActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	// show splash screen for 1 second
    	setContentView(R.layout.splash);
    	Thread countdownSplash = new Thread() {
    		@Override 
    		public void run() {
    			try {
    				// Get instance of Vibrator from current Context
    				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    			
    				// Vibrate for 1 second
    				v.vibrate(1000);
    				// wait for vibration:
    				sleep(1000);
    				
				} catch (InterruptedException e) {
					// do nothing
				}
    			// show main menu when done
    			Intent intent = new Intent(HapticMobileGameActivity.this, Menu.class);
    	        startActivity(intent);      
    	        finish();
    		};
    	};
    	countdownSplash.start();
    }
}