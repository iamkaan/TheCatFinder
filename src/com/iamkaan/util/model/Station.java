package com.iamkaan.util.model;

import java.util.HashMap;

/**
 * TFL station model
 */
public class Station {

    public int number;
    public String name;

    public int visitCount;

    // we could just remove station from connections list of connected stations
    // instead of checking isClosed value every time
    // but that could be nice to add a feature that TFL staff could open the station after cleaning
    public boolean isClosed;

    public HashMap<Integer, Station> connections;
    public HashMap<Integer, Human> peopleInside;
    public HashMap<Integer, Cat> catsInside;

    public Station(int number, String name) {
        this.number = number;
        this.name = name;
        visitCount = 0;
        isClosed = false;
        connections = new HashMap<Integer, Station>();
        peopleInside = new HashMap<Integer, Human>();
        catsInside = new HashMap<Integer, Cat>();
    }
}
