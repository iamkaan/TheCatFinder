package com.iamkaan.util.model;

import com.iamkaan.listener.StationVisitListener;
import com.iamkaan.util.StationManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * very simple cat model, walking around, probably searching for some food
 */
public class Cat extends Creature {

    public int id;
    public boolean isFound;

    public Cat(int id, int currentStationNumber, StationVisitListener listener) {
        super(id, currentStationNumber, listener);

        this.currentStationNumber = currentStationNumber;
        this.listener = listener;

        isFound = false;

        listener.onVisit(currentStationNumber, this);
    }

    @Override
    public void goToNextStation() {
        List<Object> choices = Arrays.asList(
                new HashMap<Integer, Station>(StationManager.getStation(currentStationNumber).connections)
                        .values().toArray());
        while (choices.size() > 0) {
            int random = (int) (Math.random() * choices.size());
            if (((Station) choices.get(random)).isClosed) {
                choices.remove(random);
            } else {
                if (!isFound) {
                    listener.onVisit(((Station) choices.get(random)).number, this);
                }
                return;
            }
        }
    }
}
