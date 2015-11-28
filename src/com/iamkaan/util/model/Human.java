package com.iamkaan.util.model;

import com.iamkaan.listener.StationVisitListener;
import com.iamkaan.util.StationManager;

import java.util.HashMap;

/**
 * very simple but intelligent enough human model
 */
public class Human extends Creature {

    //holds <station number, number of visits>
    HashMap<Integer, Integer> visitedStations;

    public Human(int id, int currentStationNumber, StationVisitListener listener) {
        super(id, currentStationNumber, listener);

        this.currentStationNumber = currentStationNumber;
        this.listener = listener;

        visitedStations = new HashMap<Integer, Integer>();
        visitedStations.put(currentStationNumber, 1);

        listener.onVisit(currentStationNumber, this);
    }

    @Override
    public void goToNextStation() {
        int leastVisitedStationNumber = -1;
        int lowestVisitCount = -1;

        for (Station connection : StationManager.getStation(currentStationNumber).connections.values()) {
            if (connection.isClosed) {
                continue;
            }
            if (!visitedStations.containsKey(connection.number)) {
                visitedStations.put(connection.number, 1);
                listener.onVisit(connection.number, this);
                return;
            } else {
                if (leastVisitedStationNumber == -1 || visitedStations.get(connection.number) < lowestVisitCount) {
                    lowestVisitCount = visitedStations.get(connection.number);
                    leastVisitedStationNumber = connection.number;
                }
            }
        }

        if (leastVisitedStationNumber != -1) {
            visitedStations.put(leastVisitedStationNumber, ++lowestVisitCount);
            listener.onVisit(leastVisitedStationNumber, this);
        } else {
            //nowhere to go!
        }
    }
}
