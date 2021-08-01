package i5.las2peer.services.ocd.cooperation.simulation.game;

/**
 * Constructs {@link Game} Instances.
 *
 */
public class GameFactory {

	public static GameFactory getInstance() {
		return new GameFactory();
	}

	public GameFactory() {

	}

	/**
	 * Builds a game based on the four specified payoff values.
	 * 
	 * @param AA
	 * @param AB
	 * @param BA
	 * @param BB
	 * @return Game
	 */
	public Game build(double AA, double AB, double BA, double BB) {

		Game game = new Game(AA, AB, BA, BB);
		return (game);
	}

	///// Cost Variant /////

	/**
	 * Builds the cost variant of a game based on the cost, benefit values and
	 * the specified game type.
	 * 
	 * @param gameType
	 * @param cost
	 * @param benefit
	 * @return Game
	 */
	public Game build(GameType gameType, double cost, double benefit) {
		
		if(Math.abs(cost) > benefit)
			throw new IllegalArgumentException();
		
		switch (gameType) {
		case PRISONERS_DILEMMA:
		case PRISONERS_DILEMMA_COST:
			return buildCostPD(cost, benefit);
		case CHICKEN:
		case CHICKEN_COST:
			return buildCostSD(cost, benefit);
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Builds the cost variant of a Prisoner's Dilemma.
	 * 
	 * @param cost
	 * @param benefit
	 * @return Prisoner's dilemma game
	 */
	protected Game buildCostPD(double cost, double benefit) {

		cost = Math.abs(cost);
		double AA = benefit - cost;
		double AB = -cost;
		double BA = benefit;
		double BB = 0.0;
		return (build(AA, AB, BA, BB));
	}

	/**
	 * Builds the cost variant of a Snow Drift / Chicken game.
	 * 
	 * @param cost
	 * @param benefit
	 * @return Snow Drift game
	 */
	protected Game buildCostSD(double cost, double benefit) {

		cost = Math.abs(cost);
		double AA = benefit - (0.5 * cost);
		double AB = benefit - cost;
		double BA = benefit;
		double BB = 0.0;
		return (build(AA, AB, BA, BB));
	}

}
