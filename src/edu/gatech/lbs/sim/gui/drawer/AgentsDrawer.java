// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui.drawer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.gatech.lbs.core.logging.MetricsManager;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.gui.SimPanel;

public class AgentsDrawer implements IDrawer {
  private static final double MM_PER_SEC_TO_MPH = 0.00223694;
  public boolean isAgentVectorOn = false;

  private Simulation sim;
  private SimPanel panel;

  private HashMap<Integer, Set<SimAgent>> speedRangeToAgentMap;

  public AgentsDrawer(Simulation sim, SimPanel panel) {
    this.sim = sim;
    this.panel = panel;
    this.speedRangeToAgentMap = new HashMap<Integer, Set<SimAgent>>();

    // initialize the sets within the map
    speedRangeToAgentMap.put(10, new HashSet<SimAgent>());
    speedRangeToAgentMap.put(20, new HashSet<SimAgent>());
    speedRangeToAgentMap.put(30, new HashSet<SimAgent>());
    speedRangeToAgentMap.put(40, new HashSet<SimAgent>());
    speedRangeToAgentMap.put(50, new HashSet<SimAgent>());
    speedRangeToAgentMap.put(60, new HashSet<SimAgent>());
    speedRangeToAgentMap.put(70, new HashSet<SimAgent>()); // for anything over 60mph
  }

  public void draw(Graphics g) {
    Collection<SimAgent> agents = sim.getAgents();
    for (SimAgent agent : agents) {

      IVector loc = agent.getLocation();
      Point p0 = panel.getPixel(loc.toCartesianVector());
      double mph = agent.getLocation().toRoadnetVector().toTangentVector().times(1e-6 * agent.getVelocity().getLength()).toCartesianVector().getLength() * MM_PER_SEC_TO_MPH;
      double oldMph = agent.getMph();
      agent.setMph(mph);
      g.setColor(getColorPerMPH(mph));
      g.fillOval(p0.x - 2, p0.y - 2, 5, 5);

      if (isAgentVectorOn) {
        IVector v = agent.getVelocity();
        Point p1 = panel.getPixel(loc.toCartesianVector().add(loc.toRoadnetVector().toTangentVector().times(1e-6 * v.getLength() * 5).toCartesianVector()).toCartesianVector());
        g.drawLine(p0.x, p0.y, p1.x, p1.y);
      }

      // assign agents to corresponding key
      MetricsManager.addAgentToSpeed(agent, oldMph, mph);
    }
  }

  private Color getColorPerMPH(double mph) {
    if (mph == 0) {
      return Color.DARK_GRAY;
    } else if (mph < 10) {
      return Color.BLUE;
    } else if (mph < 20) {
      return Color.CYAN;
    } else if (mph < 30) {
      return Color.GREEN;
    } else if (mph < 40) {
      return Color.YELLOW;
    } else if (mph < 50) {
      return Color.ORANGE;
    } else if (mph < 60) {
      return Color.MAGENTA;
    } else {
      return Color.RED;
    }
  }

  private Integer getNearestTens(double mph) {
    if (mph > 60.0) {
      return 70;
    } else if (mph == 0) {
      return 0;
    }

    return (int)(Math.ceil(mph/10.0)) * 10;
  }
}