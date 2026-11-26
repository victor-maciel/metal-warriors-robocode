package MetalWarriors;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

/**
 * Spider - a robot by Victor Maciel
 * Spider - Inspirado num robô do jogo Metal Warriors do Super Nintendo.
 * 		Furtivo e de emboscada: anda grudado nas paredes (as seis pernas que
 * 		escalam paredes e teto), contornando a arena pelo perímetro. De tempos
 * 		em tempos fica parado, camuflado, e só ataca se o inimigo chegar perto.
 * 		Frágil, então inverte o sentido quando leva tiro.
 * 		--> A IDEIA NÃO É VENCER E SIM SIMULAR O OS PADRÕES DO JOGO NO ROBÔ <--
 * Projeto MetalWarriors: https://github.com/victor-maciel/metal-warriors-robocode
 */
public class Spider extends AdvancedRobot {
	int sentido = 1;                   // 1 = horário, -1 = anti-horário
	static final double MARGEM = 30;   // distância da parede
	static final double CURVA = 60;    // quanto antes do canto começa a virar

	// camuflagem / emboscada: para de se mover e só ataca se o inimigo chegar perto
	int camuflado = 0;                 // ticks restantes parado
	int ticksAteCamuflar = 80;
	static final double ALCANCE_EMBOSCADA = 250;

	public void run() {
		cores();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		irParaParede();

		while (true) {
			// radar sempre varrendo a arena
			setTurnRadarRight(360);

			// alterna entre andar pela parede e ficar camuflado
			if (camuflado > 0) {
				camuflado--;
				if (camuflado == 0) {
					setMaxVelocity(6);
				}
			} else if (--ticksAteCamuflar <= 0) {
				camuflado = 40;
				ticksAteCamuflar = 100 + (int) (Math.random() * 100);
				setMaxVelocity(0);
			}

			// só decide curva quando terminou a anterior
			if (getTurnRemaining() == 0) {
				if (distanciaAteParedeNaFrente() < CURVA) {
					setTurnRight(90 * sentido);
				}
				setAhead(10000 * sentido);
			}
			corPelaEnergia();
			execute();
		}
	}

	/** Vai até a parede mais próxima e se alinha paralelo a ela. */
	void irParaParede() {
		double x = getX(), y = getY();
		double w = getBattleFieldWidth(), h = getBattleFieldHeight();

		double dEsq = x, dDir = w - x, dBaixo = y, dCima = h - y;
		double menor = Math.min(Math.min(dEsq, dDir), Math.min(dBaixo, dCima));

		double alvo; // heading apontando para a parede
		if (menor == dCima) {
			alvo = 0;
		} else if (menor == dDir) {
			alvo = 90;
		} else if (menor == dBaixo) {
			alvo = 180;
		} else {
			alvo = 270;
		}

		turnRight(Utils.normalRelativeAngleDegrees(alvo - getHeading()));
		ahead(Math.max(0, menor - MARGEM));
		turnRight(90 * sentido); // fica paralelo à parede
	}

	/** Distância até a parede na direção em que o robô está de fato se movendo. */
	double distanciaAteParedeNaFrente() {
		double mov = getHeading() + (sentido > 0 ? 0 : 180);
		int lado = (int) Math.round(Utils.normalAbsoluteAngleDegrees(mov) / 90.0) % 4;
		switch (lado) {
			case 0:  return getBattleFieldHeight() - getY();
			case 1:  return getBattleFieldWidth() - getX();
			case 2:  return getY();
			default: return getX();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double mira = Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading());
		setTurnGunRight(mira);

		// camuflado: não se revela atirando em quem está longe
		if (camuflado > 0 && e.getDistance() > ALCANCE_EMBOSCADA) {
			return;
		}

		// tiros leves (drone); emboscada = golpe forte e volta a andar
		// perto: golpe forte das lâminas da cabeça
		double poder = e.getDistance() < 100 ? 3 : (e.getDistance() < 250 ? 1.5 : 1);
		if (camuflado > 0) {
			poder = 3;
			camuflado = 0;
			setMaxVelocity(6);
		}
		if (getEnergy() < 10) {
			poder = 0.5;
		}
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 8) {
			setFire(poder);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		sentido = -sentido;
		setTurnRight(0);
		setAhead(10000 * sentido);
	}

	public void onHitWall(HitWallEvent e) {
		sentido = -sentido;
		setAhead(10000 * sentido);
	}

	public void onHitRobot(HitRobotEvent e) {
		sentido = -sentido;
		setAhead(10000 * sentido);
		double mira = Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading());
		setTurnGunRight(mira);
		if (getGunHeat() == 0) {
			setFire(2);
		}
	}

	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}

	Color corBase = new Color(100, 100, 110);

	/** Sem barra de vida: quanto menos energia, mais escuro o robô (como no jogo). */
	void corPelaEnergia() {
		double f = Math.max(0.2, Math.min(1.0, getEnergy() / 100.0));
		setBodyColor(new Color((int) (corBase.getRed() * f), (int) (corBase.getGreen() * f), (int) (corBase.getBlue() * f)));
	}

	public void cores() {
		setBodyColor(corBase);
		setGunColor(new Color(0, 160, 0));
		setRadarColor(new Color(0, 160, 0));
		setBulletColor(Color.green);
		setScanColor(Color.green);
	}
}
