package com.github.elixiroflife4u;
import java.util.ArrayList;
import java.util.Stack;

public class Maze {
	//	maze is a 2-d grid of Cells
	// each cell is of of size 1 by 1 sperated by one in index
	// initializer will allocate the given number of cell and set up the neighbors of each cell
		
	private Cell maze[][];
	private int mazeRows; // the y axis (running from north to south)
	private int mazeCols; // the x axis (running from west to east)
	
	Maze(int rows, int cols){
		maze = new Cell[rows][cols]; //bcoz everything is allocated on heap
		this.mazeRows = rows;
		this.mazeCols = cols;
		init(); 
		setNeighbors();
	}
	//functions to help set up the structure
	private void init(){
		for(int i = 0; i < mazeRows; i++){
			for(int j =0; j < mazeCols; j++){
				maze[i][j] = new Cell(j,i);
			}
		}
	}
	private void setNeighbors(){
		for(int i = 0; i < mazeRows; i++){
			for(int j =0; j < mazeCols; j++){
				Cell currCell = maze[i][j];
				//we can have atmost 4 neighbors per cell
				ArrayList<Cell> neighborList = new ArrayList<Cell>();
				if (i != 0) { //add neighbor to the North of you
					neighborList.add(maze[i-1][j]);
				}
				if (i != mazeRows - 1) { // add neighbor to the South of you
					neighborList.add(maze[i+1][j]);
				}
				if (j != 0) { //add neighbor to the West of you
					neighborList.add(maze[i][j-1]);
				}
				if (j != mazeCols - 1) { //add neighbor to the East of you.
					neighborList.add(maze[i][j+1]);
				}
				currCell.setNeighbors(neighborList); 
			}
		}
	}
	public Cell[][] getMaze(){
		return maze;
	}
	public void generateMaze(){
		maze[0][0].setStartCell(true);
		Stack<Cell> stack = new Stack<Cell>(); //stack for DFS
		
		Cell Elem1 = maze[mazeRows-1][mazeCols-1];
		Elem1.setEndCell(true);
		
		Cell Elem2 = Elem1.getRandomUnvisitedNeighbor();
		Elem1.setVisited(true);
		Elem1.breakWall(Elem2); //break a wall to create the path
		stack.push(Elem1); //start at exit
		stack.push(Elem2);
		
		while(!stack.isEmpty()){ //while the stack is not empty
			Cell currCell =  stack.pop();
			//if current element is a deadEnd or the target nodes, backtrack
			if(currCell.isDeadEnd() || currCell.isStartCell()){
				//backtrack
				currCell.setVisited(true);
				continue;
			}
			else{ //you have unvisited neighbors, put yourself on the stack (again) and a random neighbor
				Cell nextCell = currCell.getRandomUnvisitedNeighbor();
				currCell.breakWall(nextCell);
				currCell.setVisited(true); //visited now. Should not be part of neighbors to explore for any node.
				stack.push(currCell);
				stack.push(nextCell);
			}			
		}
		
	}
	public boolean testMaze(){
		for(int i = 0; i < mazeRows; i++){
			System.out.println();
			for(int j = 0; j < mazeCols; j++){
				Cell curr = maze[i][j];
				System.out.print(" "+curr.northWall);
				System.out.print(" "+curr.eastWall);
				System.out.print(" "+curr.southWall);
				System.out.print(" "+curr.westWall);
				System.out.print(" ,");
				
				if(i != 0){
					Cell northC = maze[i-1][j];
					if(northC.southWall != curr.northWall)
						return false;
				}
				else{
					// nortwall must be present
					if(!curr.northWall)
						return false;
				}
				if(i != mazeRows-1){
					Cell southC = maze[i+1][j];
					if(southC.northWall != curr.southWall)
						return false;
				}
				else{
					//south wall must be present
					if(!curr.southWall)
						return false;
				}
				if(j != 0){
					Cell westC = maze[i][j-1];
					if(westC.eastWall != curr.westWall)
						return false;
				}
				else{
					//west wall must be present
					if(!curr.westWall)
						return false;
				}
				if(j != mazeCols -1){
					Cell eastC = maze[i][j+1];
					if(eastC.westWall != curr.eastWall)
						return false;
				}
				else{
					//east wall must be present
					if(!curr.eastWall)
						return false;
				}
			}
		}
		return true;
	}
}
