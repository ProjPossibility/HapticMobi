package com.github.elixiroflife4u;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

public class Menu extends Activity{
	
	// default level is easy
	private String level = "easy";
	private int colorSelect = 0xFFFF0000;
	private int colorNorm = 0xFFFFFFFF;
	private int colorTextSelect = 0xFFFFFFFF;
	private int colorTextNorm = 0xff000000;

	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.menu);
	    	// Get instance of Vibrator from current Context
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

			// Vibrate 3 short bursts
		
			long[] pattern = {300,200,200,200,200,200,200};
			v.vibrate(pattern,-1);

	    	// REGISTER ALL BUTTONS:
	    	final Button startButton = (Button) findViewById(R.id.start);
	    	final Button easyButton = (Button) findViewById(R.id.easy);
	    	final Button mediumButton = (Button) findViewById(R.id.medium);
	    	final Button difficultButton = (Button) findViewById(R.id.difficult);
	    	
	    	//default level is easy:
	    	easyButton.setSelected(true); 
	    	
	    	// START BUTTON:
	         startButton.setOnClickListener(new View.OnClickListener() { 
	        	 // If clicked, start game 
	        	 public void onClick(View v) {
	                 Intent intent = new Intent (Menu.this, MazeActivity.class);
	                 // to pass level param to the maze activity
	                 intent.putExtra("level", level);
	                 startActivity(intent);  
	                 finish();
	             }
	         });
	         
	         // EASY BUTTON:
	         easyButton.setOnClickListener(new View.OnClickListener() {
		         // If clicked, turn easy button red and return others to normal    
	        	 public void onClick(View v) {
		                 level = "easy";
		                 easyButton.setSelected(true);

		                 // return other buttons to normal
		                 mediumButton.setSelected(false);
		                 difficultButton.setSelected(false);
		                 
		             }
		         });
		    
	         // MEDIUM BUTTON:
		    mediumButton.setOnClickListener(new View.OnClickListener() {
		        // If clicked, turn medium button red and return others to normal     
		    	public void onClick(View v) {
		                 level = "medium";
		                 mediumButton.setSelected(true);

		                 // return other buttons to normal
		                 easyButton.setSelected(false);
		                 difficultButton.setSelected(false);
		                 
		             }
		         });
		     
		    // DIFFICULT BUTTON: 
		    difficultButton.setOnClickListener(new View.OnClickListener() {
		        // If clicked, turn difficult button red and return others to normal     
		    	public void onClick(View v) {
		                 level = "difficult";
		                 difficultButton.setSelected(true);

		                 // return other buttons to normal
		                 easyButton.setSelected(false);
		                 mediumButton.setSelected(false);
		                 
		             }
		         });
	 }
}
