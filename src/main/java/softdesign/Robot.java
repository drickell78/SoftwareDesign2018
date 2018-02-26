package main.java.softdesign;


import javax.vecmath.Vector3d;

import simbad.sim.Agent;
import simbad.sim.RobotFactory;
import simbad.sim.*;

public class Robot extends Agent {

	private String currentMode;
	private RangeSensorBelt sonars;

    public Robot(Vector3d position, String name) {
        super(position, name);

        // Add bumpers
        RobotFactory.addBumperBeltSensor(this, 12);
        // Add 4 sonars and save to sonars variable
        sonars = RobotFactory.addSonarBeltSensor(this, 4);
    }

    /** This method is called by the simulator engine on reset. */
    public void initBehavior() {
        System.out.println("I exist and my name is " + this.name);
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
