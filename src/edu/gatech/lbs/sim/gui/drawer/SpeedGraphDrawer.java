package edu.gatech.lbs.sim.gui.drawer;

import edu.gatech.lbs.core.logging.MetricsManager;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.gui.SimPanel;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SpeedGraphDrawer implements IDrawer {
    private static final Integer WIDTH = 750;
    private static final Integer HEIGHT = 50;
    private static final Integer X0 = 895;
    private static final Integer Y0 = 335;
    private static final Integer GRAPH_Y0 = 845;
    private static final Integer GRAPH_WIDTH = 750;
    private static final Integer GRAPH_HEIGHT = 450;
    private static final Double X_TICK = 0.625;

    private SimPanel panel;
    private int iteration = 0;
    private Map<Integer, Double> speedToPercentageMap;
    private Map<Integer, ArrayList<Double[]>> speedToXHistoryMap;
    private Map<Integer, ArrayList<Double[]>> speedToYHistoryMap;

    public SpeedGraphDrawer(SimPanel panel) {
        this.panel = panel;
        this.speedToPercentageMap = new HashMap<Integer, Double>();
        this.speedToXHistoryMap = new HashMap<>();
        this.speedToYHistoryMap = new HashMap<>();

        speedToXHistoryMap.put(0, new ArrayList<>());
        speedToXHistoryMap.put(10, new ArrayList<>());
        speedToXHistoryMap.put(20, new ArrayList<>());
        speedToXHistoryMap.put(30, new ArrayList<>());
        speedToXHistoryMap.put(40, new ArrayList<>());
        speedToXHistoryMap.put(50, new ArrayList<>());
        speedToXHistoryMap.put(60, new ArrayList<>());
        speedToXHistoryMap.put(70, new ArrayList<>());

        speedToYHistoryMap.put(0, new ArrayList<>());
        speedToYHistoryMap.put(10, new ArrayList<>());
        speedToYHistoryMap.put(20, new ArrayList<>());
        speedToYHistoryMap.put(30, new ArrayList<>());
        speedToYHistoryMap.put(40, new ArrayList<>());
        speedToYHistoryMap.put(50, new ArrayList<>());
        speedToYHistoryMap.put(60, new ArrayList<>());
        speedToYHistoryMap.put(70, new ArrayList<>());

        speedToPercentageMap.put(0, 0.0);
        speedToPercentageMap.put(10, 0.0);
        speedToPercentageMap.put(20, 0.0);
        speedToPercentageMap.put(30, 0.0);
        speedToPercentageMap.put(40, 0.0);
        speedToPercentageMap.put(50, 0.0);
        speedToPercentageMap.put(60, 0.0);
        speedToPercentageMap.put(70, 0.0);
    }

    @Override
    public void draw(Graphics g) {
        TreeMap<Integer, Set<SimAgent>> speedRangeToAgentMap = MetricsManager.getSpeedRangeToAgentMap();
        int totalAgentCount = 0;

        Iterator it = speedRangeToAgentMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, HashSet<SimAgent>> pair = (Map.Entry)it.next();
            totalAgentCount += pair.getValue().size();
        }
        double x1 = X0;

        Graphics2D g2 = (Graphics2D) g;

        /*
           draw proportional graph
         */
        it = speedRangeToAgentMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, HashSet<SimAgent>> pair = (Map.Entry)it.next();
            double proportionWidth = (WIDTH * ((double)pair.getValue().size() / (double)totalAgentCount));
            g2.setColor(getColorPerMPH(pair.getKey()));
            g2.fill(new Rectangle2D.Double(x1, Y0, proportionWidth, HEIGHT));
            x1 += proportionWidth;
        }

        /*
           draw line graph
         */
        g2.setColor(Color.black);
        g2.drawLine(X0, GRAPH_Y0, X0 + GRAPH_WIDTH, GRAPH_Y0);
        g2.drawLine(X0, GRAPH_Y0 - GRAPH_HEIGHT, X0, GRAPH_Y0);
        g2.drawString("10%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.1));
        g2.drawString("20%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.2));
        g2.drawString("30%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.3));
        g2.drawString("40%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.4));
        g2.drawString("50%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.5));
        g2.drawString("60%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.6));
        g2.drawString("70%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.7));
        g2.drawString("80%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.8));
        g2.drawString("90%", (float)(X0 - 30), (float)(GRAPH_Y0 - (GRAPH_HEIGHT)*0.9));


        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 2), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 2), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 3), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 3), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 4), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 4), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 5), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 5), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 6), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 6), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 7), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 7), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 8), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 8), (float)(GRAPH_Y0 - 5)));
        g2.draw(new Line2D.Double((float)(X0 + X_TICK * 120 * 9), (float)GRAPH_Y0, (float)(X0 + X_TICK * 120 * 9), (float)(GRAPH_Y0 - 5)));

        g2.drawString("1 min", (float)(X0 + X_TICK * 120 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("2 min", (float)(X0 + X_TICK * 120 * 2 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("3 min", (float)(X0 + X_TICK * 120 * 3 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("4 min", (float)(X0 + X_TICK * 120 * 4 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("5 min", (float)(X0 + X_TICK * 120 * 5 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("6 min", (float)(X0 + X_TICK * 120 * 6 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("7 min", (float)(X0 + X_TICK * 120 * 7 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("8 min", (float)(X0 + X_TICK * 120 * 8 - 17), (float)GRAPH_Y0 + 20);
        g2.drawString("9 min", (float)(X0 + X_TICK * 120 * 9 - 17), (float)GRAPH_Y0 + 20);

        it = speedRangeToAgentMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, HashSet<SimAgent>> pair = (Map.Entry)it.next();
            double proportion = ((double)pair.getValue().size() / (double)totalAgentCount);
            g2.setColor(getColorPerMPH(pair.getKey()));
//            g2.draw(new Line2D.Double(X0 + iteration * X_TICK, GRAPH_Y0 - (GRAPH_HEIGHT * speedToPercentageMap.get(pair.getKey())),
//                    X0 + iteration * X_TICK + X_TICK, GRAPH_Y0 - (GRAPH_HEIGHT * proportion)));

            Double[] xHistoryPair = new Double[2];
            xHistoryPair[0] = X0 + iteration * X_TICK;
            xHistoryPair[1] = X0 + iteration * X_TICK + X_TICK;

            List<Double[]> xHistoryList = speedToXHistoryMap.get(pair.getKey());
            xHistoryList.add(xHistoryPair);

            Double[] yHistoryPair = new Double[2];
            yHistoryPair[0] = GRAPH_Y0 - (GRAPH_HEIGHT * speedToPercentageMap.get(pair.getKey()));
            yHistoryPair[1] = GRAPH_Y0 - (GRAPH_HEIGHT * proportion);

            List<Double[]> yHistoryList = speedToYHistoryMap.get(pair.getKey());
            yHistoryList.add(yHistoryPair);

            for (int i = 0; i < xHistoryList.size(); i++) {
                Double[] xHistory = xHistoryList.get(i);
                Double[] yHistory = yHistoryList.get(i);

                g2.draw(new Line2D.Double(xHistory[0], yHistory[0], xHistory[1], yHistory[1]));
            }

            speedToPercentageMap.put(pair.getKey(), proportion);
        }

        ++iteration;
    }

    private Color getColorPerMPH(int mph) {
        if (mph == 0) {
            return Color.DARK_GRAY;
        } else if (mph == 10) {
            return Color.decode("#561a56");
        } else if (mph == 20) {
            return Color.BLUE;
        } else if (mph == 30) {
            return Color.decode("#55DDE0");
        } else if (mph == 40) {
            return Color.decode("#26C485");
        } else if (mph == 50) {
            return Color.ORANGE;
        } else if (mph == 60) {
            return Color.MAGENTA;
        } else {
            return Color.RED;
        }
    }
}
