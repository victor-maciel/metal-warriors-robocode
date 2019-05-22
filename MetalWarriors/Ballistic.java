package MetalWarriors;

import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.AdvancedRobot;
import robocode.*;

import java.awt.*;

/**
 * Ballistic - a robot by (Victor Maciel)
 */
public class Ballistic extends AdvancedRobot{
	boolean movingForward;
	int turnDirection = 1; // Clockwise or counterclockwise

	/**
	 * run: Spin around looking for a target
	 */
	public void run() {
		corBlack();
		corRed();
		while (true) {
			if(pertoParede()) {
					back(100);
			}else {
					ahead(100);
				}
			// Tell the game we will want to move ahead 40000 -- some large number
			setAhead(1000);
			movingForward = true;
			// Tell the game we will want to turn right 90
			setTurnRight(90);
			corBlack();
			// At this point, we have indicated to the game that *when we do something*,
			// we will want to move ahead and turn right.  That's what "set" means.
			// It is important to realize we have not done anything yet!
			// In order to actually move, we'll want to call a method that
			// takes real time, such as waitFor.
			// waitFor actually starts the action -- we start moving and turning.
			// It will not return until we have finished turning.
			waitFor(new TurnCompleteCondition(this));
			// Note:  We are still moving ahead now, but the turn is complete.
			// Now we'll turn the other way...
			setTurnLeft(45);
			corRed();
			// ... and wait for the turn to finish ...
			waitFor(new TurnCompleteCondition(this));
			// ... then the other way ...
			setTurnRight(45);
			// .. and wait for that turn to finish.
			waitFor(new TurnCompleteCondition(this));
			// then back to the top to do it all again
	
		}
	}

	/**
	 * onScannedRobot:  We have a target.  Go get it.
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		fogo(e.getDistance());
		if (e.getBearing() >= 0) {
			turnDirection = 1;
		} else {
			turnDirection = -1;
		}

		turnRight(e.getBearing());
		ahead(e.getDistance() + 4);
		
		scan(); // Might want to move ahead again!
	}
	

	/**
	 * onHitRobot:  Turn to face robot, fire hard, and ram him again!
	 */
	public void onHitRobot(HitRobotEvent e) {
		ahead(100);
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
			corRed();
		} else if (e.getEnergy() > 10) {
			fire(2);
			corBlack();
		} else if (e.getEnergy() > 4) {
			fire(1);
			corRed();
		} else if (e.getEnergy() > 2) {
			fire(.5);
			corBlack();
		} else if (e.getEnergy() > .4) {
			fire(.1);
			corRed();
		}
		ahead(40); // Ram him again!
	}
	
	public boolean pertoParede()
	{
		return
		(getX() < 150 || getX() > getBattleFieldWidth() - 150 ||
		 getY() < 150 || getY() > getBattleFieldHeight() -150);
	}

	//comemorando se ganhar 
	public void onWin(WinEvent e)
	{
		turnLeft(72000);
	}
	

	//muda a cor do robo para vermelho
	public void corRed(){
		setBodyColor(Color.red);
		setGunColor(Color.red);
		setRadarColor(Color.red);
		setBulletColor(Color.red);
		setScanColor(Color.red);
	}
	
	//muda a cor do robo para preto
	public void corBlack(){
		setBodyColor(Color.black);
		setGunColor(Color.black);
		setRadarColor(Color.black);
		setBulletColor(Color.black);
		setBodyColor(Color.black);
	}
	
	public void fogo(double Distancia) {
		if (Distancia > 200 || getEnergy() < 15) {
			fire(1);
		} else if (Distancia > 50) {
			fire(2);
		} else {
			fire(3);
	    }
}
}
