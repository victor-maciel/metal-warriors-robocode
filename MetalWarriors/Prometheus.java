package MetalWarriors;
import robocode.*;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Prometheus - a robot by Victor Maciel
 * Prometheus - Inspirado num robô do jogo Metal Warrios do Super Nintendo. 
 * 				Um robô ótimo na trocação e se bater nele ele devolve na
 * 				mesma moeda.
 */
public class Prometheus extends AdvancedRobot{
	/**
	 * run: Prometheus's default behavior
	 */

	
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		while(true) {
		//verifica se está perto da parede
		
			// Replace the next 4 lines with any behavior you would like
			ahead(100);
			setTurnGunRight(360);
			back(100);
			setTurnGunRight(360);
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Mira de acordo com o angulo  e distância do inimigo e atira
		miraRobot(e.getBearing());
		fogo(e.getDistance());
	}
	
	public void onHitWall(HitWallEvent e) {
	// Substitua a próxima linha com o comportamento padrão que você desejar
		if(pertoParede()) {
				back(100);
		}else{
				ahead(100);
		}
	}
	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);

	}
	
	public void onHitRobot(HitRobotEvent e) {
	
		miraRobot(e.getBearing());
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

//Mira o canhão
	public void miraRobot(double Adv) {
		double A=getHeading()+Adv-getGunHeading();
		if (!(A > -180 && A <= 180)) {
			while (A <= -180) {
				A += 360;
			}
			while (A > 180) {
				A -= 360;
			}	
		}
	setTurnGunRight(A);
	}
// comemoração caso vença a partida
	public void onWin(WinEvent e){
		comemoracao();
		}
//metodo que atira para todo lado se vencer a partida		
	public void comemoracao() { 
		setMaxVelocity(5);
		setTurnGunRight(10000);
			while(true) {
				ahead(20);
				back(20);  
				if (getEnergy() > 0.1) {
					fire(0.1);
				}
		}
	}

	public void fogo(double Distancia) {
		if(Distancia > 200 || getEnergy() < 15){
			fire(1);
		} else if (Distancia > 50) {
			fire(2);
		} else {
			fire(3);
	    }
	}
//verifica se está perto da parede	
	public boolean pertoParede() {
		return (getX() < 50 || getX() > getBattleFieldWidth() - 50 ||
			getY() < 50 || getY() > getBattleFieldHeight() - 50);
}
}

