package com.github.elixiroflife4u;

public class MazeCell {
	public boolean northWall;
	//public boolean southWall;
	//public boolean eastWall;
	public boolean westWall;
	
	MazeCell(boolean N, /*boolean S, boolean E,*/ boolean W) {
		northWall = N;
		//southWall = S;
		//eastWall = E;
		westWall = W;
	}
}
