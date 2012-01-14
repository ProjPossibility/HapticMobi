package com.github.elixiroflife4u;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class MazeView extends View {

	private MazeCell mCells[][];
	private int mNumCellsX;
	private int mNumCellsY;
	private float mCellSize;
	private int mWallColor = 0xFFFFFFFF;
	private int mBGColor = 0xFF503010;
	private float mWallThickness = 2.0f;
	private float mGridOriginX, mGridOriginY;
	
	MazeView(Context ctx) {
		super(ctx);
	}
	
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		float cellw = (float)w / mNumCellsX;
		float cellh = (float)h / mNumCellsY;
		mCellSize = Math.min(cellw, cellh);
		
		mGridOriginX = 0.5f * (w - mCellSize * mNumCellsX);
		mGridOriginY = 0.5f * (h - mCellSize * mNumCellsY);
	}
	
	public void setGridDim(int cellsAcross, int cellsDown) {
		if (cellsAcross < 1 || cellsDown < 1)
			return;
		
		mNumCellsX = cellsAcross;
		mNumCellsY = cellsDown;
		
		mCells = new MazeCell[mNumCellsX][mNumCellsY];
		
		for (int ix = 0; ix < mNumCellsX; ix++)
			for (int iy = 0; iy < mNumCellsY; iy++)
				mCells[ix][iy] = new MazeCell(true, /*true, true,*/ true);
		
		mCells[1][2].northWall = false;
		
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
	
	private static final String TAG = "MazeView";
	
	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(mBGColor);
		canvas.drawRect(mGridOriginX, mGridOriginY, mGridOriginX+mCellSize*mNumCellsX, mGridOriginY+mCellSize*mNumCellsY, paint);

		paint.setColor(mWallColor);

		int ix, iy;
		float left, top, right, btm;
		float halfWallThickness = 0.5f * mWallThickness;
		
		// draw cell walls
		for (ix = 0; ix < mNumCellsX; ix++) {
			for (iy = 0; iy < mNumCellsY; iy++) {
				float x = mGridOriginX + mCellSize * ix;
				float y = mGridOriginY + mCellSize * iy;
				MazeCell c = mCells[ix][iy];
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
	}
}
