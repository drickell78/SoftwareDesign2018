package main.java.softdesign;

import java.util.*;
import javafx.util.Pair;

import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

import simbad.sim.Agent;
import simbad.sim.RobotFactory;
import simbad.sim.*;

public class Robot extends Agent {

	private String currentMode;
	private RangeSensorBelt sonars;
	private Point3d coords;
	private LinkedList<Point3d> pastCoordinates;
	private int[][] worldMap;

    public Robot(Vector3d position, String name) {
        super(position, name);
        
        // Initialize map
        worldMap = new int[40][40];
        
        // Add coordinates
        coords = new Point3d();
        pastCoordinates = new LinkedList<Point3d>();

        // Add bumpers
        RobotFactory.addBumperBeltSensor(this, 12);
        // Add 4 sonars and save to sonars variable
        sonars = RobotFactory.addSonarBeltSensor(this, 4);
    }

    /** This method is called by the simulator engine on reset. */
    public void initBehavior() {
        System.out.println("I exist and my name is " + this.name);
    }
    
    private void printMap() {
    	// Debug only
    	for (int i = 0; i < 40; i++) {
    		for (int j = 0; j < 40; j++) {
    			System.out.print(worldMap[i][j]);
    		}
    		System.out.println();
    	}
    }
    
    private void addCordinateToMap(Point3d coords) {
    	// Convert float coordinat into integers for easy visualization 
    	int translatedX = (int)((coords.x + 5) * 4);
    	int translatedZ = (int)((coords.z + 5) * 4);
    	
    	worldMap[translatedX][translatedZ] = 1;
    }
    
    private void addCoordinatesToList(Point3d coords) {
    	// Duplicate the coordinate object
		pastCoordinates.addLast(new Point3d(coords.x, coords.y, coords.z));
		
		// Save coordinate to the map
		addCordinateToMap(coords);
		
		// Store only the past 20 coordinates
		if (pastCoordinates.size() > 20) {
			pastCoordinates.removeFirst();
		}
    }
    
    private boolean isRobotMoving() {
    	int index = 0; 
    	double deltaX = 0, deltaY = 0;
    	Point3d prevPair = new Point3d();
    	
    	// Not enough sample rate
    	if (pastCoordinates.size() < 20) {
    		return true;
    	}
    	
    	for (Point3d coordsPair : pastCoordinates) {
    		 if (index++ == 0) {
    			 prevPair = coordsPair;
    			 continue;
    		 }
    		 
    		 // Compare all 10 past coordinates and sum the difference
    		 deltaX += Math.abs(coordsPair.x - prevPair.x);
    		 deltaY += Math.abs(coordsPair.z - prevPair.z);
    		 
    		 prevPair = coordsPair;
		}
    	
    	// Check if the sum of differences is 0 meaning that robot is not moving
    	if (deltaX == 0 && deltaY == 0) {
    		return false;
    	}
    	
    	return true;
    }

    /** This method is call cyclically (20 times per second) by the simulator engine. */
    public void performBehavior() {

    	// Every 50 seconds set the robot into doRandomTurn mode for 5 seconds.
    	if(this.getCounter() % 50 == 0 && this.currentMode == "goStraight") {
			this.currentMode = "doRandomTurn";
    	}
    	
    	// After 5 seconds of doRandomTurn, change the robot back to goStraight.
    	if(this.getCounter() % 50 == 5 && this.currentMode == "doRandomTurn") {
			this.currentMode = "goStraight";
    	}
    	
    	// Check if robot is stuck and send him to the initial location if so.
    	if(this.getCounter() % 50 == 0) {
    		if (isRobotMoving() == false) {
    			moveToStartPosition();
    		}
    	}
    	
    	// Perform the following actions every 5 virtual seconds
    	if(this.getCounter() % 2 == 0) {
	    	if(this.currentMode == "goStraight") {
	    		// the robot's speed is always 0.5 m/s
	            this.setTranslationalVelocity(0.5);
	            // Make the robot go straight
	            setRotationalVelocity(0);
	        } else if(this.currentMode == "avoidFrontLeftObstacle") {
	        	// don't move
	        	this.setTranslationalVelocity(0);
	        	// rotate clockwise
	        	setRotationalVelocity(0 - (Math.PI / 2));
	        } else if(this.currentMode == "avoidFrontRightObstacle") {
	        	// don't move
	        	this.setTranslationalVelocity(0);
	        	// rotate counterclockwise
	        	setRotationalVelocity(Math.PI / 2);
	        } else if(this.currentMode == "doRandomTurn") {
	        	// Rotate with a random velocity in range [-PI/2, PI/2]
	        	setRotationalVelocity((0.5 - Math.random()) * Math.PI);
	        }
    	}
    	
    	// Store curent coordinates every 2 seconds.
    	if (getCounter() % 2 == 0) {
			getCoords(coords);
			addCoordinatesToList(coords);
    	}

		if (getCounter() % 10 == 0) {
		// Check if there is getting closer to an obstacle
			if (sonars.getFrontQuadrantHits() > 0) {
				if (sonars.getLeftQuadrantHits() > 0) { // Check if there is obstacle to the left so it can safely rotate
					this.currentMode = "avoidFrontLeftObstacle";
				} else { // Rotate left because there is no obstacle there.
					this.currentMode = "avoidFrontRightObstacle";
				}
			} else  {
				this.currentMode = "goStraight";
			}
		}

    }
}
