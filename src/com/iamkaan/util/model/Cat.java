package com.iamkaan.util.model;

import com.iamkaan.listener.StationVisitListener;
import com.iamkaan.util.StationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * very simple cat model, walking around, probably searching for some food
 */
public class Cat extends Creature {

    public Cat(int id, int currentStationNumber, StationVisitListener listener) {
        super(id, currentStationNumber, listener);

        this.currentStationNumber = currentStationNumber;
        this.listener = listener;
    }

    /**
     * creates a list of possible next stations and randomly chooses between the ones which is not closed
     */
    @Override
    public void visitNextStation() {
        List<Object> choices = new ArrayList<Object>(Arrays.asList(
                new HashMap<Integer, Station>(StationManager.getManager().getStation(currentStationNumber).connections)
                        .values().toArray()));
        while (choices.size() > 0) {
            int random = (int) (Math.random() * choices.size());
            if (((Station) choices.get(random)).isClosed) {
                choices.remove(random);
            } else {
                listener.onVisit(((Station) choices.get(random)).number, this);
                return;
            }
        }
    }
}
