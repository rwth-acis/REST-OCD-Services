package i5.las2peer.services.ocd.cooperation.simulation.game;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class GameTypeTest {

	String string;
	GameType gameType;

	double payoffAA;
	double payoffAB;
	double payoffBA;
	double payoffBB;

	@Test
	public void fromStringInvalid() {

		string = "";
		gameType = GameType.fromString(string);
		assertEquals(GameType.INVALID, gameType);

		string = "skldsgiehgdg";
		gameType = GameType.fromString(string);
		assertEquals(GameType.INVALID, gameType);
	}

	@Test
	public void fromStringPrisonersDilemma() {

		string = "Prisoner's dilemma";
		gameType = GameType.fromString(string);
		assertEquals(GameType.PRISONERS_DILEMMA, gameType);

		string = "PD";
		gameType = GameType.fromString(string);
		assertEquals(GameType.PRISONERS_DILEMMA, gameType);

		string = "PRIsONErS_DIlEMMA";
		gameType = GameType.fromString(string);
		assertEquals(GameType.PRISONERS_DILEMMA, gameType);

	}

	@Test
	public void fromStringSnowDrift() {

		string = "Chicken";
		gameType = GameType.fromString(string);
		assertEquals(GameType.CHICKEN, gameType);

		string = "SD";
		gameType = GameType.fromString(string);
		assertEquals(GameType.CHICKEN, gameType);

		string = "Snow Drift";
		gameType = GameType.fromString(string);
		assertEquals(GameType.CHICKEN, gameType);

	}

	@Test
	public void fromStringCustom() {

		string = "custom";
		gameType = GameType.fromString(string);
		assertEquals(GameType.CUSTOM, gameType);

		string = "CTM";
		gameType = GameType.fromString(string);
		assertEquals(GameType.CUSTOM, gameType);

	}

	@Test
	public void getGameTypeInvalid() {

		payoffAA = 0;
		payoffAB = 0;
		payoffBA = 0;
		payoffBB = 0;
		gameType = GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
		assertEquals(GameType.INVALID, gameType);
	}

	@Test
	public void getGameTypePD() {

		payoffAA = 0.5;
		payoffAB = 0;
		payoffBA = 1;
		payoffBB = 0;
		gameType = GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
		assertEquals(GameType.PRISONERS_DILEMMA, gameType);
		
		payoffAA = 1.4;
		payoffAB = -1;
		payoffBA = 1.6;
		payoffBB = 0.1;
		gameType = GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
		assertEquals(GameType.PRISONERS_DILEMMA, gameType);
		
	}

	@Test
	public void getGameTypeSD() {

		payoffAA = 2;
		payoffAB = 1;
		payoffBA = 3;
		payoffBB = 0;
		gameType = GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
		assertEquals(GameType.CHICKEN, gameType);
		
		payoffAA = 1.4;
		payoffAB = -0.1;
		payoffBA = 1.6;
		payoffBB = -0.2;
		gameType = GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
		assertEquals(GameType.CHICKEN, gameType);
		
	}

	@Test
	public void getGameTypeCTM() {

		payoffAA = 2;
		payoffAB = 3;
		payoffBA = 1;
		payoffBB = 4;
		gameType = GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
		assertEquals(GameType.CUSTOM, gameType);

	}

}
