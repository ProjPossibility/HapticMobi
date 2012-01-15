package com.github.elixiroflife4u;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class MazeView extends View {

	private boolean magnify = false; // specifies if magnification is on or off. default is on
	private Cell mCells[][];
	private int mNumCellsX = 1;
	private int mNumCellsY = 1;
	private float mCellSize;
	// maze background color
	private int mBGColor = 0xFF503010;
	// grid (maze) origin w.r.t. view
	private float mGridOriginX, mGridOriginY;
	// maze wall color
	private int mWallColor = 0xFFFFFFFF;
	// maze wall thickness in pixels
	private float mWallThickness = 2.0f;
	// pixel coordinates of ball center w.r.t. maze rect
	private float mBallX = 0.f, mBallY = 0.f;
	// discretized ball coordinates
	// (which cell contains the center of the ball)
	private int mBallCellX, mBallCellY;
	// ball offset within its current cell
	private enum Dir {NONE,NORTH,SOUTH,EAST,WEST};
	private Dir mBallCellOffset = Dir.NONE;
	// ball foreground color
	private int mBallColor = 0xFF00FFFF;
	
	MazeView(Context ctx) {
		super(ctx);
	}
	
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		// cells are always square -> find the max cell size that will fit
		float cellw = (float)w / mNumCellsX;
		float cellh = (float)h / mNumCellsY;
		mCellSize = Math.min(cellw, cellh);
		
		mGridOriginX = 0.5f * (w - mCellSize * mNumCellsX);
		mGridOriginY = 0.5f * (h - mCellSize * mNumCellsY);
		
		setBallPosition(mBallCellX, mBallCellY);
	}
	
	public void setGridDim(int cellsAcross, int cellsDown) {
		if (cellsAcross < 1 || cellsDown < 1)
			return;
		
		mNumCellsX = cellsAcross;
		mNumCellsY = cellsDown;
		
		Maze maze = new Maze(mNumCellsY, mNumCellsX);
		maze.generateMaze();
		//if (!maze.testMaze())
		//	Log.v("maze", "maze failed test");
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
	
	public float getBallX() {
		return mBallX;
	}
	
	public float getBallY() {
		return mBallY;
	}
	
	public void setBallPosition(int cellX, int cellY) {
		// clip to valid range
		if (cellX < 0) cellX = 0; if (cellX >= mNumCellsX) cellX = mNumCellsX - 1;
		if (cellY < 0) cellY = 0; if (cellY >= mNumCellsY) cellY = mNumCellsY - 1;
		
		// compute ball center position
		mBallX = mCellSize * (cellX + 0.5f);
		mBallY = mCellSize * (cellY + 0.5f);
		
		// discretized ball position(s)
		mBallCellX = cellX;
		mBallCellY = cellY;
		mBallCellOffset = Dir.NONE;
		
		invalidate();
	}
	
	private void recomputeBallCellX()
	{
		mBallCellX = (int)(mBallX / mCellSize);
		float distFromCellCenter = mBallX - (mCellSize*mBallCellX + 0.5f*mCellSize);
		// if close to cell center, just move it to center
		if (Math.abs(distFromCellCenter) < 4.f) {
			mBallX -= distFromCellCenter;
			mBallCellOffset = Dir.NONE;
		}
		else if (distFromCellCenter < 0.f)
			mBallCellOffset = Dir.WEST;
		else
			mBallCellOffset = Dir.EAST;
		
		invalidate();
	}
	
	private void recomputeBallCellY()
	{
		mBallCellY = (int)(mBallY / mCellSize);
		float distFromCellCenter = mBallY - (mCellSize*mBallCellY + 0.5f*mCellSize);
		// if close to cell center, just move it to center
		if (Math.abs(distFromCellCenter) < 4.f) {
			mBallY -= distFromCellCenter;
			mBallCellOffset = Dir.NONE;
		}
		else if (distFromCellCenter < 0.f)
			mBallCellOffset = Dir.NORTH;
		else
			mBallCellOffset = Dir.SOUTH;
		
		invalidate();
	}
	
	public void shiftBallLeft(float dx)
	{
		if (mBallCellOffset == Dir.NORTH || mBallCellOffset == Dir.SOUTH)
			return;
		
		float rad = 0.5f*mCellSize;
		
		// compute total space to the left
		float leftspace = 0.f;
		int westwallcheck = mBallCellX;
		if (mBallCellOffset == Dir.EAST) {
			assert(mBallCellX < mNumCellsX-1);
			leftspace = (mBallX - rad) - (mCellSize*mBallCellX);
		}
		else if (mBallCellOffset == Dir.WEST) {
			assert(mBallCellX > 0);
			leftspace = (mBallX - rad) - (mCellSize*(mBallCellX-1));
			westwallcheck--;
		}
		while (westwallcheck > 0 && leftspace < dx) {
			if (mCells[mBallCellY][westwallcheck].westWall)
				break;
			leftspace += mCellSize;
			westwallcheck--;
		}
		
		// move ball as much as possible
		mBallX -= Math.min(dx, leftspace);
		
		// recompute discrete ball position
		recomputeBallCellX();
	}
	
	public void shiftBallRight(float dx)
	{
		if (mBallCellOffset == Dir.NORTH || mBallCellOffset == Dir.SOUTH)
			return;
		
		float rad = 0.5f*mCellSize;
		
		// compute total space to the right
		float rightspace = 0.f;
		int westwallcheck = mBallCellX+1;
		if (mBallCellOffset == Dir.EAST) {
			assert(mBallCellX < mNumCellsX-1);
			rightspace = (mCellSize*(mBallCellX+2)) - (mBallX + rad);
			westwallcheck++;
		}
		else if (mBallCellOffset == Dir.WEST) {
			rightspace = (mCellSize*(mBallCellX+1)) - (mBallX + rad);
		}
		while (westwallcheck < mNumCellsX && rightspace < dx) {
			if (mCells[mBallCellY][westwallcheck].westWall)
				break;
			rightspace += mCellSize;
			westwallcheck++;
		}
		
		// move ball as much as possible
		mBallX += Math.min(dx, rightspace);
		
		// recompute discrete ball position
		recomputeBallCellX();
	}
	
	public void shiftBallUp(float dy)
	{
		if (mBallCellOffset == Dir.WEST || mBallCellOffset == Dir.EAST)
			return;
		
		float rad = 0.5f*mCellSize;
		
		// compute total space up
		float upspace = 0.f;
		int northwallcheck = mBallCellY;
		if (mBallCellOffset == Dir.SOUTH) {
			assert(mBallCellY < mNumCellsY-1);
			upspace = (mBallY - rad) - (mCellSize*mBallCellY);
		}
		else if (mBallCellOffset == Dir.NORTH) {
			assert(mBallCellY > 0);
			upspace = (mBallY - rad) - (mCellSize*(mBallCellY-1));
			northwallcheck--;
		}
		while (northwallcheck > 0 && upspace < dy) {
			if (mCells[northwallcheck][mBallCellX].northWall)
				break;
			upspace += mCellSize;
			northwallcheck--;
		}
		
		// move ball as much as possible
		mBallY -= Math.min(dy, upspace);
		
		// recompute discrete ball position
		recomputeBallCellY();
	}
	
	public void shiftBallDown(float dy)
	{
		if (mBallCellOffset == Dir.WEST || mBallCellOffset == Dir.EAST)
			return;
		
		float rad = 0.5f*mCellSize;
		
		// compute total space down
		float downspace = 0.f;
		int northwallcheck = mBallCellY+1;
		if (mBallCellOffset == Dir.SOUTH) {
			assert(mBallCellY < mNumCellsY-1);
			downspace = (mCellSize*(mBallCellY+2)) - (mBallY + rad);
			northwallcheck++;
		}
		else if (mBallCellOffset == Dir.NORTH) {
			downspace = (mCellSize*(mBallCellY+1)) - (mBallY + rad);
		}
		while (northwallcheck < mNumCellsY && downspace < dy) {
			if (mCells[northwallcheck][mBallCellX].northWall)
				break;
			downspace += mCellSize;
			northwallcheck++;
		}
		
		// move ball as much as possible
		mBallY += Math.min(dy, downspace);
		
		// recompute discrete ball position
		recomputeBallCellY();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		if (!magnify)
		{
			// draw maze background (may not fill entire screen)
			Paint paint = new Paint();
			paint.setColor(mBGColor);
			canvas.drawRect(mGridOriginX, mGridOriginY, mGridOriginX+mCellSize*mNumCellsX, mGridOriginY+mCellSize*mNumCellsY, paint);

			// draw cell walls
			int ix, iy;
			float left, top, right, btm;
			float halfWallThickness = 0.5f * mWallThickness;
			paint.setColor(mWallColor);
			
			for (ix = 0; ix < mNumCellsX; ix++) {
				for (iy = 0; iy < mNumCellsY; iy++) {
					float x = mGridOriginX + mCellSize * ix;
					float y = mGridOriginY + mCellSize * iy;
					Cell c = mCells[iy][ix];
					if (iy > 0 && c.northWall) {
						// horizontal wall
						left = x;
						right = x + mCellSize;
						top = y - halfWallThickness;
						btm = y + halfWallThickness;
						canvas.drawRect(left, top, right, btm, paint);
					}
					if (ix > 0 && c.westWall) {
						// vertical wall
						top = y;
						btm = y + mCellSize;
						left = x - halfWallThickness;
						right = x + halfWallThickness;
						canvas.drawRect(left, top, right, btm, paint);
					}
				}
			}
			
			// draw ball
			paint.setAntiAlias(true);
			paint.setColor(mBallColor);				// 0.45 instead of 0.5 to be slightly smaller than cell
			canvas.drawCircle(mGridOriginX+mBallX, mGridOriginY+mBallY, 0.45f * mCellSize, paint);
		}
		else
		{
			Paint paint = new Paint();
			// draw ball
			paint.setColor(mBallColor);				// 0.45 instead of 0.5 to be slightly smaller than cell
			canvas.drawCircle( (mGridOriginX+mBallX*4)%canvas.getWidth() , (mGridOriginY+mBallY*7)%canvas.getHeight(), 0.45f * mCellSize * 4, paint);
			
			// Get cells to display
			int x1 = ((int)(mGridOriginX+mBallX)%mNumCellsX);
			//int x2 = (int) (((mGridOriginX+mBallX)%mNumCellsX) + 0.5);
			int y1 = ((int)(mGridOriginY+mBallY)%mNumCellsY);
			//int y2 = (int) (((mGridOriginY+mBallY)%mNumCellsY) + 0.5);
			System.err.println ("x1" + x1 + "y1" + y1);
			Cell c1 = mCells[y1][x1];
			//Cell c2 = mCells[y2][x2];
			
			paint.setColor(mBGColor);
			// Display cells
			if (c1.eastWall)
			{
				canvas.drawLine(canvas.getWidth(), mGridOriginY, canvas.getWidth(), canvas.getHeight()/2, paint);
			}
			
		}
	}
}
