package MetalWarriors;
import robocode.*;
import java.awt.*;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Prometheus - a robot by Victor Maciel
 * Prometheus - Inspirado num robô do jogo Metal Warrios do Super Nintendo. 
 * 				Um robô lento, com pouca movimentação, mas ótimo na trocação e
 *				se bater nele ele devolve na mesma moeda atirando e tenta se 
 *				desviar.
 * 			--> A IDEIA NÃO É VENCER E SIM SIMULAR O OS PADRÕES DO JOGO NO ROBÔ <--
 *  Prometheus gameplay: https://www.youtube.com/watch?v=rIsv8674g78
 *  Projeto MetalWarriors: https://github.com/victor-maciel/metal-warriors-robocode
 */
public class Prometheus extends AdvancedRobot{
	/**
	 * run: Prometheus's default behavior
	 */

	public void run() {

		// Robot main loop
		while(true) {
		//verifica se está perto da parede, verifica a energia e faz movimentação lenta
			pertoParede();
			
			verificaEnergia();
			
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
	/**
	 * onHitWall: Verifica a energia e aciona o método pertoParede
	 */
	public void onHitWall(HitWallEvent e) {
		verificaEnergia();
		if(pertoParede()) {
				back(100);
		}else{
				ahead(100);
		}
	}
	/**
	 * onHitByBullet: Verifica a energia e anda recua.
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		verificaEnergia();
		back(20);
		
	
		
	}
	/**
	 * onHitRobot: Verifica a energia, aponta a mira para o robÔ inimigo
	 * confronta e insentifica o tiro de acordo com a Energia do inimigo
	 */
	public void onHitRobot(HitRobotEvent e) {
		verificaEnergia();
		miraRobot(e.getBearing());

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
		ahead(40); 
	}

	//Mira o canhão de acordo com o angulo do inimigo. 
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
	//comemoração caso vença a partida
	public void onWin(WinEvent e){
		comemoracao();
		}
	//metodo que atira para todo lado se vencer a partida (se o tempo deixar)		
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
	/**
	 * fogo: Verifica a distância e atira.
	 * Quanto mais perto, mais potente o tiro.
	 */
	public void fogo(double Distancia) {
		verificaEnergia();
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

	//muda a cor do robo para vermelho
	public void corRed(){
		setBodyColor(Color.red);
		setGunColor(Color.red);
		setRadarColor(Color.red);
		setBulletColor(Color.red);
		setScanColor(Color.red);
	}
	
	//muda a cor do robo para marron
	public void corMarron(){
		setBodyColor(new Color(128, 0, 0));
		setGunColor(new Color(128, 0, 0));
		setRadarColor(new Color(128, 0, 0));
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
	/**
	 * verificaEnergia: Verifica a energia do robô para simular que está
	 * morrendo, a ideia é, de quanto menos energia mais escuro fica.
	 */
	public void verificaEnergia(){
		if(getEnergy() >= 70){
			corRed();
		}else if(getEnergy() > 30 &  getEnergy() < 70){
			corMarron();
		}else{
			corBlack();
		}
	}
		
}