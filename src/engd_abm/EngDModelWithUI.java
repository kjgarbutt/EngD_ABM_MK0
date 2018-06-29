package engd_abm;

import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class EngDModelWithUI extends GUIState {
	
	public Display2D display;
	public JFrame displayFrame;

	FieldPortrayal2D cityPortrayal = new SparseGridPortrayal2D();
	GeomVectorFieldPortrayal regionPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal countryPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal countryBndPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal roadPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal roadLinkPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal flood3Portrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal flood2Portrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal refugeePortrayal = new GeomVectorFieldPortrayal();

	public EngDModelWithUI(EngDModel sim) {
		super(sim);
	}

	@Override
	public void init(Controller c) {
		super.init(c);
		display = new Display2D(750, 520, this);
		
		display.attach(regionPortrayal, "Regions");
		display.attach(countryPortrayal, "Counties (area)");
		display.attach(countryBndPortrayal, "Countries (boundary)");
		// display.attach(roadPortrayal, "Roads");
		display.attach(cityPortrayal, "Cities");
		display.attach(flood2Portrayal, "Flood Zone #2");
		display.attach(flood3Portrayal, "Flood Zone #3");
		display.attach(roadLinkPortrayal, "Routes");
		display.attach(refugeePortrayal, "Refugees");

		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		displayFrame.setSize(800, 600);
		display.setBackdrop(Color.WHITE);
		displayFrame.setTitle("EngD ABM Model");
	}

	@Override
	public void start() {
		super.start();
		setupPortrayals();

		EngDModel engdModelSim = (EngDModel) state;

	}

	public void setupPortrayals() {
		System.out.println("Setting up Portrayals...");

		cityPortrayal.setField(((EngDModel) state).cityGrid);
		cityPortrayal.setPortrayalForAll(new OvalPortrayal2D(new Color(255,
				137, 95), 5.0, true));
		roadLinkPortrayal.setField(((EngDModel) state).roadLinks);
		roadLinkPortrayal.setPortrayalForAll(new GeomPortrayal(new Color(255,
				77, 166), 1, true));
		
		roadPortrayal.setField(((EngDModel) this.state).roads);
		roadPortrayal.setPortrayalForAll(new GeomPortrayal(Color.DARK_GRAY,
				0.0005, false));
		
		regionPortrayal.setField(((EngDModel) state).regions);
		regionPortrayal.setPortrayalForAll(new GeomPortrayal(new Color(128,
				128, 128), 2, false));
		
		countryPortrayal.setField(((EngDModel) state).countries);
		countryPortrayal.setPortrayalForAll(new GeomPortrayal(new Color(226,
				198, 141), 1, true));
		
		countryBndPortrayal.setField(((EngDModel) state).countries);
		countryBndPortrayal.setPortrayalForAll(new GeomPortrayal(new Color(64,
				64, 64), 1, false));

		flood2Portrayal.setField(((EngDModel) this.state).flood2);
		flood2Portrayal.setPortrayalForAll(new GeomPortrayal(Color.BLUE, true));

		flood3Portrayal.setField(((EngDModel) this.state).flood3);
		flood3Portrayal.setPortrayalForAll(new GeomPortrayal(Color.CYAN, true));

		//agentsPortrayal.setField(EngDModelBuilder.agents);
		//agentsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.MAGENTA,
				//150, true));

		display.reset();
		display.setBackdrop(Color.WHITE);
		display.repaint();
	}

	@Override
	public void quit() {
		System.out.println("EngDModelWithUI quitting...");
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;

	}

	public static void main(String[] args) {
		EngDModelWithUI ebUI = new EngDModelWithUI(new EngDModel(
				System.currentTimeMillis()));
		Console c = new Console(ebUI);
		c.setVisible(true);
	}
}