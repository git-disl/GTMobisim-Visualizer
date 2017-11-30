package edu.gatech.lbs.core.logging;

import edu.gatech.lbs.sim.agent.SimAgent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SOM2 on 11/16/17.
 */

// Get syncrhonized
public class MetricsManager {
    private static Map<Integer, AtomicInteger> roadSegmentTravelledCountPerSpeedLimit;  // Key: SpeedLimit of the Road Segment, Value:
    private static AtomicInteger parkCount = new AtomicInteger(0);
    private static TreeMap<Integer, Set<SimAgent>> speedRangeToAgentMap;

    //multiple thread and needs to be syncrhonized (AtomicInteger syncrhonized)
    public static void addTravelledCountPerSpeedLimit(Integer speedLimit) { //Road segment speed limit
        if (roadSegmentTravelledCountPerSpeedLimit == null) {
            roadSegmentTravelledCountPerSpeedLimit = new ConcurrentHashMap<>();
        }

        if (roadSegmentTravelledCountPerSpeedLimit.containsKey(speedLimit)) {
            roadSegmentTravelledCountPerSpeedLimit.get(speedLimit).getAndIncrement();
        } else {
            roadSegmentTravelledCountPerSpeedLimit.put(speedLimit, new AtomicInteger(1));
        }
    }
    public static Integer getParkCount(){
        return parkCount.get();
    }
    public static  void incrementPark(){
        parkCount.getAndIncrement();
    }

    public static Map<Integer, AtomicInteger> getRoadSegmentTravelledCountPerSpeedLimit() {
        return roadSegmentTravelledCountPerSpeedLimit;
    }

    public static void addAgentToSpeed(SimAgent agent, double oldMph, double newMph) {
        if (speedRangeToAgentMap == null) {
            initSpeedRangeToAgentMap();
        } else {
            // remove from the map if the map has already been initialized
            speedRangeToAgentMap.get(getNearestTens(oldMph)).remove(agent);
        }
        speedRangeToAgentMap.get(getNearestTens(newMph)).add(agent);
    }

    public static TreeMap<Integer, Set<SimAgent>> getSpeedRangeToAgentMap() {
        return speedRangeToAgentMap;
    }

    private static Integer getNearestTens(double mph) {
        if (mph > 60.0) {
            return 70;
        } else if (mph == 0) {
            return 0;
        }

        return (int)(Math.ceil(mph/10.0)) * 10;
    }

    private static void initSpeedRangeToAgentMap() {
        speedRangeToAgentMap = new TreeMap<Integer, Set<SimAgent>>();

        // initialize the sets within the map
        speedRangeToAgentMap.put(0, new HashSet<SimAgent>());
        speedRangeToAgentMap.put(10, new HashSet<SimAgent>());
        speedRangeToAgentMap.put(20, new HashSet<SimAgent>());
        speedRangeToAgentMap.put(30, new HashSet<SimAgent>());
        speedRangeToAgentMap.put(40, new HashSet<SimAgent>());
        speedRangeToAgentMap.put(50, new HashSet<SimAgent>());
        speedRangeToAgentMap.put(60, new HashSet<SimAgent>());
        speedRangeToAgentMap.put(70, new HashSet<SimAgent>()); // for anything over 60mph
    }
}
