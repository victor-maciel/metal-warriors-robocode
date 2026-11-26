package MetalWarriors;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

/**
 * Nitro - a robot by Victor Maciel
 * Nitro - Inspirado num robô do jogo Metal Warriors do Super Nintendo.
 * 		Versátil e equilibrado:
 * 		- rifle de energia: tiro médio com mira preditiva a média distância
 * 		- sabre de luz: se o inimigo chega perto, parte para cima e dá dano alto
 * 		- escudo: quando percebe que o inimigo atirou, se esquiva
 * 		- voo livre com jatos: movimentação ágil em volta do inimigo
 * 		--> A IDEIA NÃO É VENCER E SIM SIMULAR O OS PADRÕES DO JOGO NO ROBÔ <--
 * Projeto MetalWarriors: https://github.com/victor-maciel/metal-warriors-robocode
 */
public class Nitro extends AdvancedRobot {
	int direcao = 1;
	double energiaInimigo = 100;
	static final double DISTANCIA_IDEAL = 250; // alcance do rifle
	static final double DISTANCIA_SABRE = 110; // alcance do sabre

	public void run() {
		robotColor();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		while (true) {
			if (getRadarTurnRemaining() == 0) {
				setTurnRadarRight(360);
			}
			if (getDistanceRemaining() == 0 && getTurnRemaining() == 0) {
				setAhead(100);
			}
			corPelaEnergia();
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double anguloAbs = getHeadingRadians() + e.getBearingRadians();
		setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(anguloAbs - getRadarHeadingRadians()));

		// escudo: o inimigo atirou (energia caiu) -> esquiva
		double queda = energiaInimigo - e.getEnergy();
		if (queda > 0.09 && queda <= 3.0) {
			direcao = -direcao;
		}
		energiaInimigo = e.getEnergy();

		double dist = e.getDistance();
		double poder;

		if (dist < DISTANCIA_SABRE) {
			// sabre de luz: investe e causa dano alto no corpo a corpo
			setMaxVelocity(8);
			setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing()));
			setAhead(dist + 20);
			poder = 3;
		} else {
			// rifle: orbita em média distância
			double ajuste = 0;
			if (dist > DISTANCIA_IDEAL + 70) {
				ajuste = -25;
			} else if (dist < DISTANCIA_IDEAL - 70) {
				ajuste = 25;
			}
			setMaxVelocity(8);
			orbita(e.getBearing() + 90 * direcao + ajuste * direcao, 100);
			poder = dist < 350 ? 2 : 1.5; // dano médio
		}
		if (getEnergy() < 15) {
			poder = Math.min(poder, 1);
		}

		double mira = miraPreditiva(e, poder);
		setTurnGunRightRadians(Utils.normalRelativeAngle(mira - getGunHeadingRadians()));
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
			setFire(poder);
		}
	}

	/**
	 * Orbita de verdade nos dois sentidos: anda sempre pelo caminho mais curto
	 * (frente ou ré) e, se o ponto à frente sair da arena, vira para o centro.
	 */
	void orbita(double giro, double dist) {
		giro = Utils.normalRelativeAngleDegrees(giro);
		if (Math.abs(giro) > 90) {
			giro = Utils.normalRelativeAngleDegrees(giro + 180);
			dist = -dist;
		}
		double h = Math.toRadians(getHeading() + giro + (dist < 0 ? 180 : 0));
		double px = getX() + Math.sin(h) * 150, py = getY() + Math.cos(h) * 150;
		double m = 60;
		if (px < m || px > getBattleFieldWidth() - m || py < m || py > getBattleFieldHeight() - m) {
			double centro = Math.toDegrees(Math.atan2(getBattleFieldWidth() / 2 - getX(), getBattleFieldHeight() / 2 - getY()));
			giro = Utils.normalRelativeAngleDegrees(centro - getHeading());
			dist = Math.abs(dist);
			if (Math.abs(giro) > 90) {
				giro = Utils.normalRelativeAngleDegrees(giro + 180);
				dist = -dist;
			}
		}
		setTurnRight(giro);
		setAhead(dist);
	}

	double miraPreditiva(ScannedRobotEvent e, double poder) {
		double velTiro = 20 - 3 * poder;
		double anguloAbs = getHeadingRadians() + e.getBearingRadians();
		double ex = getX() + e.getDistance() * Math.sin(anguloAbs);
		double ey = getY() + e.getDistance() * Math.cos(anguloAbs);
		double px = ex, py = ey;

		for (int i = 0; i < 10; i++) {
			double t = Math.hypot(px - getX(), py - getY()) / velTiro;
			px = ex + e.getVelocity() * t * Math.sin(e.getHeadingRadians());
			py = ey + e.getVelocity() * t * Math.cos(e.getHeadingRadians());
			px = Math.max(18, Math.min(getBattleFieldWidth() - 18, px));
			py = Math.max(18, Math.min(getBattleFieldHeight() - 18, py));
		}
		return Math.atan2(px - getX(), py - getY());
	}

	public void onHitRobot(HitRobotEvent e) {
		// golpe de sabre
		setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
		if (getGunHeat() == 0) {
			setFire(3);
		}
	}

	public void onHitWall(HitWallEvent e) {
		direcao = -direcao;
	}

	public void onHitByBullet(HitByBulletEvent e) {
		direcao = -direcao;
	}

	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}

	Color corBase = new Color(153, 50, 204);

	/** Sem barra de vida: quanto menos energia, mais escuro o robô (como no jogo). */
	void corPelaEnergia() {
		double f = Math.max(0.2, Math.min(1.0, getEnergy() / 100.0));
		setBodyColor(new Color((int) (corBase.getRed() * f), (int) (corBase.getGreen() * f), (int) (corBase.getBlue() * f)));
	}

	public void robotColor() {
		setBodyColor(corBase);
		setGunColor(new Color(255, 0, 0));
		setRadarColor(Color.white);
		setScanColor(Color.white);
		setBulletColor(Color.blue);
	}
}
