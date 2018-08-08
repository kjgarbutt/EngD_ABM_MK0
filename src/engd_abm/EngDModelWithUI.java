package engd_abm;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class EngDModelWithUI extends GUIState {
	
	public Display2D display;
	public JFrame displayFrame;

	GeomVectorFieldPortrayal osviPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal boundaryPortrayal = new GeomVectorFieldPortrayal();
	//GeomVectorFieldPortrayal centroidsPortrayal = new GeomVectorFieldPortrayal();
	FieldPortrayal2D centroidsPortrayal = new SparseGridPortrayal2D();
	GeomVectorFieldPortrayal roadsPortrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal flood3Portrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal flood2Portrayal = new GeomVectorFieldPortrayal();
	GeomVectorFieldPortrayal agentsPortrayal = new GeomVectorFieldPortrayal();

	public EngDModelWithUI(EngDModel sim) {
		super(sim);
	}

	@Override
	public void init(Controller c) {
		super.init(c);
		display = new Display2D(750, 520, this);

		display.attach(boundaryPortrayal, "Boundary");
		display.attach(osviPortrayal, "OSVI");
		display.attach(roadsPortrayal, "Roads");
		display.attach(flood2Portrayal, "Flood Zone #2");
		display.attach(flood3Portrayal, "Flood Zone #3");
		display.attach(centroidsPortrayal, "Centroids");
		display.attach(agentsPortrayal, "Agents");

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
		setupFixedPortrayals();
		setupMovingPortrayals();

		EngDModel engdModelSim = (EngDModel) state;

	}

	public void setupFixedPortrayals() {
		System.out.println("Setting up Fixed Portrayals...");

		centroidsPortrayal.setField(((EngDModel) this.state).cityGrid);
		centroidsPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.GREEN, true));
		
		flood2Portrayal.setField(((EngDModel) this.state).flood2);
		flood2Portrayal.setPortrayalForAll(new GeomPortrayal(Color.BLUE, true));
		
		roadsPortrayal.setField(((EngDModel) this.state).roads);
		roadsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.DARK_GRAY,
				0.0005, false));

		flood2Portrayal.setField(((EngDModel) this.state).flood2);
		flood2Portrayal.setPortrayalForAll(new GeomPortrayal(Color.BLUE, true));

		flood3Portrayal.setField(((EngDModel) this.state).flood3);
		flood3Portrayal.setPortrayalForAll(new GeomPortrayal(Color.CYAN, true));

		osviPortrayal.setField(((EngDModel) this.state).lsoa);
		osviPortrayal.setPortrayalForAll(new GeomPortrayal(Color.PINK,
				true));
		
		boundaryPortrayal.setField(((EngDModel) this.state).boundary);
		boundaryPortrayal.setPortrayalForAll(new GeomPortrayal(Color.YELLOW,
				true));

	}
	
	public void setupMovingPortrayals() {
		System.out.println("Setting up Moving Portrayals...");
		centroidsPortrayal.setPortrayalForAll(new OvalPortrayal2D() {

			private static final long serialVersionUID = 546102092597315413L;

				@Override
	            public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	            {
	                EngDCentroid centroid = (EngDCentroid)object;

	                Rectangle2D.Double draw = info.draw;
	                int agent_pop = centroid.getAgentPopulation();
	                paint = new Color(255, 255, 051);	//YELLOW
	                Double scale = 5.0;
	                if(agent_pop == 0) {
	                	scale = 5.0;
	                	paint = new Color(110, 94, 0);	//BROWN
	                }
	                else if(agent_pop > 0 && agent_pop <= EngDParameters.TOTAL_POP * 0.3) {
	                	scale = 15.0;
	                	paint = new Color(255, 132, 255);	//PINK
	                	//paint = new Color(163, 177, 255);
	                }
	                else if(agent_pop > EngDParameters.TOTAL_POP * 0.3 && agent_pop <= EngDParameters.TOTAL_POP*0.6){
	                	scale = 25.0;
	                	paint = new Color(255, 123, 43);	//ORANGE
	                	//paint = new Color(177, 138, 255);
	                }
	                else if(agent_pop > EngDParameters.TOTAL_POP*0.6){
	                	scale = 40.0;
	                	paint = new Color(0, 218, 36);	//GREEN
	                	//paint = new Color(177, 138, 255);
	                }
	                final double width = draw.width*scale + offset;
	                final double height = draw.height*scale + offset;

	                graphics.setPaint(paint);
	                final int x = (int)(draw.x - width / 2.0);
	                final int y = (int)(draw.y - height / 2.0);
	                int w = (int)(width);
	                int h = (int)(height);
	                        
	                // draw centered on the origin
	                if (filled)
	                    graphics.fillOval(x,y,w,h);
	                else
	                    graphics.drawOval(x,y,w,h);

	            }
	        });
	  
		
		//agentsPortrayal.setField(EngDModelBuilder.agents);
		//agentsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.MAGENTA,
				//150, true));
		agentsPortrayal.setField(EngDModelBuilder.agents);
		//agentsPortrayal.setField(((EngDModel) this.state).world);
		agentsPortrayal.setPortrayalForAll(new GeomPortrayal
        		(Color.MAGENTA, 150, true));
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