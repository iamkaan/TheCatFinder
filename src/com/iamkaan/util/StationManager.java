package com.iamkaan.util;

import com.iamkaan.listener.StationVisitListener;
import com.iamkaan.util.model.Cat;
import com.iamkaan.util.model.Creature;
import com.iamkaan.util.model.Human;
import com.iamkaan.util.model.Station;

import java.util.HashMap;

/**
 * creates a singleton station list
 * manages all travels
 */
public class StationManager implements StationVisitListener {

    private static HashMap<Integer, Station> stations;

    public static HashMap<Integer, Station> getStations() {
        if (stations == null) {
            stations = new HashMap<Integer, Station>();
        }
        return stations;
    }

    public static Station getStation(int stationNumber) {
        return getStations().get(stationNumber);
    }

    @Override
    public void onVisit(final int destinationStationNumber, final Creature visitedBy) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                handleVisit(destinationStationNumber, visitedBy);
            }
        });
        thread.start();
    }

    private synchronized void handleVisit(int destinationStationNumber, Creature traveler) {
        System.out.println(traveler.getClass().getName() + " " + traveler.id + " visited " + destinationStationNumber);

        Station currentStation = stations.get(traveler.currentStationNumber);

        if (traveler instanceof Human) {
            currentStation.peopleInside.remove(traveler.id);
            traveler.currentStationNumber = destinationStationNumber;
            currentStation.peopleInside.put(traveler.id, (Human) traveler);

            if (currentStation.catsInside.containsKey(traveler.id)) {
                currentStation.catsInside.get(traveler.id).isFound = true;
                currentStation.isClosed = true;
            } else {
                traveler.goToNextStation();
            }
        } else {
            currentStation.catsInside.remove(traveler.id);
            traveler.currentStationNumber = destinationStationNumber;
            currentStation.catsInside.put(traveler.id, (Cat) traveler);

            traveler.goToNextStation();
        }
    }
}
