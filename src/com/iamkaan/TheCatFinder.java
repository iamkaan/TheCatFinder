package com.iamkaan;

import com.iamkaan.util.StationManager;
import com.iamkaan.util.model.Cat;
import com.iamkaan.util.model.Human;
import com.iamkaan.util.model.Station;

import java.io.*;
import java.util.Random;

/**
 * main class
 */
public class TheCatFinder {

    private static final StationManager stationManager = StationManager.getManager();

    // number of cat owners (better to keep it under 1000)
    public static final int N = 1000;

    // number of stations for an owner to visit before giving up
    // to make them never give up, make the limit -1 but
    // I suggest to keep it around 3000 because at some point I lose my hope /:
    public static final int GIVE_UP_LIMIT = 3000;

    // after the first owner gives up, other owners have TIME_OUT time left
    // to find their cats before finishing the process and printing results
    // unit: seconds
    public static final int TIME_OUT = 7;

    // this is the time limit program can run
    // unit: seconds
    public static final int RUNTIME_LIMIT = 30;

    public static void main(String[] args) {
        loadStationsFromCSV();
        addConnectionsFromCSV();

        // creating owners and cats and placing them to random stations
        for (int i = 0; i < N; i++) {
            Random random = new Random();
            int humanRandom = random.nextInt(stationManager.stations.size());
            Human person = new Human(i, (Integer) stationManager.stations.keySet().toArray()[humanRandom], stationManager);
            stationManager.stations.get(person.currentStationNumber).peopleInside.put(person.id, person);

            int catRandom = random.nextInt(stationManager.stations.size());
            while (catRandom == humanRandom) {
                catRandom = random.nextInt(stationManager.stations.size());
            }
            Cat cat = new Cat(i, (Integer) stationManager.stations.keySet().toArray()[catRandom], stationManager);
            stationManager.stations.get(cat.currentStationNumber).catsInside.put(cat.id, cat);
        }

        stationManager.unleashCreatures();
    }

    private static void loadStationsFromCSV() {
        try {
            InputStream in = TheCatFinder.class.getResourceAsStream("assets/tfl_stations.csv");
            Reader reader = new InputStreamReader(in, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] stationLine = line.split(",");
                Station station = new Station(Integer.parseInt(stationLine[0]), stationLine[1]);
                stationManager.getStations().put(station.number, station);
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addConnectionsFromCSV() {
        BufferedReader bufferedReader = null;

        try {
            InputStream in =
                    TheCatFinder.class.getResourceAsStream("assets/tfl_connections.csv");
            Reader reader = new InputStreamReader(in, "utf-8");

            bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] connectionLine = line.split(",");
                Station station1 = stationManager.getStation(Integer.parseInt(connectionLine[0]));
                Station station2 = stationManager.getStation(Integer.parseInt(connectionLine[1]));
                station1.connections.put(station2.number, station2);
                station2.connections.put(station1.number, station1);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
