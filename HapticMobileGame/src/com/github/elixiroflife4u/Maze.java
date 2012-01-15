package com.github.elixiroflife4u;

import android.app.Activity;
import android.os.Bundle;

public class Maze extends Activity{

	private MazeView mazeview;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
    	mazeview = new MazeView(this);
		mazeview.setBackgroundColor(0xFFFFFFFF);
		mazeview.setGridDim(8,14);
		setContentView(mazeview);
		
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
		{
			String value = extras.getString("level");
			System.out.println(value);
		}
	}
	
}
