package com.github.elixiroflife4u;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

	    	// REGISTER ALL BUTTONS:
	    	final Button startButton = (Button) findViewById(R.id.start);
	    	final Button easyButton = (Button) findViewById(R.id.easy);
	    	final Button mediumButton = (Button) findViewById(R.id.medium);
	    	final Button difficultButton = (Button) findViewById(R.id.difficult);
	    	
	    	// START BUTTON:
	         startButton.setOnClickListener(new View.OnClickListener() { 
	        	 // If clicked, start game 
	        	 public void onClick(View v) {
	                 Intent intent = new Intent (Menu.this, Maze.class);
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
		                 easyButton.setBackgroundColor(colorSelect);
		                 easyButton.setTextColor(colorTextSelect);
		                 
		                 //return other buttons to normal
		                 mediumButton.setBackgroundColor(colorNorm);
		                 mediumButton.setTextColor(colorTextNorm);
		                 difficultButton.setBackgroundColor(colorNorm);
		                 difficultButton.setTextColor(colorTextNorm);
		             }
		         });
		    
	         // MEDIUM BUTTON:
		    mediumButton.setOnClickListener(new View.OnClickListener() {
		        // If clicked, turn medium button red and return others to normal     
		    	public void onClick(View v) {
		                 level = "medium";
		                 mediumButton.setBackgroundColor(colorSelect);
		                 mediumButton.setTextColor(colorTextSelect);
		                 
		                 //return other buttons to normal
		                 easyButton.setBackgroundColor(colorNorm);
		                 easyButton.setTextColor(colorTextNorm);
		                 difficultButton.setBackgroundColor(colorNorm);
		                 difficultButton.setTextColor(colorTextNorm);
		                 
		             }
		         });
		     
		    // DIFFICULT BUTTON: 
		    difficultButton.setOnClickListener(new View.OnClickListener() {
		        // If clicked, turn difficult button red and return others to normal     
		    	public void onClick(View v) {
		                 level = "difficult";
		                 difficultButton.setBackgroundColor(colorSelect);
		                 difficultButton.setTextColor(colorTextSelect);
		                 
		                 //return other buttons to normal
		                 easyButton.setBackgroundColor(colorNorm);
		                 easyButton.setTextColor(colorTextNorm);
		                 mediumButton.setBackgroundColor(colorNorm);
		                 mediumButton.setTextColor(colorTextNorm);
		                 
		             }
		         });
	 }
}
