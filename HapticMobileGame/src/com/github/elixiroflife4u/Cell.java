package com.github.elixiroflife4u;

import java.util.ArrayList;
import java.util.Random;

//model class represents a maze grid cell
//refer to Wikipedia article "Maze generation" for more information
public class Cell {
	//common to all Cell objects
	static Random r = new Random();
	//per object
	private boolean visited = false;
	private boolean startCell = false; //not sure about these.
	private boolean endCell = false;
	//wall related information. true indicates that a wall is present.
	public boolean northWall = true;
	public boolean southWall = true;
	public boolean eastWall  = true;
	public boolean westWall  = true;
	//location
	public int xCoord;
	public int yCoord;
	
	public ArrayList<Cell> neighbors;
	
	//constructors
	Cell(int x, int y){
		this.xCoord = x;
		this.yCoord = y;
	}
	Cell(int x, int y, ArrayList<Cell> neighbors){
		this.neighbors = neighbors;
		this.xCoord = x;
		this.yCoord = y;
	}
	//setters
	public void setVisited(boolean value){
		this.visited = value;
	}
	public void setStartCell(boolean value){
		this.startCell = value;
	}
	public void setEndCell(boolean value){
		this.endCell = value;
	}
	public void setX(int X){
		this.xCoord = X;
	}
	public void setY(int Y){
		this.yCoord = Y;
	}
	public void setXY(int X, int Y){
		this.xCoord = X;
		this.yCoord = Y;
	}
	public void setNeighbors(ArrayList<Cell> neighborsArray){
		this.neighbors = neighborsArray;
	}
	
	//getters
	public boolean isVisited(){
		return this.visited;
	}
	public boolean isStartCell(){
		return this.startCell;
	}
	public boolean isEndCell(){
		return this.endCell;
	}
	public int getXCoord(){
		return this.xCoord;
	}
	public int getYCoord(){
		return this.yCoord;
	}
	public ArrayList<Cell> getAllNeighbors(){
		return this.neighbors;
	}
	public int getTotalNumberOfNeighbors(){
		return neighbors.size();
	}
	//helper functions
	//
	private static int getRandomInt(int size){
		return r.nextInt(size);
	}
	//get unvisited neighboring cells
	public ArrayList<Cell> getUnvisitedNeighbors(){
		ArrayList<Cell> l = new ArrayList<Cell>();
		for(Cell c: neighbors){
			if(!c.visited)
				l.add(c);
		}
		return l;
	}
	//get random neighboring cell
	public Cell getRandomUnvisitedNeighbor(){
		ArrayList<Cell> listOfUnvisitedNeighbors = getUnvisitedNeighbors(); 
		return listOfUnvisitedNeighbors.get(Cell.getRandomInt(listOfUnvisitedNeighbors.size()));
	}
	//check if dead end, ie. no unvisited neighbors
	public boolean isDeadEnd(){
		for(Cell c: neighbors){
			if(!c.visited)
				return false;
		}
		return true;
	}
	
	//wall manipulation
	/**
	 * --->x axis      N
	 * |			W     E
	 * v y axis        S
	 *   --- ---  < east and west neighbors are y aligned.
	 *   | | | |
	 *   --- ---
	 *   ---
	 *   | |
	 *   ---
	 *   ^ north and south neighbors are x aligned
	 */
	public void breakWall(Cell neighborCell){
		//the positive x axis is going left to right and the positive y axis is going from top to bottom
		if(xCoord == neighborCell.xCoord){ //north or south neighbor
			if(yCoord < neighborCell.yCoord) {//south wall will vanish.
				this.southWall = false;
				neighborCell.northWall = false;
			} else{
				this.northWall = false;
				neighborCell.southWall = false;
			}
		}
		else if(yCoord == neighborCell.yCoord){ //east or west neighbor
			if(xCoord < neighborCell.xCoord) {//east wall of current cell will vanish
				this.eastWall = false;
				neighborCell.westWall = false;
			} else{
				this.westWall = false;
				neighborCell.eastWall = false;
			}
		}
	}
	
}