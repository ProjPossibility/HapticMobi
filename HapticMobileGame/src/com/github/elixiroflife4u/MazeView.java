package com.github.elixiroflife4u;

import android.util.Log;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class MazeView extends View {

	private Cell mCells[][];
	private int mNumCellsX = 1;
	private int mNumCellsY = 1;
	private float mCellSize;
	private int mWallColor = 0xFFFFFFFF;
	private int mBGColor = 0xFF503010;
	private float mWallThickness = 2.0f;
	// grid (maze) origin w.r.t. view
	private float mGridOriginX, mGridOriginY;
	
	// coordinates of ball center w.r.t. maze rect
	private float mBallX = 0.f, mBallY = 0.f;
	// discretized ball coordinates
	// ball may be (partially) in two cells in the X direction and two cells in the Y direction
	// always true: _i1 <= _i2
	private int mBallXi1 = 0, mBallXi2 = 0, mBallYi1 = 0, mBallYi2 = 0;
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
		
		setBallPosition(mBallXi1, mBallYi1);
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
		
/*		mCells = new Cell[mNumCellsX][mNumCellsY];
		
		// create sample maze
		for (int ix = 0; ix < mNumCellsX; ix++)
			for (int iy = 0; iy < mNumCellsY; iy++)
				mCells[ix][iy] = new Cell(true, true);
		
		mCells[1][2].northWall = false;
		mCells[1][3].northWall = false;
		mCells[2][3].westWall = false;
		mCells[3][3].westWall = false;
		mCells[3][3].northWall = false;
		mCells[3][2].northWall = false;
		mCells[3][1].westWall = false;
		mCells[2][1].westWall = false;
*/
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
		mBallXi1 = cellX;
		mBallXi2 = cellX;
		mBallYi1 = cellY;
		mBallYi2 = cellY;
		
		invalidate();
	}
	
	public void shiftBallPosition(float dx, float dy) {
		
		Log.v("maze", "shiftBallPosition("+dx+","+dy+")");
		
		// shift the ball as much as possible
		// (this is the wall collision detection)
		if (dx < 0.f)
			dx = Math.max(dx, -ballMaxShiftLeft());
		else if (dx > 0.f)
			dx = Math.min(dx, ballMaxShiftRight());
		if (dy < 0.f)
			dy = Math.max(dy, -ballMaxShiftUp());
		else if (dy > 0.f)
			dy = Math.min(dy, ballMaxShiftDown());
		
		Log.v("maze", "    clipped to "+dx+" "+dy);
		
		mBallX += dx;
		mBallY += dy;
		
		// recompute discretized ball coordinates
		float rad = 0.5f*mCellSize;
		
		float tmp = (mBallX - rad) / mCellSize;
		float tmpfloor = (float)Math.floor(tmp);
		mBallXi1 = (int) tmpfloor;
		//mBallXi2 = (int) Math.ceil((mBallX + rad) / mCellSize);
		mBallXi2 = mBallXi1 + ((tmp - tmpfloor <= 1.e-5f) ? 0 : 1);
		
		tmp = (mBallY - rad) / mCellSize;
		tmpfloor = (float)Math.floor(tmp);
		mBallYi1 = (int) tmpfloor;
		//mBallYi2 = (int) Math.ceil((mBallY + rad) / mCellSize);
		mBallYi2 = mBallYi1 + ((tmp - tmpfloor <= 1.e-5f) ? 0 : 1);
		
		Log.v("maze", String.format("    x1=%d x2=%d y1=%d y2=%d", mBallXi1, mBallXi2, mBallYi1, mBallYi2));
		
		invalidate();
	}
	
	private float ballMaxShiftUp() {
		
		float distToWallAbove = (mBallY - mCellSize*0.5f) - mBallYi1*mCellSize;
		
		// look upwards for walls
		for (int y = mBallYi1; y > 0; y--) {
			if (mCells[y][mBallXi1].northWall || mCells[y][mBallXi2].northWall)
				break;
			distToWallAbove += mCellSize;
		}
		
		return distToWallAbove;
	}
	
	private float ballMaxShiftDown() {
		
		int ybelow = mBallYi2 + ((mBallYi2 == mBallYi1) ? 1 : 0);
		float distToWallBelow = ybelow*mCellSize - (mBallY + mCellSize*0.5f);
		
		// look downwards for walls
		for (int y = ybelow; y < mNumCellsY; y++) {
			if (mCells[y][mBallXi1].northWall || mCells[y][mBallXi2].northWall)
				break;
			distToWallBelow += mCellSize;
		}
		
		return distToWallBelow;
	}
	
	private float ballMaxShiftLeft() {
		
		float distToWallLeft = (mBallX - mCellSize*0.5f) - mBallXi1*mCellSize;
		
		// look leftwards for walls
		for (int x = mBallXi1; x > 0; x--) {
			if (mCells[mBallYi1][x].westWall || mCells[mBallYi2][x].westWall)
				break;
			distToWallLeft += mCellSize;
		}
		
		return distToWallLeft;
	}
	
	private float ballMaxShiftRight() {

		int xright = mBallXi2 + ((mBallXi2 == mBallXi1) ? 1 : 0);
		float distToWallRight = xright*mCellSize - (mBallX + mCellSize*0.5f);
		
		// look rightwards for walls
		for (int x = xright; x < mNumCellsX; x++) {
			if (mCells[mBallYi1][x].westWall || mCells[mBallYi2][x].westWall)
				break;
			distToWallRight += mCellSize;
		}
		
		return distToWallRight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
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
		paint.setColor(mBallColor);				// 0.45 instead of 0.5 to be slightly smaller than cell
		canvas.drawCircle(mGridOriginX+mBallX, mGridOriginY+mBallY, 0.45f * mCellSize, paint);
	}
}
