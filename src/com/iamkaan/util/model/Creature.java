package com.iamkaan.util.model;

import com.iamkaan.listener.StationVisitListener;

/**
 * super class for human and cat
 */
public abstract class Creature {
    public int id;
    public int currentStationNumber;

    StationVisitListener listener;

    public Creature(int id, int currentStationNumber, StationVisitListener listener) {
        this.id = id;
        this.currentStationNumber = currentStationNumber;
        this.listener = listener;
    }

    public abstract void goToNextStation();
}
