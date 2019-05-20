package MetalWarriors;

import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * Ballistic - a robot by (Victor Maciel)
 */
public class Ballistic extends Robot{
int turnDirection = 1; // Clockwise or counterclockwise

	/**
	 * run: Spin around looking for a target
	 */
	public void run() {
		// Set colors
		setBodyColor(Color.magenta);
		setGunColor(Color.magenta);
		setRadarColor(Color.gray);

		while (true) {
			turnRight(5 * turnDirection);
		}
	}

	/**
	 * onScannedRobot:  We have a target.  Go get it.
	 */
	public void onScannedRobot(ScannedRobotEvent e) {

		if (e.getBearing() >= 0) {
			turnDirection = -1;
		} else {
			turnDirection = 1;
		}

		turnRight(e.getBearing());
		ahead(e.getDistance() + 3);
		scan(); // Might want to move ahead again!
	}
/*	public void onWin(WinEvent e) {	
    		turnRight(36000);
		}
*/
	/**
	 * onHitRobot:  Turn to face robot, fire hard, and ram him again!
	 */
	public void onHitRobot(HitRobotEvent e) {
		if (e.getBearing() >= 0) {
			turnDirection = 1;
		} else {
			turnDirection = -1;
		}
		turnRight(e.getBearing());

		// Determine a shot that won't kill the robot...
		// We want to ram him instead for bonus points
		if (e.getEnergy() > 16) {
			fire(3);
		} else if (e.getEnergy() > 10) {
			fire(2);
		} else if (e.getEnergy() > 4) {
			fire(1);
		} else if (e.getEnergy() > 2) {
			fire(.5);
		} else if (e.getEnergy() > .4) {
			fire(.1);
		}
		ahead(40); // Ram him again!
	}

}