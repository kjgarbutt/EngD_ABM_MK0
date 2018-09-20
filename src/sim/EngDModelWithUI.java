package sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import org.jfree.data.xy.XYSeries;

import entities.EngDAgent;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.media.chart.TimeSeriesChartGenerator;
import utilities.EngDParameters;

public class EngDModelWithUI extends GUIState {

	public Display2D display;
	public JFrame displayFrame;

	GeomVectorFieldPortrayal boundaryPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal lsoaPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal osviPortrayal = new GeomVectorFieldPortrayal(true);
	GeomVectorFieldPortrayal roadsPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal flood2Portrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal flood3Portrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal centroidsPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal agentsPortrayal = new GeomVectorFieldPortrayal();
	TimeSeriesChartGenerator trafficChart;
	XYSeries maxSpeed;
	XYSeries avgSpeed;
	XYSeries minSpeed;

	public EngDModelWithUI(EngDModel sim) {
		super(sim);
	}

	public void setupPortrayals() {
		EngDModel engdModelSim = (EngDModel) state;
		System.out.println("Setting up Portrayals...");
		System.out.println();
		osviPortrayal.setField(engdModelSim.osvi);
		osviPortrayal.setPortrayalForAll(new PolyPortrayal());

		display.reset();
		display.repaint();
	}

	public static void main(String[] args) {
		EngDModelWithUI ebUI = new EngDModelWithUI(new EngDModel(System.currentTimeMillis()));
		Console c = new Console(ebUI);
		c.setVisible(true);
	}

	/**
     * //////////////////////// Simulation Name //////////////////////////////
     * @return name of the simulation
     */
	public static String getName() {
		return "EngD ABM Model MK_0";
	}

	/**
     *  /////////////////////// Model Modification ///////////////////////////
     *  This must be included to have model tab, which allows mid-simulation
     *  modification of the coefficients
     */
	public Object getSimulationInspectedObject() {
		return state;
	}

	/**
     * //////////////////////// Model Setup //////////////////////////////////
     * Called when starting a new run of the simulation. Sets up the portrayals
     * and chart data.
     */
	@Override
	public void start() {
		super.start();
		setupPortrayals();

		EngDModel engdModelSim = (EngDModel) state;

		maxSpeed = new XYSeries("Max Speed");
		avgSpeed = new XYSeries("Average Speed");
		minSpeed = new XYSeries("Min Speed");
		trafficChart.removeAllSeries();
		trafficChart.addSeries(maxSpeed, null);
		trafficChart.addSeries(avgSpeed, null);
		trafficChart.addSeries(minSpeed, null);

		state.schedule.scheduleRepeating(new Steppable() {
			private static final long serialVersionUID = -3749005402522867098L;

			public void step(SimState state) {
				EngDModel engdModelSim = (EngDModel) state;
				double maxS = 0, minS = 10000, avgS = 0, count = 0;
				//////////////////////////// Main Agent //////////////////////
				for (EngDAgent a : engdModelSim.agentList) {
					if (a.reachedGoal) {
						continue;
					}
					count++;
					double speed = Math.abs(a.speed);
					avgS += speed;
					if (speed > maxS) {
						maxS = speed;
					}
					if (speed < minS) {
						minS = speed;
					}
				}

				double time = state.schedule.time();
				avgS /= count;
				maxSpeed.add(time, maxS, true);
				minSpeed.add(time, minS, true);
				avgSpeed.add(time, avgS, true);
			}
		});

		/**
    	 * Sets up the portrayals within the map visualization.
    	 */
		roadsPortrayal.setField(engdModelSim.roads);
		roadsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.DARK_GRAY, 0.0005, false));

		osviPortrayal.setField(engdModelSim.osvi);
		osviPortrayal.setPortrayalForAll(new PolyPortrayal());

		flood2Portrayal.setField(engdModelSim.flood2);
		flood2Portrayal.setPortrayalForAll(new GeomPortrayal(Color.BLUE, true));

		flood3Portrayal.setField(engdModelSim.flood3);
		flood3Portrayal.setPortrayalForAll(new GeomPortrayal(Color.CYAN, true));

		lsoaPortrayal.setField(engdModelSim.lsoa);
		lsoaPortrayal.setPortrayalForAll(new GeomPortrayal(Color.PINK, true));

		boundaryPortrayal.setField(engdModelSim.boundary);
		boundaryPortrayal.setPortrayalForAll(new GeomPortrayal(Color.YELLOW, true));

		centroidsPortrayal.setField(engdModelSim.centroids);
		centroidsPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.GREEN, 3, true));

		agentsPortrayal.setField(engdModelSim.agents);
		agentsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.MAGENTA, 150, true));

		display.reset();
		display.setBackdrop(Color.WHITE);
		display.repaint();
	}

	/**
     * /////////////////////// Poly Portrayal Colours ////////////////////////
     * The portrayal used to display Polygons with the appropriate color
     * */
	class PolyPortrayal extends GeomPortrayal {

		@Override
		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			Polygon poly = (Polygon) object;

			if (poly.getSoc().equals("Red")) {
				paint = Color.red;
			}

			else if (poly.getSoc().equals("Orange")) {
				paint = Color.orange;
			}

			else if (poly.getSoc().equals("Yellow")) {
				paint = Color.yellow;
			}

			else if (poly.getSoc().equals("Green")) {
				paint = Color.green;
			} else {
				paint = Color.gray;
			}

			super.draw(object, graphics, info);
		}

	}

	@Override
	public void init(Controller c) {
		super.init(c);
		display = new Display2D(750, 520, this);

		// Put portrayals in order from bottom layer to top
		display.attach(boundaryPortrayal, "Boundary");
		display.attach(osviPortrayal, "OSVI");
		display.attach(lsoaPortrayal, "LSOA");
		display.attach(flood2Portrayal, "Flood Zone #2");
		display.attach(flood3Portrayal, "Flood Zone #3");
		display.attach(roadsPortrayal, "Roads");
		display.attach(centroidsPortrayal, "Centroids");

		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		displayFrame.setSize(800, 600);
		display.setBackdrop(Color.WHITE);
		displayFrame.setTitle("EngD ABM Model");

		trafficChart = new TimeSeriesChartGenerator();
		trafficChart.setTitle("Traffic Stats");
		trafficChart.setYAxisLabel("Speed");
		trafficChart.setXAxisLabel("Time");
		JFrame chartFrame = trafficChart.createFrame(this);
		chartFrame.pack();
		c.registerFrame(chartFrame);
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
}