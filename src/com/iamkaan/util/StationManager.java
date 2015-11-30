package com.iamkaan.util;

import com.iamkaan.TheCatFinder;
import com.iamkaan.listener.StationVisitListener;
import com.iamkaan.util.model.Cat;
import com.iamkaan.util.model.Creature;
import com.iamkaan.util.model.Human;
import com.iamkaan.util.model.Station;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * creates a singleton manager
 * manages all travels
 */
public class StationManager implements StationVisitListener {

    private static StationManager manager;

    public HashMap<Integer, Station> stations;
    private boolean firstGiveUpHappened = false;
    private Human neverGiveUpOwner;

    Timer runtimeTimer = new Timer();
    Timer giveUpTimer = new Timer();

    private StationManager() {
        this.stations = new HashMap<Integer, Station>();
    }

    public static StationManager getManager() {
        if (manager == null) {
            manager = new StationManager();
        }
        return manager;
    }

    public HashMap<Integer, Station> getStations() {
        if (stations == null) {
            stations = new HashMap<Integer, Station>();
        }
        return stations;
    }

    public Station getStation(int stationNumber) {
        return getStations().get(stationNumber);
    }

    /**
     * unleashing all cats and owners at the same time
     */
    public synchronized void unleashCreatures() {
        final CountDownLatch latch = new CountDownLatch(1);

        for (final Station station : stations.values()) {
            for (final Cat cat : station.catsInside.values()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        latch.countDown();
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleVisit(station.number, cat);
                    }
                }).start();
            }
            for (final Human person : station.peopleInside.values()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        latch.countDown();
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handleVisit(station.number, person);
                    }
                }).start();
            }
        }

        latch.countDown();

        // setting timer to finish program in runtime limit
        runtimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // nothing else to do, printing final information
                printResultsAndFinish();

                giveUpTimer.cancel();
            }
        }, TheCatFinder.RUNTIME_LIMIT * 1000);
    }

    @Override
    public void onVisit(final int destinationStationNumber, final Creature visitedBy) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handleVisit(destinationStationNumber, visitedBy);
            }
        }).start();
    }

    /**
     * synchronized method that handles travels of cats and owners
     * <p/>
     * if creature is already found, method does nothing
     * <p/>
     * if all owners found their cats or gave up, method does nothing
     * <p/>
     * if an owner finds his/her cat, method prints the information,
     * closes the station, marks the owner and the cat as found
     * and decreases the number of people who are still searching
     * <p/>
     * if owner cannot find his/her cat, method sends the owner to the next station
     * <p/>
     * for cats, method method doesn't check anything, sends it to the next station
     * <p/>
     * when a creature changes station, method removes it from related list of the station
     * (catsInside or peopleInside) and adds to list of the next station
     *
     * @param destinationStationNumber station number of traveler's destination
     * @param traveler                 creature that changing station
     */
    private synchronized void handleVisit(int destinationStationNumber, Creature traveler) {
        if (!traveler.found) {
            Station currentStation = stations.get(traveler.currentStationNumber);
            currentStation.visitCount++;

            if (traveler instanceof Human) {
                currentStation.peopleInside.remove(traveler.id);
                traveler.currentStationNumber = destinationStationNumber;
                currentStation = stations.get(traveler.currentStationNumber);
                currentStation.peopleInside.put(traveler.id, (Human) traveler);

                if (currentStation.catsInside.containsKey(traveler.id)) {
                    currentStation.peopleInside.get(traveler.id).found = true;
                    currentStation.catsInside.get(traveler.id).found = true;
                    currentStation.isClosed = true;

                    System.out.println("Owner " + traveler.id + " found cat " + traveler.id +
                            " - " + getStation(destinationStationNumber).name + " is now closed.");

                    if (firstGiveUpHappened) {
                        neverGiveUpOwner = (Human) traveler;
                    }
                } else {
                    if (!((Human) traveler).giveUp()) {
                        traveler.visitNextStation();
                    } else {
                        if (!firstGiveUpHappened) {
                            // printing only the first owner who gave up
                            System.out.println("Owner " + traveler.id + " gave up. :(");

                            // setting timer to finish process after the first owner gave up
                            giveUpTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    // nothing else to do, printing final information
                                    printResultsAndFinish();

                                    runtimeTimer.cancel();
                                }
                            }, TheCatFinder.TIME_OUT * 1000);

                            firstGiveUpHappened = true;
                        }
                    }
                }
            } else {
                currentStation.catsInside.remove(traveler.id);
                traveler.currentStationNumber = destinationStationNumber;
                currentStation = stations.get(traveler.currentStationNumber);
                currentStation.catsInside.put(traveler.id, (Cat) traveler);
            }
        }
    }

    /**
     * we gave a last chance to the owners but we have other jobs to do, we can't wait forever
     * method prints some statistics and then finishes the process
     */
    private void printResultsAndFinish() {
        Station mostPopularStation = null;
        Station leastPopularStation = null;
        Station stationOfTrappedCreatures = null;
        Human luckiestOwner = null;
        Human unluckiestOwner = null;
        int happyOwnerCount = 0;
        int trappedOwnerCount = 0;
        int totalNumberOfStationsHappyOwnersVisited = 0;

        for (Station station : stations.values()) {
            boolean isTrapStation = false;
            if (mostPopularStation == null || station.visitCount > mostPopularStation.visitCount) {
                mostPopularStation = station;
            }
            if (leastPopularStation == null || station.visitCount < leastPopularStation.visitCount) {
                leastPopularStation = station;
            }
            for (Human owner : station.peopleInside.values()) {
                if (owner.found) {
                    happyOwnerCount++;
                    totalNumberOfStationsHappyOwnersVisited += owner.visitedStationCount;
                    if (luckiestOwner == null ||
                            owner.visitedStationCount < luckiestOwner.visitedStationCount) {
                        luckiestOwner = owner;
                    }
                } else if (owner.gotTrapped) {
                    isTrapStation = true;
                    trappedOwnerCount++;
                    if (unluckiestOwner == null ||
                            owner.visitedStationCount < unluckiestOwner.visitedStationCount) {
                        unluckiestOwner = owner;
                    }
                }
            }
            int peopleTrappedInsideCount = 0;
            int catsTrappedInsideCount = 0;
            int stationOfTrappedPeopleTrappedInsideCount = 0;
            int stationOfTrappedCatsTrappedInsideCount = 0;

            if (isTrapStation) {
                if (station.peopleInside != null) {
                    peopleTrappedInsideCount = station.peopleInside.size();
                }
                if (station.catsInside != null) {
                    catsTrappedInsideCount = station.catsInside.size();
                }
                if (stationOfTrappedCreatures != null && stationOfTrappedCreatures.peopleInside != null) {
                    peopleTrappedInsideCount = stationOfTrappedCreatures.peopleInside.size();
                }
                if (stationOfTrappedCreatures != null && stationOfTrappedCreatures.catsInside != null) {
                    catsTrappedInsideCount = stationOfTrappedCreatures.catsInside.size();
                }
                if (stationOfTrappedCreatures == null ||
                        peopleTrappedInsideCount + catsTrappedInsideCount >
                                stationOfTrappedPeopleTrappedInsideCount + stationOfTrappedCatsTrappedInsideCount) {
                    stationOfTrappedCreatures = station;
                }
            }
        }

        // calculated the average number using only the successful owners' data
        String ave;
        if (happyOwnerCount == 0) {
            ave = "Nobody found their cats. There is no average number! No.. Nothing.. :(";
        } else {
            ave = String.valueOf(totalNumberOfStationsHappyOwnersVisited / happyOwnerCount);
        }

        System.out.println("\nTotal number of cats: " + TheCatFinder.N + "\n" +
                "Number of cats found: " + happyOwnerCount + "\n" +
                "Average number of movements required to find a cat: " + ave);

        if (mostPopularStation != null) {
            System.out.println("\nThe most popular station is: " +
                    mostPopularStation.number + ". " + mostPopularStation.name + " with " +
                    mostPopularStation.visitCount + " visit" +
                    (mostPopularStation.visitCount > 1 ? "s" : "") +
                    ".");
        }

        if (leastPopularStation != null) {
            if (leastPopularStation.visitCount > 0) {
                System.out.println("The least popular station is: " +
                        leastPopularStation.number + ". " + leastPopularStation.name + " with " +
                        leastPopularStation.visitCount + " visit" +
                        (leastPopularStation.visitCount > 1 ? "s" : "") +
                        ".");
            } else {
                System.out.println("Fun fact: nobody visited the station " +
                        leastPopularStation.number + ". " + leastPopularStation.name + ".");
            }
        }

        if (luckiestOwner != null) {
            System.out.println("\nOwner " + luckiestOwner.id + " is the luckiest one. " +
                    "Found his/her cat in " + luckiestOwner.visitedStationCount + " step" +
                    (luckiestOwner.visitedStationCount > 1 ? "s" : "") +
                    ".");
        }

        if (unluckiestOwner != null) {
            System.out.println("\nOwner " + unluckiestOwner.id + " got trapped after " +
                    "he/she visited just " + unluckiestOwner.visitedStationCount + " station" +
                    (unluckiestOwner.visitedStationCount > 1 ? "s" : "") +
                    ". Poor guy :(");
            if (trappedOwnerCount == 1) {
                System.out.println("Even worse thing is, he/she was the only one who got trapped!");
            } else {
                System.out.println("Well, he/she was not the only one. " +
                        "There are " + (trappedOwnerCount - 1) + " other " +
                        (unluckiestOwner.visitedStationCount > 1 ? "people" : "person") +
                        " who got trapped.");
            }
        } else {
            System.out.println("Nobody got trapped! YAY!");
        }

        if (stationOfTrappedCreatures != null) {
            System.out.println("\nThe total number of " +
                    (stationOfTrappedCreatures.peopleInside.size() > 0 ?
                            stationOfTrappedCreatures.peopleInside.size() + " owner" +
                                    (stationOfTrappedCreatures.peopleInside.size() > 1 ? "s" : "") : "") +
                    (stationOfTrappedCreatures.peopleInside.size() > 0 && stationOfTrappedCreatures.catsInside.size() > 0 ?
                            " and " : "") +
                    (stationOfTrappedCreatures.catsInside.size() > 0 ?
                            stationOfTrappedCreatures.catsInside.size() + " cat" +
                                    (stationOfTrappedCreatures.catsInside.size() > 1 ? "s" : "") : "") +
                    " got trapped in " +
                    stationOfTrappedCreatures.number + ". " + stationOfTrappedCreatures.name + " station.\n" +
                    "At least, they are not alone there.");
        }

        if (neverGiveUpOwner != null) {
            System.out.println("\nThe owner " + neverGiveUpOwner.id + " didn't give up when others did and" +
                    " found his/her cat in the last " + TheCatFinder.TIME_OUT + " second" +
                    (TheCatFinder.TIME_OUT > 1 ? "s" : "") +
                    ". NEVER GIVE UP!");
        }

        System.exit(0);
    }
}
