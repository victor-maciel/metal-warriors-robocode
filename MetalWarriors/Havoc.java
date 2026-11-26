package MetalWarriors;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

/**
 * Havoc - a robot by Victor Maciel
 * Havoc - Inspirado num robô do jogo Metal Warriors do Super Nintendo.
 * 		Ágil, de curto/médio alcance e focado em pressão: gira em volta do
 * 		inimigo (strafe) o mais rápido possível (jatos nos pés), com a
 * 		metralhadora mais rápida do jogo (porém de menor dano) e a corrente
 * 		com lâmina (tiro forte) quando chega perto.
 * 		--> A IDEIA NÃO É VENCER E SIM SIMULAR O OS PADRÕES DO JOGO NO ROBÔ <--
 * Projeto MetalWarriors: https://github.com/victor-maciel/metal-warriors-robocode
 */
public class Havoc extends AdvancedRobot {
	int direcao = 1;          // 1 = horário, -1 = anti-horário
	int ticksNaDirecao = 0;   // quanto tempo está indo na mesma direção
	static final double DISTANCIA_IDEAL = 150; // curto/médio alcance

	public void run() {
		cores();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		while (true) {
			// radar girando até achar alguém (depois a trava no onScannedRobot)
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

		// trava o radar no inimigo
		double giroRadar = Utils.normalRelativeAngle(anguloAbs - getRadarHeadingRadians());
		setTurnRadarRightRadians(giroRadar * 2);

		// caos: troca de direção de vez em quando
		ticksNaDirecao++;
		if (ticksNaDirecao > 15 && Math.random() < 0.08) {
			direcao = -direcao;
			ticksNaDirecao = 0;
		}

		// orbita o inimigo; se estiver longe, se aproxima, se perto, se afasta
		double ajuste = 0;
		if (e.getDistance() > DISTANCIA_IDEAL + 60) {
			ajuste = -25;
		} else if (e.getDistance() < DISTANCIA_IDEAL - 60) {
			ajuste = 25;
		}
		orbita(e.getBearing() + 90 * direcao + ajuste * direcao, 120);
		// jatos nos pés: robô mais rápido em solo
		setMaxVelocity(8);

		// mira preditiva com dispersão (rifle de dispersão)
		double poder = escolhePoder(e.getDistance());
		double mira = miraPreditiva(e, poder) + (Math.random() - 0.5) * 0.12;
		setTurnGunRightRadians(Utils.normalRelativeAngle(mira - getGunHeadingRadians()));
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
			setFire(poder);
		}
	}

	/** Quanto mais perto, mais potente. Com pouca energia economiza. */
	double escolhePoder(double distancia) {
		if (getEnergy() < 15) {
			return 1;
		}
		// perto: corrente com lâmina (dano alto); longe: metralhadora (rápida, dano baixo)
		return distancia < 90 ? 2.5 : 0.7;
	}

	/** Prediz onde o inimigo estará assumindo velocidade e direção constantes. */
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

	public void onHitWall(HitWallEvent e) {
		direcao = -direcao;
		ticksNaDirecao = 0;
	}

	/** Colidiu: rajada à queima-roupa. */
	public void onHitRobot(HitRobotEvent e) {
		double giro = Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading());
		setTurnGunRight(giro);
		if (getGunHeat() == 0) {
			setFire(3);
		}
	}

	/** Levou tiro: inverte o sentido para atrapalhar a mira do inimigo. */
	public void onHitByBullet(HitByBulletEvent e) {
		direcao = -direcao;
		ticksNaDirecao = 0;
	}

	public void onWin(WinEvent e) {
		setMaxVelocity(8);
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}

	Color corBase = new Color(255, 120, 0);

	/** Sem barra de vida: quanto menos energia, mais escuro o robô (como no jogo). */
	void corPelaEnergia() {
		double f = Math.max(0.2, Math.min(1.0, getEnergy() / 100.0));
		setBodyColor(new Color((int) (corBase.getRed() * f), (int) (corBase.getGreen() * f), (int) (corBase.getBlue() * f)));
	}

	public void cores() {
		setBodyColor(corBase);
		setGunColor(new Color(60, 60, 60));
		setRadarColor(Color.yellow);
		setBulletColor(Color.orange);
		setScanColor(Color.orange);
	}
}
