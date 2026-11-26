package MetalWarriors;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

/**
 * Ballistic - a robot by Victor Maciel
 * Ballistic - Inspirado num robô do jogo Metal Warriors do Super Nintendo.
 * 		Ataque em alta velocidade e tiros carregados:
 * 		- ROLANDO: vira esfera e avança a toda velocidade (power spin).
 * 		  NÃO atira enquanto está em movimento (fraqueza do robô).
 * 		- CARREGANDO: para, mira e dispara o canhão de plasma (tiro forte),
 * 		  depois volta a rolar.
 * 		--> A IDEIA NÃO É VENCER E SIM SIMULAR O OS PADRÕES DO JOGO NO ROBÔ <--
 * Projeto MetalWarriors: https://github.com/victor-maciel/metal-warriors-robocode
 */
public class Ballistic extends AdvancedRobot {
	static final int ROLANDO = 0;
	static final int CARREGANDO = 1;

	int estado = ROLANDO;
	int ticksCarga = 0;

	// último alvo visto
	boolean temAlvo = false;
	double alvoAbs = 0;       // direção absoluta (graus) até o inimigo
	double alvoDist = 9999;
	long ultimoScan = -100;

	public void run() {
		cores();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		while (true) {
			if (getRadarTurnRemaining() == 0) {
				setTurnRadarRight(360);
			}
			if (getTime() - ultimoScan > 20) {
				temAlvo = false;
			}

			if (estado == ROLANDO) {
				rolar();
			} else {
				carregar();
			}
			corPelaEnergia();
			execute();
		}
	}

	/** Esfera rolando: muito rápido, sem atirar. */
	void rolar() {
		setMaxVelocity(8);
		if (temAlvo) {
			// muito rápido, porém mais voltado para fuga: com pouca energia foge
			boolean fugindo = getEnergy() < 30;
			double rumo = fugindo ? alvoAbs + 180 : alvoAbs;
			setTurnRight(Utils.normalRelativeAngleDegrees(rumo - getHeading()));
			setAhead(fugindo ? 200 : alvoDist);
			if (alvoDist < 170 && !fugindo) {
				estado = CARREGANDO;
				ticksCarga = 0;
			}
		} else {
			// sem alvo: gira procurando
			setTurnRight(30);
			setAhead(150);
		}
	}

	/** Parado, carregando o canhão de plasma. */
	void carregar() {
		setMaxVelocity(0); // para de se mover
		setAhead(0);
		ticksCarga++;

		// inimigo fugiu ou sumiu: volta a rolar
		if (!temAlvo || alvoDist > 320) {
			estado = ROLANDO;
			return;
		}

		// só atira quando está totalmente parado, mirando e com a carga pronta
		if (ticksCarga >= 20 && getVelocity() == 0
				&& getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 6) {
			// plasma carregável (estilo Megaman): quanto mais carga, mais forte
			setFire(Math.min(getEnergy() > 20 ? 3 : 1.5, 0.5 + ticksCarga * 0.1));
			estado = ROLANDO;
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		alvoAbs = Utils.normalAbsoluteAngleDegrees(getHeading() + e.getBearing());
		alvoDist = e.getDistance();
		temAlvo = true;
		ultimoScan = getTime();

		// trava radar e mira o canhão
		double anguloAbsRad = getHeadingRadians() + e.getBearingRadians();
		setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(anguloAbsRad - getRadarHeadingRadians()));
		setTurnGunRight(Utils.normalRelativeAngleDegrees(alvoAbs - getGunHeading()));
	}

	/** Bateu rolando: metralhadora à queima-roupa (parado pelo impacto). */
	public void onHitRobot(HitRobotEvent e) {
		setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
		if (getGunHeat() == 0 && getVelocity() == 0) {
			setFire(1);
		}
	}

	public void onHitWall(HitWallEvent e) {
		setBack(60);
		setTurnRight(90);
	}

	public void onWin(WinEvent e) {
		setMaxVelocity(8);
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}

	Color corBase = new Color(70, 80, 120);

	/** Sem barra de vida: quanto menos energia, mais escuro o robô (como no jogo). */
	void corPelaEnergia() {
		double f = Math.max(0.2, Math.min(1.0, getEnergy() / 100.0));
		setBodyColor(new Color((int) (corBase.getRed() * f), (int) (corBase.getGreen() * f), (int) (corBase.getBlue() * f)));
	}

	public void cores() {
		setBodyColor(corBase);
		setGunColor(Color.red);
		setRadarColor(Color.red);
		setBulletColor(Color.red);
		setScanColor(Color.red);
	}
}
