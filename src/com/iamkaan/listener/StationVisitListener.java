package com.iamkaan.listener;

import com.iamkaan.util.model.Creature;

/**
 * to listen when a creature visits a station
 */
public interface StationVisitListener {
    void onVisit(int destinationStationNumber, Creature visitedBy);
}
