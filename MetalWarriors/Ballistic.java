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
int turnDirection = 1; // Clockwise or counterclockwise

	/**
	 * run: Spin around looking for a target
	 */
	public void run() {
		// Set colors
		

		while (true) {
			
			if(pertoParede()){
					corRed();
					back(30);
			}else{
			
					corBlack();
					setTurnLeft(10000);
					setMaxVelocity(5);
					ahead(10000);
			}
		}
	}

	/**
	 * onScannedRobot:  We have a target.  Go get it.
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		corRed();
		fire(3);
		corBlack();
		scan(); // Might want to move ahead again!
	}
	
	
	
	public void onHitWall(HitWallEvent e) {
		back(20);
	}

	/**
	 * onHitRobot:  Turn to face robot, fire hard, and ram him again!
	 */
	public void onHitRobot(HitRobotEvent e) {
		ahead(100);
		if (getGunHeat() == 0) {
			fire(Rules.MAX_BULLET_POWER);
		}
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
		(getX() < 50 || getX() > getBattleFieldWidth() - 50 ||
		 getY() < 50 || getY() > getBattleFieldHeight() - 50);
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
}
