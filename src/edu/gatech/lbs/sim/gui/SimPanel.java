// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.NumberFormatter;

import edu.gatech.lbs.core.FileHelper;
import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.world.BoundingBox;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.gui.drawer.IDrawer;

import static edu.gatech.lbs.sim.Simulation.AGENT_COUNT_OVERRIDE_KEY;

public class SimPanel extends JPanel {
  protected Image image;

  protected boolean doPause = false;

  protected List<IDrawer> drawers;

  protected Simulation sim;

  public double mmPerPixel;
  protected final double zoomRate = 2;

  protected BoundingBox bounds;

  private JFormattedTextField agentCountField;
  private String configFileName;

  public static SimPanel makeGui(Simulation sim) {
    JFrame frame = new JFrame("GT Mobile Agent Simulator (gt-mobisim)");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    SimPanel panel = new SimPanel(sim);
    panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    panel.setBorder(new LineBorder(Color.BLACK));

    frame.add(panel);

    frame.pack();
    frame.setVisible(true);

    return panel;
  }

  public void setConfigFileName(String configFileName) {
    this.configFileName = configFileName;
  }

  public SimPanel(Simulation sim2) {
    this.sim = sim2;

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    rightPanel.setBackground(Color.white);

    setBorder(BorderFactory.createLineBorder(Color.black));
    setBackground(Color.WHITE);

    rightPanel.add(getControlPanel());
    rightPanel.add(getConfigurationPanel());

    add(rightPanel);

    setFocusable(true);
    this.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
      }

      public void keyTyped(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
        long x0 = bounds.getX0();
        long y0 = bounds.getY0();
        double m = 1;
        switch (e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
          x0 -= bounds.getWidth() / 10;
          break;
        case KeyEvent.VK_RIGHT:
          x0 += bounds.getWidth() / 10;
          break;
        case KeyEvent.VK_UP:
          y0 += bounds.getHeight() / 10;
          break;
        case KeyEvent.VK_DOWN:
          y0 -= bounds.getHeight() / 10;
          break;
        case KeyEvent.VK_ADD:
          m /= zoomRate;
          break;
        case KeyEvent.VK_SUBTRACT:
          m *= zoomRate;
          break;
        case KeyEvent.VK_SPACE:
          doPause = !doPause;
          break;
        }
        bounds = new BoundingBox(x0 + (long) (bounds.getWidth() * (1 - m) / 2), y0 + (long) (bounds.getHeight() * (1 - m) / 2), (long) (bounds.getWidth() * m), (long) (bounds.getHeight() * m));
      }
    });
/*
    this.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        // JOptionPane.showMessageDialog(null, e.getLocationOnScreen().x + " px, " + e.getLocationOnScreen().y + " px\n" + loc + "\n" + loc.toRoadnetVector((RoadMap) sim.getWorld()));

        double m = 1;
        switch (e.getButton()) {
        case MouseEvent.BUTTON1:
          m /= zoomRate;
          break;
        case MouseEvent.BUTTON3:
          m *= zoomRate;
          break;
        }
        zoom(getLocation(bounds, new Point(e.getX(), e.getY())), m);
      }
    });

    this.addMouseWheelListener(new MouseAdapter() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        double m = (e.getWheelRotation() > 0 ? zoomRate : 1 / zoomRate);
        zoom(getLocation(bounds, new Point(e.getX(), e.getY())), m);
      }
    });
*/
    this.addComponentListener(new ComponentAdapter() {
      // This method is called after the component's size changes
      public void componentResized(ComponentEvent evt) {
        Component c = (Component) evt.getSource();
        Dimension newSize = c.getSize();
      }
    });

    image = null;
  }

  private JPanel getControlPanel() {
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
    controlPanel.setBackground(Color.WHITE);

    JButton pauseButton = new JButton("|| >");

    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doPause = !doPause;
      }
    });

    controlPanel.add(pauseButton);

    JButton restartButton = new JButton("Apply and Restart");
    restartButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Map<String, String> overrideData = new HashMap<String, String>();

        // only use the override field if it has been changed
        if (agentCountField != null && sim.getAgents().size() != Integer.parseInt(agentCountField.getText().replace(",", ""))) {
          overrideData.put(AGENT_COUNT_OVERRIDE_KEY, agentCountField.getText().replace(",", ""));
        }

        sim.killSwitchOn(overrideData);
      }
    });

    controlPanel.add(restartButton);

    JButton configEditButton = new JButton("Edit Configuration File");
    configEditButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showConfigWindow();
      }
    });

    controlPanel.add(configEditButton);
    return controlPanel;
  }

  private JPanel getConfigurationPanel() {
    JPanel configPanel = new JPanel();
    configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
    configPanel.setBackground(Color.WHITE);

    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setValueClass(Integer.class);
    formatter.setMinimum(0);
    formatter.setMaximum(Integer.MAX_VALUE);
    formatter.setAllowsInvalid(false);
    // If you want the value to be committed on each keystroke instead of focus lost
    formatter.setCommitsOnValidEdit(true);
    JFormattedTextField agentCountField = new JFormattedTextField(formatter);
    this.agentCountField = agentCountField;

    agentCountField.setText(Integer.toString(sim.getAgentCount()));

    JLabel agentCountLabel = new JLabel("Agent count: ");
    configPanel.add(agentCountLabel);
    configPanel.add(agentCountField);

    JLabel worldBoundsLabel = new JLabel("World Bounds: " + sim.getWorld().getBounds().toString() + "\n");
    configPanel.add(worldBoundsLabel);

    Collection<RoadSegment> segs = ((RoadMap)sim.getWorld()).getRoadSegments();

    double lengthTotal = 0, lengthMin = Double.MAX_VALUE, lengthMax = Double.MIN_VALUE;
    int pointsTotal = 0, pointsMin = Integer.MAX_VALUE, pointsMax = Integer.MIN_VALUE;
    double travelTimeTotal = 0;

    for (RoadSegment segment : segs) {
      double length = segment.getLength() / 1000.0;
      lengthTotal += length;
      lengthMin = Math.min(lengthMin, length);
      lengthMax = Math.max(lengthMax, length);

      int points = segment.getGeometry().getPoints().length;
      pointsTotal += points;
      pointsMin = Math.min(pointsMin, points);
      pointsMax = Math.max(pointsMax, points);

      travelTimeTotal += segment.getLength() / (double) segment.getSpeedLimit();
    }
    int segmentCount = segs.size();
    double lengthAvg = lengthTotal / (double) segmentCount;
    double pointsAvg = pointsTotal / (double) segmentCount;
    double travelTimeAvg = travelTimeTotal / (double) segmentCount;

    configPanel.add(new JLabel("Segment totals: count= " + segmentCount + ", length= " + String.format("%.1f", lengthTotal / 1000) + " km (" + String.format("%.1f", travelTimeTotal / 3600) + " h), points= " + pointsTotal));
    configPanel.add(new JLabel("  length per segment: avg= " + String.format("%.1f", lengthAvg) + " m (" + String.format("%.1f", travelTimeAvg) + " sec), min= " + String.format("%.1f", lengthMin) + " m, max= " + String.format("%.1f", lengthMax) + " m"));
    configPanel.add(new JLabel("  points per segment: avg= " + String.format("%.1f", pointsAvg) + ", min= " + pointsMin + ", max= " + pointsMax + " " + "\n"));
    configPanel.add(new JLabel("    "));
    configPanel.add(new JLabel("Color Legend (Car Status): \n"));

    JLabel parkedLabel = new JLabel("parked (Black) \n");
    parkedLabel.setForeground(Color.BLACK);
    JLabel under10Label = new JLabel("< 10 mph\n");
    under10Label.setForeground(Color.BLUE);
    JLabel under20Label = new JLabel("< 20 mph\n");
    under20Label.setForeground(Color.CYAN);
    JLabel under30Label = new JLabel("< 30 mph\n");
    under30Label.setForeground(Color.GREEN);
    JLabel under40Label = new JLabel("< 40 mph\n");
    under40Label.setForeground(Color.YELLOW);
    JLabel under50Label = new JLabel("< 50 mph\n");
    under50Label.setForeground(Color.ORANGE);
    JLabel under60Label = new JLabel("< 60 mph\n");
    under60Label.setForeground(Color.MAGENTA);
    JLabel elseLabel = new JLabel("> 60 mph\n");
    elseLabel.setForeground(Color.RED);

    configPanel.add(parkedLabel);
    configPanel.add(under10Label);
    configPanel.add(under20Label);
    configPanel.add(under30Label);
    configPanel.add(under40Label);
    configPanel.add(under50Label);
    configPanel.add(under60Label);
    configPanel.add(elseLabel);

    configPanel.add(new JLabel("    "));
    JLabel graphNameLabel = new JLabel("Speed Proportion");
    configPanel.add(graphNameLabel);

    return configPanel;
  }

  protected void zoom(CartesianVector center, double times) {
    bounds = new BoundingBox((long) (center.getX() - (center.getX() - bounds.getX0()) * times), (long) (center.getY() - (center.getY() - bounds.getY0()) * times), (long) (bounds.getWidth() * times), (long) (bounds.getHeight() * times));
  }

  public void setDrawers(List<IDrawer> drawers) {
    this.drawers = drawers;
  }

  public Dimension getPreferredSize() {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();

    return new Dimension(screenSize.width - 10, screenSize.height - 60);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // put the offscreen image on the screen:
    g.drawImage(image, 0, 0, null);
  }

  public void redrawSim() {
    // ensure off-screen image reflects component size:
    Dimension d = getSize();
    Image imageNew = createImage(d.width, d.height);

    Graphics g = imageNew.getGraphics();
    g.setColor(getBackground());
    g.fillRect(0, 0, d.width, d.height);

    RoadMap roadmap = (RoadMap) sim.getWorld();
    if (bounds == null) {
      bounds = roadmap.getBounds();
    }

    mmPerPixel = Math.max(bounds.getWidth() / getWidth(), bounds.getHeight() / getHeight());
    // Ensure that mm/pixel is a power of 2.
    mmPerPixel = Math.pow(2, 1 + (int) (Math.log(mmPerPixel) / Math.log(2)));

    for (IDrawer drawer : drawers) {
      drawer.draw(g);
    }

    image = imageNew;

    // pause simulation:
    try {
      if (doPause) {
        doScreenshot();
      }
      while (doPause) {
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      System.out.println("Thread interrupted: " + e.getMessage());
    }
  }

  protected void doScreenshot() {
    try {
      File dir = new File("screenshots");
      if (!dir.exists()) {
        dir.mkdir();
      }
      File imageFile = new File("screenshots/sim_" + sim.getTime() / 1000 + "s.png");
      ImageIO.write((RenderedImage) image, "png", imageFile);
    } catch (IOException e) {
      System.out.println("Couldn't write screenshot file.");
    }
  }

  private void showConfigWindow() {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      InputStream in = FileHelper.openFileOrUrl(configFileName);
      stringBuilder.append(FileHelper.getContentsFromInputStream(in));
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    final String contents = stringBuilder.toString();

    final JFrame configWindow = new JFrame();
    final JTextArea configText = new JTextArea(40,10);
    JScrollPane scroll = new JScrollPane(configText);

    configText.setText(contents);
    JPanel configPanel = new JPanel();
    configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
    configPanel.add(scroll);

    JButton saveAndCloseButton = new JButton("                                                            Save and Close                                                                 ");
    saveAndCloseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          File configFile = new File(configFileName);
          System.out.println(configFileName);
          FileWriter fileWriter = new FileWriter(configFile, false);
          fileWriter.write(configText.getText());
          fileWriter.close();


        } catch (IOException ie) {
          ie.printStackTrace();
        }

        configWindow.dispose();
      }
    });

    configPanel.add(saveAndCloseButton);
    configWindow.add(configPanel);
    configWindow.pack();
    configWindow.setVisible(true);
  }

  public Point getPixel(CartesianVector vector) {
    return getPixel(bounds, vector);
  }

  protected Point getPixel(BoundingBox bounds, CartesianVector vector) {
    return new Point((int) ((vector.getX() - bounds.getX0()) / mmPerPixel), (int) ((bounds.getNorthBoundary() - vector.getY()) / mmPerPixel));
  }

  public CartesianVector getLocation(BoundingBox bounds, Point px) {
    return new CartesianVector((long) (px.x * mmPerPixel + bounds.getX0()), (long) (bounds.getNorthBoundary() - px.y * mmPerPixel));
  }
}
