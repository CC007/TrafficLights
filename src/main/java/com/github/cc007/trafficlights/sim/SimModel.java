/*-----------------------------------------------------------------------
 * Copyright (C) 2001 Green Light District Team, Utrecht University
 *
 * This program (Green Light District) is free software.
 * You may redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by
 * the Free Software Foundation (version 2 or later).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * See the documentation of Green Light District for further information.
 *------------------------------------------------------------------------*/
package com.github.cc007.trafficlights.sim;

import com.github.cc007.trafficlights.GLDSim;
import com.github.cc007.trafficlights.Model;

import com.github.cc007.trafficlights.algo.dp.*;
import com.github.cc007.trafficlights.algo.edit.ShortestPathCalculator;
import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.sim.stats.TrackerFactory;
import com.github.cc007.trafficlights.utils.Arrayutils;
import com.github.cc007.trafficlights.utils.NumberDispenser;
import com.github.cc007.trafficlights.xml.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * The heart of the simulation.
 *
 * @author Group Model
 * @version 1.0
 */
public class SimModel extends Model implements XMLSerializable {

    /**
     * The pseudo-random-number-generator we need in this simulation
     */
    protected Random generator;
    /**
     * The second thread that runs the actual simulation
     */
    protected SimModelThread thread;
    /**
     * The SimController
     */
    protected SimController controller;
    /**
     * The current cycle we're in
     */
    protected int curCycle;
    /* number of roadusers waiting in queues at all edges */
    protected int numWaiting = 0;
    /**
     * The Driving Policy in this Simulation
     */
    protected static DrivingPolicy dp;
    /**
     * The TrafficLightControlling Algorithm
     */
    protected TLController tlc;
    /**
     * The Thing that makes all Trafficlights shudder
     */
    protected static SignController sgnctrl;
    /**
     * Name of the simulation
     */
    protected String simName = "untitled";
    /**
     * A boolean to keep track if this sim has already run (ivm initialization)
     */
    protected boolean hasRun = false;
    /**
     * Indicates if roadusers cross nodes or jump over them.
     */
    public static boolean CrossNodes = true;
    /**
     * Indicates whether we are running a series of simulations
     */
    protected boolean runSeries = false;
    protected boolean locked = false;
    /**
     * The number of steps each of the simulations in a series should make
     */
    protected static int numSeriesSteps = 50000;
    protected static int LOCK_THRESHOLD = 10000;
    protected static int numSeries = 10; // DOAS 06: 10 series per test config
    protected int curSeries = 0;

    /**
     * The derivation factor for calculating shortest paths
     */
    protected double derivationFactor = 1.1;

    /**
     * Creates second thread
     */
    public SimModel() {
        thread = new SimModelThread();
        thread.start();
        curCycle = 0;
        generator = new Random(GLDSim.seriesSeed[GLDSim.seriesSeedIndex]);
        sgnctrl = new SignController(tlc, infra);
    }
    
    public static SignController getSignController(){
        return sgnctrl;
    }

    //(DOAS 06)
    public SimController getSimController() {
        return controller;
    }

    public void setSimController(SimController sc) {
        controller = sc;
    }

    @Override
    public void setInfrastructure(Infrastructure i) {
        pause();

        super.setInfrastructure(i);
        if (tlc != null) {
            tlc.setInfrastructure(i);
        }
        if (sgnctrl != null) {
            sgnctrl.setInfrastructure(i);
        }
    }

    /**
     * Returns the current cycle
     */
    public int getCurCycle() {
        return curCycle;
    }

    /**
     * Sets the current cycle
     */
    public void setCurCycle(int c) {
        curCycle = c;
    }

    /**
     * Returns the current cycle
     */
    public int getCurNumWaiting() {
        return numWaiting;
    }

    /**
     * Sets the current cycle
     */
    public void setCurNumWaiting(int c) {
        numWaiting = c;
    }

    /**
     * Returns the current Driving Policy
     */
    public static DrivingPolicy getDrivingPolicy() {
        return dp;
    }

    /**
     * Sets the current DrivTLController
     */
    public void setDrivingPolicy(DrivingPolicy _dp) {
        dp = _dp;
    }

    /**
     * Returns the current TLController
     */
    public TLController getTLController() {
        return tlc;
    }

    /**
     * Sets the current TLController
     */
    public void setTLController(TLController _tlc) {
        tlc = _tlc;
        sgnctrl.setTLC(tlc);
    }

    /**
     * Returns the random number generator
     */
    public Random getRandom() {
        return generator;
    }

    /**
     * Sets the random number generator
     */
    public void setRandom(Random r) {
        generator = r;
    }

    /**
     * Returns the name of the simulation
     */
    public String getSimName() {
        return simName;
    }

    /**
     * Sets the name of the simulation
     */
    public void setSimName(String s) {
        simName = s;
    }

    /**
     * Returns the pseudo-random-number generator of this Model
     */
    public Random getRNGen() {
        return generator;
    }

    /**
     * Sets spawn frequency for given node and ru type.
     */
    public void setSpawnFrequency(EdgeNode en, int rutype, float newspawn) {
        en.setSpawnFrequency(rutype, newspawn);
        setChanged();
        notifyObservers();
    }

    /**
     * Stops the simulation. This should only be called when the program exits.
     * To start a new simulation, the simulation should be paused with a call to
     * pause(), then followed by a call to reset(), and finally resumed with
     * unpause().
     */
    public void stop() {
        thread.die();
    }

    /**
     * Pauses the simulation
     */
    public void pause() {
        thread.pause();
    }

    /**
     * Unpauses the simulation
     */
    public void unpause() {
        thread.unpause();
    }

    public boolean isRunning() {
        return thread.isRunning();
    }

    public void runSeries() {
        curSeries = 0;
        runSeries = true;
        nextSeries();
    }

    public void nextSeries() {
        controller.nextSeries();
    }

    public void lockedSeries() {
        pause();
        for (; curCycle < numSeriesSteps; curCycle++) {
            setChanged();
            notifyObservers();
        }
        locked = false;
        nextSeries();
    }

    public void stopSeries() {
        curSeries = 0;
        GLDSim.seriesSeedIndex = 0;
        runSeries = false;
    }

    public void nextCurSeries() {
        curSeries++;
        GLDSim.seriesSeedIndex = curSeries;

    }

    public int getCurSeries() {
        return curSeries;
    }

    public boolean isRunSeries() {
        return runSeries;
    }

    public int getNumSeries() {
        return numSeries;
    }

    //(DOAS 06)
    public void setNumSeries(int value) {
        numSeries = value;
    }

    //(DOAS 06)
    public void setSeriesSteps(int value) {
        numSeriesSteps = value;
    }

    /**
     * Resets data
     */
    public void reset() throws SimulationRunningException {
        if (thread.isRunning()) {
            throw new SimulationRunningException(
                    "Cannot reset data while simulation is running.");
        }
        infra.reset(derivationFactor);
        tlc.reset();
        dp.reset(); //driving policy might also need being reset (DOAS 06)
        curCycle = 0;
        generator = new Random(GLDSim.seriesSeed[GLDSim.seriesSeedIndex]);
        TrackerFactory.resetTrackers();

        setChanged();
        notifyObservers();
    }

    /**
     * Does 1 step in the simulation. All cars move, pedestrians get squashed
     * etc...
     */
    public void doStep() {
        curCycle++;
        infra.setCurCycle(curCycle);    //(DOAS 06) so that the Infrastructure.curCycle is not zero all the time

        boolean structureChanged = false;
        // (DOAS 06) Setting added to the menu
        if (this.controller.getAccidents()) {
            structureChanged = infra.disableRandomLane(derivationFactor); // (DOAS 05) these two functions enable the use of 'accidents'
            structureChanged |= infra.enableRandomLane(derivationFactor);  // removing those two lines will revert the program to the normal version
        }

//TODO make sure that this can actually be turned off (enable/disable already calcs all shortest paths twice)
//        if (structureChanged && this.controller.getRerouting()) {
//            // the paths have to be recalculated, because the structure changed (DOAS 06)
//            ShortestPathCalculator calc = new ShortestPathCalculator();
//            try {
//                calc.calcAllShortestPaths(infra);
//            } catch (InfraException e) {
//                e.printStackTrace();
//            }
//        }

        if (!hasRun) {
            initialize();
            hasRun = true;
        }
        try {
            cityDoStep();
        } catch (StackOverflowError s) {
            System.out.println("java.lang.StackOverflowError: " + s.getMessage());
            nextSeries();
        }
        setChanged();
        notifyObservers();
        if (runSeries && curCycle >= numSeriesSteps) {
            nextSeries();
        }
        if (locked && runSeries) {
            lockedSeries();
        }
    }

    public void initialize() {
        SAVE_STATS = true;
        Iterator it = Arrays.asList(infra.getSpecialNodes()).iterator();
        while (it.hasNext()) {
            ((SpecialNode) (it.next())).start();
        }
    }

    /**
     * Gets the speed of the simulation
     *
     * @return the speed
     */
    public int getSpeed() {
        return thread.getSleepTime();
    }

    /**
     * Sets the speed of the simulation
     *
     * @param s the speed
     */
    public void setSpeed(int s) {
        thread.setSleepTime(s);
    }

    /**
     * Gets the derivation factor of shortest path calculations
     *
     * @return the derivation factor
     */
    public double getDerivationFactor() {
        return derivationFactor;
    }

    /**
     * Sets the derivation factor of shortest path calculations
     *
     * @param derivationFactor the derivation factor
     */
    public void setDerivationFactor(double derivationFactor) {
        this.derivationFactor = derivationFactor;
    }

    protected void cityDoStep() {
        try {
            specialNodesDoStep();
            moveAllRoadusers();
            spawnNewRoadusers();
            sgnctrl.switchSigns();
        } catch (Exception e) {
            System.out.println("The simulator made a booboo:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void specialNodesDoStep() throws Exception {
        Iterator specialNodes = Arrays.asList(infra.getSpecialNodes()).iterator();
        while (specialNodes.hasNext()) {
            ((SpecialNode) (specialNodes.next())).doStep(this);
        }
    }

    /**
     *
     * moving all Roadusers to their new places
     *
     * @author Blah... why put an author tag at every 10-line piece of code? ->
     * Just because we can! MUHAHAahahahahah!
     */
    public void moveAllRoadusers() throws InfraException {
        // Line below is faster than the obvious alternative
        Iterator lanes = infra.getAllInboundLanes().iterator();
        DriveLane lane;

        while (lanes.hasNext()) {
            lane = (DriveLane) (lanes.next());
            // First you should check wheter they are already moved ......
            if (lane.getCycleMoved() != curCycle) {
                moveLane(lane);
            }
        }
    }

    /**
     * moving all roadusers from one lane to their new places
     *
     * @author Jilles V, Arne K, Chaim Z and Siets el S
     * @param lane The lane whose roadusers should be moved
     * @param callingLanes ArrayList of drivelanes, for recursive internal use
     * only, this parameter should have the value null, when called from the
     * outside
     * @version 1.0
     */
    protected void moveLane(DriveLane lane) throws InfraException {
        LinkedList queue;
        ListIterator li;
        DriveLane sourceLane, destLane;
        Node node;
        Sign sign;
        Roaduser ru;
        int ru_pos, ru_des, ru_speed, ru_type, ru_len;

        sign = lane.getSign();
        queue = lane.getQueue();
        li = queue.listIterator();

        //(DOAS 06) This must be in the begining!!!!
        lane.setCycleAsked(curCycle);

        while (li.hasNext()) {
            try {
                ru = (Roaduser) li.next();
            } // When this exception is thrown you removed the first element of the queue, therefore re-create the iterator.
            catch (Exception e) {
                li = queue.listIterator();
                continue;
            }

            // Only attempt to move this RU when it hasnt already
            if (!ru.didMove(curCycle)) {
                // DOAS 06
                // No need to reset color
                // ru.setColor(new Color(0, 0, 255));
                boolean edgeNodeReached = false;

                ru.setCycleAsked(curCycle);
                node = sign.getNode();
                ru_pos = ru.getPosition();
                ru_speed = ru.getSpeed();
                ru_len = ru.getLength();
                ru_type = ru.getType();
                ru_des = ru.getDestNode().getId();

                PosMov[] posMovs = calcPosMovs(node, sign, lane, ru, li);

                ru.setInQueueForSign(false);

                if (lane.getFreeUnitsInFront(ru) < ru_speed) // DOAS 06: Speed of car = 2.
                {
                    ru.setInQueueForSign(true);
                }

                // Handle Roadusers that possibly can cross a Node: a roaduser with speed
                // equal or greater than the number of positions between him and the
                // junction.
                if (ru_pos - ru_speed < 0) {
                    // Handle Roadusers that get to Special Nodes
                    if (node instanceof SpecialNode) {
                        if (ru_pos == 0 || lane.getPosFree(li, 0, ru_len, ru_speed, ru) == 0) {
                            node.processStats(ru, curCycle, sign);
                            ru.setPosition(-1);
                            ru.setPrevSign(-1);
                            li.remove();
                            ru.setWaitPos(sign.getId(), sign.mayDrive(), ru_pos);
                            ru.setInQueueForSign(false);
                            tlc.updateRoaduserMove(ru, lane, sign, ru_pos, null, null, 0, posMovs, null);
                            ((SpecialNode) (node)).enter(ru);
                            ru = null;
                            edgeNodeReached = true;
                        }
                    } // Handle Roadusers that are (or nearly) at a Sign
                    else if (lane.getSign().mayDrive()) {
                        if (ru_pos == 0 || lane.getPosFree(li, 0, ru_len, ru_speed, ru) == 0) { // Can cross-check
                            destLane = dp.getDirection(ru, lane, node); // HACK

                            if (destLane != null) {
                                // Check if there is room on the node
                                if (destLane.isLastPosFree(ru_len)) {
                                    try {
                                        node.processStats(ru, curCycle, sign); // Let the RU touch the Sign to refresh/unload some statistical data
                                        destLane.addRoaduserAtEnd(ru);
                                        ru.setPrevSign(lane.getSign().getId());
                                        li.remove(); // Remove the RU from the present lane, and place it on the destination lane
                                        ru.setWaitPos(sign.getId(),
                                                sign.mayDrive(),
                                                ru_pos);
                                        ru.setInQueueForSign(false);
                                        ru.setCycleMoved(curCycle);
                                        tlc.updateRoaduserMove(ru, lane, sign,
                                                ru_pos, destLane,
                                                destLane.getSign(),
                                                ru.getPosition(), posMovs,
                                                destLane);
                                    } catch (Exception e) {
                                        System.out.println(
                                                "Something screwd up in SimModel.moveLane where a Roaduser is about to cross:");
                                        e.printStackTrace();
                                    }
                                } else { // Otherwise, check if the next lane should move, and then do just that
                                    if (curCycle != destLane.getCycleAsked()
                                            && curCycle != destLane.getCycleMoved()) { // If the position is not free, then check if it already moved this turn, if not:
                                        moveLane(destLane); // System.out.println("Waiting for another lane to move..");
                                    }
                                    if (destLane.isLastPosFree(ru_len)) { // Ok now the lane that should have moved, moved so try again .........
                                        try {
                                            node.processStats(ru, curCycle,
                                                    sign);
                                            destLane.addRoaduserAtEnd(ru);
                                            ru.setPrevSign(lane.getSign().getId());
                                            li.remove();
                                            ru.setWaitPos(sign.getId(),
                                                    sign.mayDrive(), ru_pos);
                                            ru.setInQueueForSign(false);
                                            ru.setCycleMoved(curCycle);
                                            tlc.updateRoaduserMove(ru, lane,
                                                    sign,
                                                    ru_pos, destLane,
                                                    destLane.getSign(),
                                                    ru.getPosition(), posMovs,
                                                    destLane);
                                        } catch (Exception e) {
                                            Logger.getLogger(SimModel.class.getName()).log(Level.SEVERE, null, e);
                                        }
                                    } else { // Apparently no space was created, so we're still here.
                                        if (moveRoaduserOnLane(li, ru, ru_speed,
                                                lane) > 0) {
                                            ru.setWaitPos(sign.getId(),
                                                    sign.mayDrive(), ru_pos);
                                        }
                                        tlc.updateRoaduserMove(ru, lane, sign,
                                                ru_pos, lane, sign,
                                                ru.getPosition(),
                                                posMovs, null);
                                    }
                                }
                            }
                        }
                    } else {
                        /* Light==red, Try to move user as far as it can go on this lane. Update it's move. */
                        if (moveRoaduserOnLane(li, ru, ru_speed, lane) > 0) {
                            ru.setWaitPos(sign.getId(), sign.mayDrive(), ru_pos);
                        }
                        tlc.updateRoaduserMove(ru, lane, sign, ru_pos, lane,
                                sign, ru.getPosition(), posMovs, null);
                    }
                } /* Roaduser impossibly can cross a sign. The maximum amount of space
				 *  per speed is travelled */ else {
                    if (moveRoaduserOnLane(li, ru, ru_speed, lane) > 0) {
                        ru.setWaitPos(sign.getId(), sign.mayDrive(), ru_pos);
                    }
                    ru.setCycleMoved(curCycle);
                    tlc.updateRoaduserMove(ru, lane, sign, ru_pos, lane, sign,
                            ru.getPosition(), posMovs, null);
                }

                // DOAS 06: this code block checks whether the current road user has
                // been stuck for more than x cycles, if so this car and all other waiting
                // behind it will be removed. A penalty will be given to the learner, since
                // stuck cars aren't nice.
                //DEBUG DOAS 06
                /*if (   !edgeNodeReached &&
				    curCycle - ru.getCycleMoved() > controller.getMaxWaitingTime())
				{
                                    System.out.println("I'd remove a car now " + lane.getNodeLeadsTo().getId());
                                    controller.pause();
                                }*/
                if (this.controller.getRemoveStuckCars()
                        && !edgeNodeReached
                        && curCycle - ru.getCycleMoved() > controller.getMaxWaitingTime()) {
                    //System.out.println("Car seems to be stuck, removing car.");
                    //ru.addDelay(controller.getPenalty());
                    //node.processStats(ru, curCycle, sign);
                    ru.setPosition(-1);
                    ru.setPrevSign(-1);
                    li.remove();
                    ru.setWaitPos(sign.getId(), sign.mayDrive(), ru_pos);
                    ru.setInQueueForSign(false);

                    //update learners (punishment for removed cars) (DOAS 06)
                    if (tlc instanceof TCRL) {
                        //System.out.println("We have penalty: " + controller.getPenalty());
                        ((TCRL) tlc).updateRoaduserMove(ru, lane, sign, ru_pos, lane, sign, 0, posMovs, null, controller.getPenalty());
                    } else {
                        tlc.updateRoaduserMove(ru, lane, sign, ru_pos, lane, sign, 0, posMovs, null);
                    }

                    ru = null;
                    // increment counter of cars removed.
                    infra.removedCarsIncrement();
                }

//				if (ru != null)
//				{
//					ru.setCycleMoved(curCycle);
//				}
            } else {
                // DOAS 06: No need to repaint the vehicle.
                // ru.setColor(new Color(0, 0, 0));
            }
        }
        lane.setCycleMoved(curCycle);
    }

    /* Moves a roaduser on it's present lane as far as it can go. */
    protected int moveRoaduserOnLane(ListIterator li, Roaduser ru,
            int speed_left, DriveLane lane) {
        int ru_pos = ru.getPosition();
        int ru_len = ru.getLength();
        int best_pos = ru_pos;
        int max_pos = ru_pos;
        int target_pos = (ru_pos - speed_left > 0) ? ru_pos - speed_left : 0;
        int waitsteps;
        //System.out.println("Targetpos:"+target_pos+" and hasPrev:"+li.hasPrevious());

        // Previous should be 'ru'
        Roaduser prv = (Roaduser) li.previous();

        if (prv == ru && li.hasPrevious()) {
            /* has car in front */
            prv = (Roaduser) li.previous();
            /*  named prv */
            int prv_pos = prv.getPosition();
            max_pos = prv_pos + prv.getLength();
            if (max_pos < target_pos) {
                best_pos = target_pos;
            } else {
                best_pos = max_pos;
            }
            li.next();
            //System.out.println("RU had previous, now bestpos ="+best_pos);
        } else {
            best_pos = target_pos;
        }

        li.next();
        if (best_pos != ru_pos) {
            /* has no car in front, advance to your best pos */
            // The Roaduser can advance some positions
            ru.setPosition(best_pos);
            return (speed_left - (ru_pos - best_pos));
        } else {
            /* best_pos == ru_pos, or, you cant move. */
            return speed_left;
        }
    }

    protected PosMov[] calcPosMovs(Node node, Sign sign, DriveLane lane,
            Roaduser ru, ListIterator li) {
        // =======================================
        // Calculating the ranges per drivelane to where roaduser could get to
        // =======================================
        int ru_pos = ru.getPosition();
        int ru_speed = ru.getSpeed();
        int ru_len = ru.getLength();
        int ru_type = ru.getType();
        int ru_des = ru.getDestNode().getId();

        ArrayList<PosMov> vPosMovs = new ArrayList<>();
        int tlId = sign.getId();

        // Get the position closest to the Sign the RU can reach
        int bestPos = lane.getPosFree(li, ru_pos, ru_len, ru_speed, ru);
        for (int z = ru_pos; z >= bestPos; z--) {
            vPosMovs.add(new PosMov(tlId, z));
        }

        int speedLeft = ru_speed - ru_pos; // ru_pos as that is the number of units to be moven to the Sign

        // Now figure out the other possible lanes
        if (bestPos == 0 && speedLeft > 0) {
            DriveLane[] possiblelanes = node.getShortestPaths(ru_des, ru_type);
            int lanes = possiblelanes.length;
            for (int j = 0; j < lanes; j++) {
                // For each possible lane
                DriveLane testLane = possiblelanes[j];

                if (testLane.isLastPosFree(ru_len)) {
                    bestPos = -1;
                    speedLeft = speedLeft > ru_len ? speedLeft - ru_len : 0;
                    int worstPos = testLane.getCompleteLength() - ru_len;
                    int tltlId = testLane.getId();

                    // We kunnen ervanuitgaan dat we nooit 'echt' op de drivelane springen
                    // We kunnen wel 'naar' deze drivelane springen
                    // dwz, de posities op de node tot max(lane.length-ru_len,lane.clength-speedleft) zijn vrij om te gaan
                    bestPos = Math.max(testLane.getCompleteLength() - ru_len
                            - speedLeft, testLane.getLength() - ru_len);
                    for (int k = worstPos; k >= bestPos; k--) {
                        vPosMovs.add(new PosMov(tltlId, k));
                    }
                }
            }
        }
        // Fuck it, we aint got the power to cross, so don't even bother calculating further..
        return vPosMovs.toArray(new PosMov[vPosMovs.size()]);
    }

    /**
     * New road users are placed on the roads when necessary. When roads are
     * full, new road users are queued.
     *
     * @throws com.github.cc007.trafficlights.infra.InfraException
     * @throws java.lang.ClassNotFoundException
     */
    public void spawnNewRoadusers() throws InfraException, ClassNotFoundException {
        SpecialNode[] specialNodes = infra.getSpecialNodes();
        LinkedList<Roaduser> wqueue;
        ListIterator<Roaduser> list;
        EdgeNode edge;
        Roaduser r;
        int num_edges = specialNodes.length;
        int total_queue = 0;

        for (int i = 0; i < num_edges; i++) {
            if (!(specialNodes[i] instanceof EdgeNode)) {
                break;
            } else {
                edge = (EdgeNode) (specialNodes[i]);
            }
            boolean placed = false;
            wqueue = edge.getWaitingQueue();
            int wqsize = wqueue.size();
            list = wqueue.listIterator();
            while (list.hasNext()) {
                total_queue++;
                r = list.next();
                if (placeRoaduser(r, edge)) {
                    list.remove();
                }
            }

            SpawnFrequency[] freqs = edge.getSpawnFrequencies();
            DestFrequency[][] destfreqs = edge.getDestFrequencies();
            int num_freqs = freqs.length;
            int cur_index;
            int[] freqIndexes = new int[num_freqs];

            for (int nrs = 0; nrs < num_freqs; nrs++) {
                freqIndexes[nrs] = nrs; //Shuffle the indexes
            }

            Arrayutils.randomizeIntArray(freqIndexes, generator);

            for (int j = 0; j < num_freqs; j++) {
                //First try to place new road users on the road.
                cur_index = freqIndexes[j];
                if (freqs[cur_index].freq >= generator.nextFloat()) {
                    int ruType = freqs[cur_index].ruType;
                    /* Spawn road user of type freqs[i].ruType to a random destination.
					 * When all drivelanes are full the road users are queued.			*/
                    SpecialNode dest = getRandomDestination(specialNodes, edge,
                            ruType, destfreqs);
                    r = RoaduserFactory.genRoaduser(ruType, edge, dest, 0);
                    r.setDrivelaneStartTime(curCycle);

                    // Add R in queue if there is no place
                    if (!placeRoaduser(r, edge)) {
                        /*if(wqsize < 375) {
						 list.add(r);
						 wqsize++;
						 } else {
						 break;
						 }*/
                        if (total_queue < SimController.getMaxRuWaitingQueue()) {
                            list.add(r);
                            wqsize++;
                        } else {
                            break;
                        }

                    }
                }
            }
        }

        setCurNumWaiting(total_queue);
        if (total_queue >= SimController.getMaxRuWaitingQueue()) {
            locked = true;
        }
    }

    /**
     * A road user is placed on the given edge node. When road is full the ru is
     * queued
     */
    private boolean placeRoaduser(Roaduser r, SpecialNode edge) {
        DriveLane found = findDrivelaneForRU(r, edge);
        if (found == null) {
            return false;
        } else {
            // There is room for me!
            try {
                //System.out.println("Adding RU with type:"+r.getType()+" to lane:"+found.getSign().getId()+" going to Node:"+found.getNodeLeadsTo().getId()+" at pos:"+found.getNodeLeadsTo().isConnectedAt(found.getRoad())+" with type:"+found.getType());
                found.addRoaduserAtEnd(r, found.getLength() - r.getLength());
                r.addDelay(curCycle - r.getDrivelaneStartTime());
                r.setDrivelaneStartTime(curCycle);
                infra.enteredCarsIncrement();//DOAS 06 Statistics blahhh
                return true;
            } catch (Exception e) {
                Logger.getLogger(SimModel.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        }
    }

    private DriveLane findDrivelaneForRU(Roaduser r, SpecialNode e) {
        SpecialNode dest = (SpecialNode) r.getDestNode();
        DriveLane[] lanes = (DriveLane[]) e.getShortestPaths(dest.getId(),
                r.getType()).clone();
        Arrayutils.randomizeArray(lanes);
        int num_lanes = lanes.length;
        for (int i = 0; i < num_lanes; i++) {
            if (lanes[i].isLastPosFree(r.getLength())) {
                return lanes[i];
            }
        }
        //System.out.println("Couldnt place RU");
        return null;
    }

    /**
     * Get a completely random destination, don't choose moi
     */
    public SpecialNode getRandomDestination(SpecialNode moi) throws InfraException {
        SpecialNode[] dests = infra.getSpecialNodes();
        if (dests.length < 2) {
            throw new InfraException(
                    "Cannot choose random destination. Not enough special nodes.");
        }
        SpecialNode result;
        while (moi
                == (result = dests[(int) (generator.nextFloat() * dests.length)])) {
            ;
        }
        return result;
    }

    /*Choose a destination*/
    private SpecialNode getRandomDestination(SpecialNode[] dests,
            SpecialNode here, int ruType,
            DestFrequency[][] destfreqs) {
        int[] destIds = here.getShortestPathDestinations(ruType);
        float choice = generator.nextFloat();
        float total = 0f;

        /*All frequencies are between 0 and 1, but their total can be greater than 1*/
        for (int i = 0; i < destIds.length; i++) {
            for (int j = 0; j < destfreqs[i].length; j++) {
                if (destfreqs[destIds[i]][j].ruType == ruType) {
                    total += destfreqs[destIds[i]][j].freq;
                }
            }
        }

        float sumSoFar = 0f;
        int j = 0;
        int index = 0;
        boolean foundIndex = false;
        while (j < destIds.length && !foundIndex) {
            for (int i = 0; i < destfreqs[j].length; i++) {
                if (destfreqs[destIds[j]][i].ruType == ruType) {
                    float now = (destfreqs[destIds[j]][i].freq) / total;
                    if (now + sumSoFar >= choice) {
                        foundIndex = true;
                        index = j;
                    } else {
                        sumSoFar += now;
                    }
                }
            }
            j++;
        }
        return dests[destIds[index]];
    }

    /*Get a random index out of the lanes*/
    private int getRandomLaneNr(DriveLane[] lanes) {
        int ind = (int) Math.floor(generator.nextFloat() * (lanes.length));
        while (ind != lanes.length) {
            ind = (int) Math.floor(generator.nextFloat() * (lanes.length));
        }
        return ind;
    }

    /**
     *
     * The second thread that runs the simulation.
     *
     * @author Joep Moritz
     * @version 1.0
     */
    public class SimModelThread extends Thread {

        /**
         * Is the thread suspended?
         */
        private volatile boolean suspended;
        /**
         * Is the thread alive? If this is set to false, the thread will die
         * gracefully
         */
        private volatile boolean alive;
        /**
         * The time in milliseconds this thread sleeps after a call to doStep()
         */
        private int sleepTime = 100;

        /**
         * Returns the current sleep time
         */
        public int getSleepTime() {
            return sleepTime;
        }

        /**
         * Sets the sleep time
         */
        public void setSleepTime(int s) {
            sleepTime = s;
        }

        /**
         * Starts the thread.
         */
        public SimModelThread() {
            alive = true;
            suspended = true;
        }

        /**
         * Suspends the thread.
         */
        public synchronized void pause() {
            suspended = true;
        }

        /**
         * Resumes the thread.
         */
        public synchronized void unpause() {
            suspended = false;
            notify();
        }

        /**
         * Stops the thread. Invoked when the program exitst. This method cannot
         * be named stop().
         */
        public synchronized void die() {
            alive = false;
            interrupt();
        }

        /**
         * Returns true if the thread is not suspended and not dead
         */
        public boolean isRunning() {
            return !suspended && alive;
        }

        /**
         * Invokes Model.doStep() and sleeps for sleepTime milliseconds
         */
        @Override
        public void run() {
            while (alive) {
                try {
                    sleep(sleepTime);
                    synchronized (this) {
                        while (suspended && alive) {
                            wait();
                        }
                    }
                    doStep();
//					} catch (InterruptedException e) { }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Exception: " + e.getMessage());
                    locked = true;
                }
            }
        }
    }

    // Some XMLSerializable stuff
    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws
            XMLTreeException, IOException, XMLInvalidInputException {
        super.load(myElement, loader);
        setInfrastructure(infra);
        Map<String, Map<Integer, TwoStageLoader>> loadMap;
        try {
            loadMap = infra.getMainMap();
        } catch (InfraException ohNo) {
            throw new XMLInvalidInputException(
                    "This is weird. The infra can't make a map for the second "
                    + "stage loader of the algorithms. Like : " + ohNo);
        }
        /* TODO fix and explain why you want to put an infra into the loadMap, even though it's not a two stage loader and the index isn't an integer
        Map infraMap = new HashMap();
        infraMap.put("infra", infra);
        loadMap.put("infra", infraMap);
         */
        boolean savedBySim = ("simulator").equals(myElement.getAttribute(
                "saved-by").getValue());
        if (savedBySim) {
            thread.setSleepTime(myElement.getAttribute("speed").getIntValue());
            simName = myElement.getAttribute("sim-name").getValue();
            curCycle = myElement.getAttribute("current-cycle").getIntValue();
            TLCFactory factory = new TLCFactory(infra);
            tlc = null;

            try {
                tlc = factory.getInstanceForLoad(TLCFactory.getNumberByXMLTagName(loader.getNextElementName()));
                loader.load(this, tlc);
                System.out.println("Loaded TLC " + tlc.getXMLName());
            } catch (InfraException e2) {
                throw new XMLInvalidInputException(
                        "Problem while TLC algorithm was processing infrastructure :"
                        + e2);
            }
            tlc.loadSecondStage(loadMap);
            DPFactory dpFactory = new DPFactory(this, tlc);
            try {
                dp = dpFactory.getInstance(DPFactory.getNumberByXMLTagName(loader.getNextElementName()));
                loader.load(this, dp);
                System.out.println("Loaded DP " + dp.getXMLName());
            } catch (ClassNotFoundException e) {
                throw new XMLInvalidInputException("Problem with creating DP in SimModel."
                        + "Could not generate instance of DP type :" + e);
            }
            dp.loadSecondStage(loadMap);
            loader.load(this, sgnctrl);
            sgnctrl.setTLC(tlc);
        } else {
            curCycle = 0;
        }
        while (loader.getNextElementName().equals("dispenser")) {
            loader.load(this, new NumberDispenser());
        }
    }

    @Override
    public XMLElement saveSelf() {
        XMLElement result = super.saveSelf();
        result.addAttribute(new XMLAttribute("sim-name", simName));
        result.addAttribute(new XMLAttribute("saved-by", "simulator"));
        result.addAttribute(new XMLAttribute("speed", thread.getSleepTime()));
        result.addAttribute(new XMLAttribute("current-cycle", curCycle));
        return result;
    }

    @Override
    public void saveChilds(XMLSaver saver) throws IOException, XMLTreeException,
            XMLCannotSaveException {
        super.saveChilds(saver);
        System.out.println("Saving TLC " + tlc.getXMLName());
        saver.saveObject(tlc);
        System.out.println("Saving DP " + dp.getXMLName());
        saver.saveObject(dp);
        saver.saveObject(sgnctrl);
    }
}
