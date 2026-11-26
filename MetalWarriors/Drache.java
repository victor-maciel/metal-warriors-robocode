package MetalWarriors;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

/**
 * Drache - a robot by Victor Maciel
 * Drache - Inspirado num robô do jogo Metal Warriors do Super Nintendo.
 * 		Unidade aérea de hit-and-run (bate e sai):
 * 		- APROXIMA: voa direto no inimigo, atirando leve com o canhão de energia.
 * 		- Chegando perto: mergulho de investida (power dive), tiro de impacto máximo.
 * 		- FUGA: sai voando para longe e volta a atacar.
 * 		  No jogo o Drache tem a melhor defesa (campo de força com as asas),
 * 		  que não existe no Robocode; aqui ele compensa não ficando parado
 * 		  perto do inimigo.
 * 		--> A IDEIA NÃO É VENCER E SIM SIMULAR O OS PADRÕES DO JOGO NO ROBÔ <--
 * Projeto MetalWarriors: https://github.com/victor-maciel/metal-warriors-robocode
 */
public class Drache extends AdvancedRobot {
	static final int APROXIMA = 0;
	static final int FUGA = 1;

	int estado = APROXIMA;
	int ticksFuga = 0;
	static final double DISTANCIA_MERGULHO = 170;
	static final double DISTANCIA_SEGURA = 400;

	public void run() {
		cores();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setMaxVelocity(8); // voo constante e rápido

		while (true) {
			if (getRadarTurnRemaining() == 0) {
				setTurnRadarRight(360);
			}
			corPelaEnergia();
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double anguloAbs = getHeadingRadians() + e.getBearingRadians();
		setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(anguloAbs - getRadarHeadingRadians()));

		double dist = e.getDistance();
		double poder = 1; // canhão de energia padrão

		if (estado == APROXIMA) {
			// voa direto para o inimigo
			setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing()));
			setAhead(200);

			if (dist < DISTANCIA_MERGULHO) {
				// power dive: tiro de impacto máximo e já sai fora
				poder = getEnergy() > 20 ? 3 : 1;
				estado = FUGA;
				ticksFuga = 0;
			}
		} else {
			// foge para o lado oposto ao inimigo
			ticksFuga++;
			setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 180));
			setAhead(200);

			if (dist > DISTANCIA_SEGURA || ticksFuga > 40 || pertoParede()) {
				estado = APROXIMA; // volta para outra investida
			}
		}

		double mira = miraPreditiva(e, poder);
		setTurnGunRightRadians(Utils.normalRelativeAngle(mira - getGunHeadingRadians()));
		if (getGunHeat() == 0 && (poder == 3 || Math.abs(getGunTurnRemaining()) < 10)) {
			setFire(poder);
		}
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

	boolean pertoParede() {
		return getX() < 70 || getX() > getBattleFieldWidth() - 70
				|| getY() < 70 || getY() > getBattleFieldHeight() - 70;
	}

	public void onHitWall(HitWallEvent e) {
		estado = APROXIMA;
		setTurnRight(90);
	}

	public void onHitRobot(HitRobotEvent e) {
		// frágil em combate prolongado: bate e sai
		estado = FUGA;
		ticksFuga = 0;
	}

	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}

	Color corBase = new Color(0, 100, 60);

	/** Sem barra de vida: quanto menos energia, mais escuro o robô (como no jogo). */
	void corPelaEnergia() {
		double f = Math.max(0.2, Math.min(1.0, getEnergy() / 100.0));
		setBodyColor(new Color((int) (corBase.getRed() * f), (int) (corBase.getGreen() * f), (int) (corBase.getBlue() * f)));
	}

	public void cores() {
		setBodyColor(corBase);
		setGunColor(new Color(212, 175, 55));
		setRadarColor(new Color(212, 175, 55));
		setBulletColor(Color.cyan);
		setScanColor(Color.cyan);
	}
}
