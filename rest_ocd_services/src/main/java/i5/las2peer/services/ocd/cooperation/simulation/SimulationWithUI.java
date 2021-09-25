package i5.las2peer.services.ocd.cooperation.simulation;

import java.awt.Color;

import javax.swing.JFrame;

import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cooperation.simulation.game.Game;
import i5.las2peer.services.ocd.cooperation.simulation.termination.Condition;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.TabbedInspector;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;

/**
 * Provides a UI for the {@link Simulation} class. Used only in UI mode.
 */
public class SimulationWithUI extends GUIState {

	public Display2D display;
	public JFrame displayFrame;

	NetworkPortrayal2D networkPortrayal = new NetworkPortrayal2D();

	public static void main(String[] args) {
		SimulationWithUI vid = new SimulationWithUI();
		Console c = new Console(vid);
		c.setVisible(true);
	}

	public SimulationWithUI() {
		super(new Simulation(System.currentTimeMillis()));
	}

	public SimulationWithUI(SimState state) {
		super(state);
	}

	@Override
	public Inspector getInspector() {
		
		Simulation simulation = (Simulation) state;
		Game game = simulation.getGame();
		Dynamic dynamic = simulation.getDynamic();
		Condition condition = simulation.getBreakCondition();
		System.out.println(condition);
		
		Inspector simInspector = new SimpleInspector(simulation, this);
		Inspector gameInspector = new SimpleInspector(game, this);
		Inspector dynamicInspector = new SimpleInspector(dynamic, this);
		Inspector breakInspector = new SimpleInspector(condition, this);
		
		TabbedInspector inspector =  new sim.portrayal.inspector.TabbedInspector();
		
		inspector.addInspector(simInspector, "Simulation");
		inspector.addInspector(gameInspector, "Game");	
		inspector.addInspector(dynamicInspector, "Dynamic");	
		inspector.addInspector(breakInspector, "Break");
		
		return inspector;
	}

	public static String getName() {
		return "Cooperation & Defection";
	}

	@Override
	public void start() {
		super.start();
		setupPortrayals();
	}

	@Override
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}

	public void setupPortrayals() {
		Simulation simulation = (Simulation) state;

		networkPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());

		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);

		// redraw the display
		display.repaint();

	}

	@Override
	public void init(Controller c) {
		super.init(c);

		// make the displayer
		display = new Display2D(600, 600, this);
		// turn off clipping
		display.setClipping(false);

		displayFrame = display.createFrame();
		displayFrame.setTitle("Network Cooperation Display");
		c.registerFrame(displayFrame); // register the frame so it appears in
										// the "Display" list
		displayFrame.setVisible(true);
		display.attach(networkPortrayal, "Agents");

	}

	@Override
	public void quit() {
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;
	}

}
