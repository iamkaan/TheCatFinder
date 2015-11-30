package com.iamkaan.util.model;

import com.iamkaan.TheCatFinder;
import com.iamkaan.listener.StationVisitListener;
import com.iamkaan.util.StationManager;

import java.util.Collection;
import java.util.HashMap;

/**
 * very simple but intelligent enough human model
 */
public class Human extends Creature {

    //holds <station number, number of visits>
    HashMap<Integer, Integer> visitedStations;
    public int visitedStationCount;
    public boolean gotTrapped = false;

    public Human(int id, int currentStationNumber, StationVisitListener listener) {
        super(id, currentStationNumber, listener);

        this.currentStationNumber = currentStationNumber;
        this.listener = listener;
        visitedStationCount = 1;

        visitedStations = new HashMap<Integer, Integer>();
        visitedStations.put(currentStationNumber, 1);
    }

    /**
     * checks all possible connections for the next station and chooses the least visited one
     * if there is no connection available, prints the information that owner got trapped.
     */
    @Override
    public void visitNextStation() {
        visitedStationCount++;

        int leastVisitedStationNumber = -1;
        int lowestVisitCount = -1;

        Collection<Station> connections = StationManager.getManager().getStation(currentStationNumber).connections.values();
        for (Station connection : connections) {
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
            gotTrapped = true;
            System.out.println("Owner " + id + " got trapped in " +
                    StationManager.getManager().stations.get(currentStationNumber).name);
        }
    }

    /**
     * check if the owner reached the give up limit (number of stations visited in total)
     *
     * @return true if the owner gave up
     */
    public boolean giveUp() {
        if (TheCatFinder.GIVE_UP_LIMIT == -1) {
            return false;
        }
        return visitedStationCount >= TheCatFinder.GIVE_UP_LIMIT;
    }
}
