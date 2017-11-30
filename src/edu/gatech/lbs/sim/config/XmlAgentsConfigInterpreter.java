// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.config.helper.XmlMobilityModelInterpreter;
import edu.gatech.lbs.sim.config.helper.XmlQueryModelInterpreter;
import edu.gatech.lbs.sim.config.paramparser.IParamParser;
import edu.gatech.lbs.sim.config.paramparser.TimeParser;
import edu.gatech.lbs.sim.scheduling.activity.PeriodicLocationTraceSavingActivity;

import static edu.gatech.lbs.sim.Simulation.AGENT_COUNT_OVERRIDE_KEY;

public class XmlAgentsConfigInterpreter implements IXmlConfigInterpreter {

  protected SimAgent makeAgent(Simulation sim, int simAgentId) {
    return new SimAgent(sim, simAgentId);
  }

  public void initFromXmlElement(Element rootNode, Simulation sim, Map<String, String> configOverride) {
    // groups of agents:
    Collection<SimAgent> agents = new ArrayList<SimAgent>();
    NodeList agentsNodes = rootNode.getElementsByTagName("agents");
    for (int i = 0; i < agentsNodes.getLength(); i++) {
      Element agentsNode = (Element) agentsNodes.item(i);

      int agentCount = configOverride.containsKey(AGENT_COUNT_OVERRIDE_KEY) && configOverride.get(AGENT_COUNT_OVERRIDE_KEY).matches("^-?\\d+$")
              ? Integer.parseInt(configOverride.get(AGENT_COUNT_OVERRIDE_KEY)) : Integer.parseInt(agentsNode.getAttribute("count"));

      for (int simAgentId = 0; simAgentId < agentCount; simAgentId++) {
        agents.add(makeAgent(sim, simAgentId));
      }
      sim.setAgents(agents);

      // mobility model:
      Element mobilitymodelNode = (Element) agentsNode.getElementsByTagName("mobilitymodel").item(0);
      XmlMobilityModelInterpreter mmInterpreter = new XmlMobilityModelInterpreter();
      mmInterpreter.initFromXmlElement(mobilitymodelNode, sim, configOverride);

      // query model:
      Element querymodelNode = (Element) agentsNode.getElementsByTagName("querymodel").item(0);
      XmlQueryModelInterpreter qmInterpreter = new XmlQueryModelInterpreter(mobilitymodelNode.getAttribute("filename"));
      qmInterpreter.initFromXmlElement(querymodelNode, sim, configOverride);

      // periodic location trace saving:
      Element periodicTraceNode = (Element) agentsNode.getElementsByTagName("periodictraceoutput").item(0);
      if (periodicTraceNode != null) {
        String snapshotTraceFilename = periodicTraceNode.getAttribute("filename");
        IParamParser pparser2 = new TimeParser();
        int period = pparser2.parse(periodicTraceNode.getAttribute("period"));
        sim.addActivity(new PeriodicLocationTraceSavingActivity(snapshotTraceFilename, period));
      }
    }
  }
}
