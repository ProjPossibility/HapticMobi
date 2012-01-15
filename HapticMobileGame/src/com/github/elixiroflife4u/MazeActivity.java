package com.github.elixiroflife4u;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MazeActivity extends Activity implements OnTouchListener {

	private MazeView mazeview;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
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
		
    	mazeview = new MazeView(this);
		mazeview.setBackgroundColor(0xFFFFFFFF);
		mazeview.setGridDim(nx,ny);
		mazeview.setBallPosition(0, 0);
		setContentView(mazeview);
		
		mazeview.setOnTouchListener(this);
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			float x = event.getX();
			float y = event.getY();
			Log.v("maze", "touch x="+x+" y="+y);
			
			if (y < 200.f)
				mazeview.shiftBallUp(8.f);
			else if (y > 500.f)
				mazeview.shiftBallDown(8.f);
			else if (x < 200.f)
				mazeview.shiftBallLeft(8.f);
			else
				mazeview.shiftBallRight(8.f);
			return true;
		}
		return false;
	}
	
}
