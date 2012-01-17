package com.github.elixiroflife4u;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

public class MazeView extends View {
	// specifies if magnification is on or off. default is on
	private boolean mIsMagnified = false;
	// 2D array of maze cells
	private Cell mCells[][];
	// maze grid dimensions
	private int mNumCellsX = 1;
	private int mNumCellsY = 1;
	// cell size in pixels
	private float mCellSize;
	// maze background color
	private int mBGColor = 0xFF000000;
	// color of maze end marker
	private int mMazeEndMarkerColor = 0xFFFF0000;
	// grid (maze) origin w.r.t. view
	private float mGridOriginX, mGridOriginY;
	// maze wall color
	private int mWallColor = 0xFF00FF00;
	// maze wall thickness in pixels
	private float mWallThickness = 2.0f;
	// discrete ball coordinates
	// (which cell contains the center of the ball)
	private int mBallCellX, mBallCellY;
	// ball movement animation state
	private enum Dir {NONE,NORTH,SOUTH,EAST,WEST};
	private Dir mBallMotion = Dir.NONE;
	private float mBallMotionPct;
	// ball foreground color
	private int mBallColor = 0xFFFFFFFF;
	// pre-rendered BG and ball bitmaps
	Bitmap mBGBitmap = null, mBallBitmap = null;
	// ball velocity
	float mBallVX, mBallVY;
	// vibrator and durations
	Vibrator mVibrator;
	final int LONG_VIBRATION = 112;
	final int SHORT_VIBRATION = 41;
	
	// constructor
	MazeView(Context ctx) {
		super(ctx);
		mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		// cells are always square -> find the max cell size that will fit
		float cellw = (float)w / mNumCellsX;
		float cellh = (float)h / mNumCellsY;
		mCellSize = (float) Math.floor(Math.min(cellw, cellh));
		
		// origin of maze grid w.r.t. view
		mGridOriginX = 0.5f * (w - mCellSize * mNumCellsX);
		mGridOriginY = 0.5f * (h - mCellSize * mNumCellsY);
		
		// set the ball 
		setBallPosition(mBallCellX, mBallCellY);
		
		// clear the ball bitmaps so they will be regenerated
		mBallBitmap = null;
		mBGBitmap = null;
	}
	
	// set magnification state
	public void setMagnify(boolean isMagnified)
	{
		if (mIsMagnified != isMagnified) {
			mIsMagnified = isMagnified;
			invalidate();
		}
	}
	
	// toggles magnification state
	public void setMagnify()
	{
		mIsMagnified = !mIsMagnified;
		invalidate();
	}
	
	// set maze grid dimensions
	public void setGridDim(int cellsAcross, int cellsDown) {
		if (cellsAcross < 1 || cellsDown < 1)
			return;
		
		mNumCellsX = cellsAcross;
		mNumCellsY = cellsDown;
		
		// regenerate maze
		Maze maze = new Maze(mNumCellsY, mNumCellsX);
		maze.generateMaze();
		mCells = maze.getMaze();
		
		invalidate();
	}
	
	public void setWallColor(int color) {
		mWallColor = color;
		invalidate();
	}
	
	public void setBGColor(int color) {
		mBGColor = color;
		invalidate();
	}
	
	public void setWallThickness(float thickness) {
		if (thickness >= 1.0f) {
			mWallThickness = thickness;
			invalidate();
		}
	}
	
	public void setBallPosition(int cellX, int cellY) {
		// clip to valid range
		if (cellX < 0) cellX = 0; if (cellX >= mNumCellsX) cellX = mNumCellsX - 1;
		if (cellY < 0) cellY = 0; if (cellY >= mNumCellsY) cellY = mNumCellsY - 1;
		
		// discrete ball position
		mBallCellX = cellX;
		mBallCellY = cellY;

		mBallMotion = Dir.NONE;
		mBallMotionPct = 0.f;
		
		invalidate();
	}
	
	private void ballFailedMove(Dir attemptedDir, boolean border)
	{
		// ball attempted to move but was blocked by a wall
		Log.v("ball", "Ball failed move dir=" + attemptedDir + " border=" + border);
		
		mVibrator.vibrate(LONG_VIBRATION);
	}
	
	private void ballMovedToWall(boolean border)
	{
		// ball successfully moved and hit a wall (possibly border)
		Log.v("ball", "Ball moved to wall, border="+border);
		
		mVibrator.vibrate(SHORT_VIBRATION);
	}
	
	private void ballMotionComplete(Dir lastDir)
	{
		// animation callback when ball motion completed
		Log.v("ball", "Ball completed move dir=" + lastDir);
		
		// update ball cell position
		switch (lastDir) {
		case NORTH: mBallCellY--; break;
		case SOUTH: mBallCellY++; break;
		case EAST:  mBallCellX++; break;
		case WEST:  mBallCellX--; break;
		}
		
		// reset ball motion state
		mBallMotion = Dir.NONE;
		mBallMotionPct = 0.f;
		
		// check for maze completion
		if (mBallCellX == mNumCellsX-1 && mBallCellY == mNumCellsY-1) {
			// maze complete!
			Log.v("ball", "MAZE COMPLETE!");
			long pattern[] = {2*LONG_VIBRATION,2*LONG_VIBRATION,2*LONG_VIBRATION,2*LONG_VIBRATION,2*LONG_VIBRATION,2*LONG_VIBRATION};
			mVibrator.vibrate(pattern, -1);
			// call MazeActivity callback here
			((MazeActivity) this.getContext()).won();
			return;
		}
		
		// check for wall collisions
		boolean collision = true;
		switch (lastDir) {
		case NORTH:
			if (mBallCellY <= 0)
				ballMovedToWall(true);
			else if (mCells[mBallCellY][mBallCellX].northWall)
				ballMovedToWall(false);
			else
				collision = false;
			break;
		case SOUTH:
			if (mBallCellY >= mNumCellsY-1)
				ballMovedToWall(true);
			else if (mCells[mBallCellY+1][mBallCellX].northWall)
				ballMovedToWall(false);
			else
				collision = false;
			break;
		case EAST:
			if (mBallCellX >= mNumCellsX-1)
				ballMovedToWall(true);
			else if (mCells[mBallCellY][mBallCellX+1].westWall)
				ballMovedToWall(false);
			else
				collision = false;
			break;
		case WEST:
			if (mBallCellX <= 0)
				ballMovedToWall(true);
			else if (mCells[mBallCellY][mBallCellX].westWall)
				ballMovedToWall(false);
			else
				collision = false;
			break;
		}
		
		// successful move
		if (!collision)
			mVibrator.vibrate(SHORT_VIBRATION);
	}
	
	public void moveBallLeft()
	{
		if (mBallMotion != Dir.NONE)
			return;
		
		// check west wall of current cell
		if (mBallCellX <= 0)
			ballFailedMove(Dir.WEST, true);
		else if (mCells[mBallCellY][mBallCellX].westWall)
			ballFailedMove(Dir.WEST, false);
		else {
			// start ball motion animation
			mBallMotion = Dir.WEST;
			mBallMotionPct = 0.f;
			invalidate();
		}
	}
	
	public void moveBallRight()
	{
		if (mBallMotion != Dir.NONE)
			return;
		
		// check west wall of right neighbor cell
		if (mBallCellX >= mNumCellsX-1)
			ballFailedMove(Dir.EAST, true);
		else if (mCells[mBallCellY][mBallCellX+1].westWall)
			ballFailedMove(Dir.EAST, false);
		else {
			// start ball motion animation
			mBallMotion = Dir.EAST;
			mBallMotionPct = 0.f;
			invalidate();
		}
	}
	
	public void moveBallUp()
	{
		if (mBallMotion != Dir.NONE)
			return;
		
		// check north wall of current cell
		if (mBallCellY <= 0)
			ballFailedMove(Dir.NORTH, true);
		else if (mCells[mBallCellY][mBallCellX].northWall)
			ballFailedMove(Dir.NORTH, false);
		else {
			// start ball motion animation
			mBallMotion = Dir.NORTH;
			mBallMotionPct = 0.f;
			invalidate();
		}
	}
	
	public void moveBallDown()
	{
		if (mBallMotion != Dir.NONE)
			return;
		
		// check west wall of right neighbor cell
		if (mBallCellY >= mNumCellsY-1)
			ballFailedMove(Dir.SOUTH, true);
		else if (mCells[mBallCellY+1][mBallCellX].northWall)
			ballFailedMove(Dir.SOUTH, false);
		else {
			// start ball motion animation
			mBallMotion = Dir.SOUTH;
			mBallMotionPct = 0.f;
			invalidate();
		}
	}
	
	// render maze background and ball into bitmaps
	private void renderMazeBitmaps()
	{
		int cellsize = (int) Math.round(mCellSize);
		mBGBitmap = Bitmap.createBitmap(mNumCellsX*cellsize, mNumCellsY*cellsize, Bitmap.Config.ARGB_8888);
		Canvas bgCanvas = new Canvas(mBGBitmap);
		
		// draw maze background (may not fill entire screen)
		Paint paint = new Paint();
		paint.setColor(mBGColor);
		bgCanvas.drawRect(0, 0, mCellSize*mNumCellsX, mCellSize*mNumCellsY, paint);
		
		// draw cell walls
		int ix, iy;
		float left, top, right, btm;
		float halfWallThickness = 0.5f * mWallThickness;
		paint.setColor(mWallColor);
		
		for (ix = 0; ix < mNumCellsX; ix++) {
			for (iy = 0; iy < mNumCellsY; iy++) {
				float x = mCellSize * ix;
				float y = mCellSize * iy;
				Cell c = mCells[iy][ix];
				if (iy > 0 && c.northWall) {
					// horizontal wall
					left = x;
					right = x + mCellSize;
					top = y - halfWallThickness;
					btm = y + halfWallThickness;
					bgCanvas.drawRect(left, top, right, btm, paint);
				}
				if (ix > 0 && c.westWall) {
					// vertical wall
					top = y;
					btm = y + mCellSize;
					left = x - halfWallThickness;
					right = x + halfWallThickness;
					bgCanvas.drawRect(left, top, right, btm, paint);
				}
			}
		}

		// draw end marker
		Path path = new Path();
		bgCanvas.translate((mNumCellsX-0.5f)*mCellSize, (mNumCellsY-0.5f)*mCellSize);
		float d = mCellSize/4.f;
		path.moveTo(-d, 0.f);
		path.lineTo(d, 0.f);
		path.lineTo(0.f, d);
		path.close();
		paint.setColor(mMazeEndMarkerColor);
		paint.setStyle(Paint.Style.FILL);
		bgCanvas.drawPath(path, paint);
		
		// draw ball into its own bitmap
		mBallBitmap = Bitmap.createBitmap(cellsize, cellsize, Bitmap.Config.ARGB_8888);
		Canvas ballCanvas = new Canvas(mBallBitmap);
		paint.setAntiAlias(true);
		paint.setColor(mBallColor);				// 0.45 instead of 0.5 to be slightly smaller than cell
		float radius = 0.5f*(float)cellsize;
		ballCanvas.drawCircle(radius, radius, 0.45f * mCellSize, paint);
	}
	
	// recorded start time of ball motion animation
	private long mAnimStartTime;
	
	@Override
	protected void onDraw(Canvas canvas) {
		// compute ball coordinates in pixels
		float mBallX = mBallCellX * mCellSize;
		float mBallY = mBallCellY * mCellSize;
		
		// check if ball is being animated
		if (mBallMotion != Dir.NONE) {
			// add offset for ball motion
			float delta = mBallMotionPct * mCellSize;
			Log.v("ballanim", "Animation " + (mBallMotionPct*100.f) + "%  offset=" + delta);
			
			if (mBallMotion == Dir.EAST)
				mBallX += delta;
			else if (mBallMotion == Dir.WEST)
				mBallX -= delta;
			else if (mBallMotion == Dir.NORTH)
				mBallY -= delta;
			else if (mBallMotion == Dir.SOUTH)
				mBallY += delta;
			
			// motion animation is done
			if (mBallMotionPct >= 1.f)
				ballMotionComplete(mBallMotion);
		}
		
		// compute time since last draw
		long curTime = System.currentTimeMillis();
		if (mBallMotionPct <= 0.f)
			mAnimStartTime = curTime - 50;
		
		float dt =  (float)(curTime - mAnimStartTime);

		// if we are animating the ball, update the animation parameter
		if (mBallMotion != Dir.NONE) {
			mBallMotionPct = dt / 100.f;	// complete motion animation takes 500 ms
			Log.v("ballanim", "delta-t="+dt+" now at "+(100.f*mBallMotionPct)+"%");
			if (mBallMotionPct > 1.f)
				mBallMotionPct = 1.f;
			invalidate();
		}
		
		// draw normal unmagnified view
		if (!mIsMagnified)
		{
			// render maze and ball bitmaps if necessary
			if (mBGBitmap == null || mBallBitmap == null)
				renderMazeBitmaps();
			
			// draw maze and ball
			Paint paint = new Paint();
			canvas.drawBitmap(mBGBitmap, mGridOriginX, mGridOriginY, paint);
			canvas.drawBitmap(mBallBitmap, mGridOriginX+mBallX, mGridOriginY+mBallY, paint);
		}
		// draw magnified view
		else {
			// black background
			Paint paint = new Paint();
			paint.setColor(0xFF000000);
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
			
			// Calculate intervals: coordinate range ==> cell number
			int intervalX = (int) canvas.getWidth()/mNumCellsX;
			int intervalY = (int) canvas.getHeight()/mNumCellsY;
			Log.v("maze", "intervalx = " + intervalX + "  intervaly = " + intervalY + "  X=" + (mGridOriginX+mBallX) + "  Y=" + (mGridOriginY+mBallY));
			// Get cells to display
			int cell_x1 = (int) (((mGridOriginX+mBallX)/intervalX)+0.5);
			// draw only consecutive vertical two cells
			int cell_y1 = (int) (((mGridOriginY+mBallY)/intervalY)+0.5);
			int cell_y2 = cell_y1 + 1;
			Log.v("maze", "x1 " + cell_x1 + "  y1 " + cell_y1 + "  mBallY=" + mBallY);
			
			if (cell_y2 >= mNumCellsY || cell_y1 >= mNumCellsY) {
				cell_y1 = mNumCellsY - 1;
				cell_y2 = mNumCellsY - 2;
			}
			
			Cell c1 = mCells[cell_y1][cell_x1];
			Cell c2 = mCells[cell_y2][cell_x1];
			
			// draw ball
			paint.setColor(0xFFFFFFFF);
						
			if ((mBallY/mCellSize) <= cell_y1) {
				Log.v("maze", "TOP");
				canvas.drawCircle(canvas.getWidth()/2, (canvas.getHeight()/4f), 0.35f * mCellSize * 4, paint);
			}
			else {
				Log.v("maze", "BOTTOM");
				canvas.drawCircle(canvas.getWidth()/2, (canvas.getHeight()/0.75f), 0.35f * mCellSize * 4, paint);
			}
			paint.setColor(0XFFFFFFFF);
			paint.setStrokeWidth(53.5f);
			
			// Display cell walls
			if (c1.northWall)
				canvas.drawLine(0, 0, canvas.getWidth(), 0, paint);
			if (c1.eastWall)
				canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight()/2, paint);
			if (c1.southWall)
				canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, paint);
			if (c1.westWall)
				canvas.drawLine(0, 0, 0, canvas.getHeight()/2, paint);
			
			// bottom of screen cell
			if (c2.northWall)
				canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, paint);
			if (c2.eastWall)
				canvas.drawLine(canvas.getWidth(), canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight(), paint);
			if (c2.southWall)
				canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), paint);
			if (c2.westWall)
				canvas.drawLine(0, canvas.getHeight()/2, 0, canvas.getHeight(), paint);
		} // end else case for magnified drawing
	} // end onDraw method
} // end class MazeView