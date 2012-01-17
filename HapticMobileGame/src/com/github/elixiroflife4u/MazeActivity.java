package com.github.elixiroflife4u;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

// This class manages the MazeView which is the maze game itself
public class MazeActivity extends Activity implements OnTouchListener, SensorEventListener {

	private MazeView mazeview;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private WakeLock mWakeLock;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAccelerometer == null){ //should die here.
        	Log.v("Error", "Could not find an accelarometer.");
        }
        
        // Get an instance of the PowerManager
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        // Create a bright wake lock
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
        
        // read the requested difficulty level from the bundle sent by the main menu
		Bundle extras = getIntent().getExtras();
		int nx = 4, ny = 7;
		if (extras != null)
		{
			String difficulty = extras.getString("level");
			if (difficulty.equalsIgnoreCase("medium")) {
				nx = 6;
				ny = 11;
			}
			else if (difficulty.equalsIgnoreCase("difficult")) {
				nx = 8;
				ny = 14;
			}
		}
		
		// create a MazeView and initialize the game state
    	mazeview = new MazeView(this);
		mazeview.setBackgroundColor(0xFFFFFFFF);
		mazeview.setGridDim(nx,ny);
		mazeview.setBallPosition(0, 0);
		setContentView(mazeview);
		mazeview.setOnTouchListener(this);
	}

	public boolean onKeyDown(int keycode, KeyEvent event ) {
		// device's menu button toggles magnification state
		if(keycode == KeyEvent.KEYCODE_MENU) {
			System.err.println ("MENU PRESSED");
			mazeview.setMagnify();
		}
		return super.onKeyDown(keycode,event);  
	}

	public boolean onTouch(View v, MotionEvent event) {
		// touch handler for maze view
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			Log.v("maze", "touch x="+x+" y="+y);
			int w = v.getWidth();
			int h = v.getHeight();
			// 4 screen regions: upper, lower, left, right
			if (y <= h/4)
				mazeview.moveBallUp();
			else if (y >= 3*h/4)
				mazeview.moveBallDown();
			else if (x < w/2)
				mazeview.moveBallLeft();
			else
				mazeview.moveBallRight();
			return true;
		}
		return false;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// stop listening for accelerometer events
		mSensorManager.unregisterListener(this);
		// release our wake lock
		mWakeLock.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// start listening for accelerometer events
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		// acquire a wake lock
		mWakeLock.acquire();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// ignore
	}

	// last acceleration from sensor
	private float lastAccX, lastAccY;
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

		System.out.println("x: "+ (event.values[0])+"  y: "+ (event.values[1])+"  z: "+ (event.values[2]));
		float accx = event.values[0];
		float accy = event.values[1];
		
		final float thresh = 3.f;
		final float thresh2 = 2.7f;
		
		// if acceleration is larger than a threshold, 
		// and the previous acceleration was lower than a threshold
		// then send the appropriate ball move command to the maze view
		if (accx > thresh && Math.abs(lastAccX) < thresh2)
			mazeview.moveBallLeft();
		else if (accx < -thresh && Math.abs(lastAccX) < thresh2)
			mazeview.moveBallRight();
		if (accy > thresh && Math.abs(lastAccY) < thresh2)
			mazeview.moveBallDown();
		else if (accy < -thresh && Math.abs(lastAccY) < thresh2)
			mazeview.moveBallUp();
		
		// save acceleration values
		lastAccX = accx;
		lastAccY = accy;
	}
	
	public void won()
	{
		// game over: maze completed
		// go back to the main menu
		Intent intent = new Intent(MazeActivity.this, Menu.class);
        startActivity(intent);      
        finish();
	}	
}