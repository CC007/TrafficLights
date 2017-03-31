
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
package com.github.cc007.trafficlights.infra;

import com.github.cc007.trafficlights.sim.SimModel;
import com.github.cc007.trafficlights.utils.*;
import com.github.cc007.trafficlights.xml.*;

import java.awt.Point;
import java.io.IOException;
import java.util.*;

/**
 * Class with common code for NetTunnels and EdgeNodes
 */
public abstract class SpecialNode extends Node implements XMLSerializable, TwoStageLoader {

    /**
     * The road this SpecialNode is connected to
     */
    protected Road road;
    /**
     * True if the connected road is an alpha road
     */
    protected boolean isAlpha;
    /**
     * The connection-position of the connected road
     */
    protected int roadPos;
    /**
     * The queue with all road users which have not entered the road. For
     * example because it's already full
     */
    protected LinkedList<Roaduser> waitingQueue = new LinkedList<>();

    /**
     * Temporary data structure to tranfer info from the first stage loader to
     * the second stage loader
     */
    protected TwoStageLoaderData loadData = new TwoStageLoaderData();

    public SpecialNode() {
    }

    public SpecialNode(Point _coord) {
        super(_coord);
    }

    /*============================================*/
 /* LOAD and SAVE                              */
 /*============================================*/
    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
        super.load(myElement, loader);
        isAlpha = myElement.getAttribute("road-is-alpha").getBoolValue();
        roadPos = myElement.getAttribute("position").getIntValue();
        if (isAlpha) {
            road = new Road();
            loader.load(this, road);
        }
        loadData.roadId = myElement.getAttribute("road-id").getIntValue();
        waitingQueue = (LinkedList<Roaduser>) (XMLArray.loadArray(this, loader));
    }

    @Override
    public XMLElement saveSelf() throws XMLCannotSaveException {
        XMLElement result = super.saveSelf();
        result.setName("node-special");
        result.addAttribute(new XMLAttribute("road-is-alpha", isAlpha));
        result.addAttribute(new XMLAttribute("road-id", road.getId()));
        result.addAttribute(new XMLAttribute("position", roadPos));
        return result;
    }

    @Override
    public void saveChilds(XMLSaver saver) throws XMLTreeException, IOException, XMLCannotSaveException {
        super.saveChilds(saver);
        if (isAlpha) {
            saver.saveObject(road);
        }
        XMLUtils.setParentName(waitingQueue.iterator(), getXMLName());
        XMLArray.saveArray(waitingQueue, this, saver, "queue");
    }

    @Override
    public String getXMLName() {
        return parentName + ".node-special";
    }

    @Override
    public void loadSecondStage(Map<String, Map<Integer, TwoStageLoader>> maps) throws XMLInvalidInputException, XMLTreeException {
        super.loadSecondStage(maps);
        if (!isAlpha) {
            Map<Integer, TwoStageLoader> roadMap = maps.get("road");
            TwoStageLoader tsl = roadMap.get(loadData.roadId);
            if (!(tsl instanceof Road)) {
                throw new XMLInvalidInputException("The two stage loader isn't a road");
            }
            road = (Road) tsl;
        }
        road.loadSecondStage(maps);
        try {
            updateLanes();
        } catch (InfraException e) {
            throw new XMLInvalidInputException("Cannot initialize lanes of node " + nodeId);
        }
        XMLUtils.loadSecondStage(waitingQueue, maps);
    }

    class TwoStageLoaderData {

        int roadId;
    }

    /*============================================*/
 /* Basic GET and SET methods                  */
 /*============================================*/
    /**
     * Returns the road this SpecialNode is connected to
     */
    public Road getRoad() {
        return road;
    }

    /**
     * Sets the road this SpecialNode is connected to
     */
    public void setRoad(Road r) throws InfraException {
        road = r;
        updateLanes();
    }

    /**
     * Returns the position of the road
     */
    public int getRoadPos() {
        return roadPos;
    }

    /**
     * Sets the position of the road
     */
    public void setRoadPos(int pos) throws InfraException {
        roadPos = pos;
        updateLanes();
    }

    /**
     * Returns true if the road is an alpha road
     */
    public boolean getAlpha() {
        return isAlpha;
    }

    /**
     * Sets the isAlpha flag
     */
    public void setAlpha(boolean f) {
        isAlpha = f;
    }

    /**
     * Returns all roads connected to this node
     */
    @Override
    public Road[] getAllRoads() {
        Road[] r = {road};
        return r;
    }

    /**
     * Returns the alpha roads connected to this node
     */
    @Override
    public Road[] getAlphaRoads() {
        if (isAlpha) {
            return getAllRoads();
        }
        return new Road[0];
    }

    @Override
    public int getWidth() {
        if (road != null) {
            int w = road.getWidth();
            if (w < 4) {
                return 4;
            }
            return w;
        }
        return 4;
    }

    /*============================================*/
 /* RU over node methods	                      */
 /*============================================*/
    /**
     * Place a roaduser in one of the outbound queues
     */
    public void placeRoaduser(Roaduser ru) throws InfraException {
        DriveLane[] lanes = (DriveLane[]) getShortestPaths(ru.getDestNode().getId(), ru.getType()).clone();
        Arrayutils.randomizeArray(lanes);
        // The next person who outcomments this code will
        // be chopped into little pieces, burned, hanged, chainsawed, 
        // shredded, killed, /toaded and then ported to Microsoft Visual Lisp.
        // You were warned.
        //							Was signed,
        //												Siets El Snel
        if (lanes.length == 0) {
            throw new InfraException("Cannot find shortest path for new Roaduser in EdgeNode");
        }
        lanes[0].addRoaduserAtEnd(ru);
    }

    /**
     * Returns the queue with waiting road users for this node
     */
    public LinkedList<Roaduser> getWaitingQueue() {
        return waitingQueue;
    }

    /**
     * Sets a new queue with waiting road users
     * @param l
     */
    public void setWaitingQueue(LinkedList<Roaduser> l) {
        waitingQueue = l;
    }

    /**
     * Get the number of waiting road users, i.e. the length of the waitingQueue
     */
    public int getWaitingQueueLength() {
        return waitingQueue.size();
    }

    /**
     * Place a roaduser in the waitingQueue
     */
    public void enqueueRoaduser(Roaduser ru) {
        waitingQueue.addLast(ru);
    }

    /**
     * Remove a roaduser from the waitingQueue
     */
    public Roaduser dequeueRoaduser() {
        return (Roaduser) waitingQueue.removeFirst();
    }

    /*============================================*/
 /* STATISTICS                                 */
 /*============================================*/
    @Override
    protected int calcDelay(Roaduser ru, int stop, int distance) {
        // first, add the delay for the drivelane leading to this EdgeNode
        int start = ru.getDrivelaneStartTime();
        int speed = ru.getSpeed();
        ru.addDelay((stop - start) - (distance / speed));
        // then, return the total delay of the full trip
        return ru.getDelay();
    }

    /*============================================*/
 /* MODIFYING DATA                             */
 /*============================================*/
    @Override
    public void reset() {
        super.reset();
        if (isAlpha) {
            road.reset();
        }
        waitingQueue = new LinkedList<>();
    }

    @Override
    public void addRoad(Road r, int pos) throws InfraException {
        if (r == null) {
            throw new InfraException("Parameter r is null");
        }
        if (pos > 3 || pos < 0) {
            throw new InfraException("Position out of range: " + pos);
        }
        if (road != null) {
            throw new InfraException("Road already exists");
        }
        Node other = r.getOtherNode(this);
        if (other == null || !other.isAlphaRoad(r)) {
            isAlpha = true;
        } else {
            isAlpha = false;
        }

        roadPos = pos;
        road = r;
        updateLanes();
    }

    @Override
    public void setAlphaRoad(int pos) throws InfraException {
        if (pos > 3 || pos < 0) {
            throw new InfraException("Position out of range");
        }
        if (road == null || pos != roadPos) {
            throw new InfraException("Road at position " + pos + " does not exist");
        }
        isAlpha = true;
        updateLanes();
    }

    @Override
    public void remRoad(int pos) throws InfraException {
        if (pos > 3 || pos < 0) {
            throw new InfraException("Position out of range");
        }
        if (road == null || pos != roadPos) {
            throw new InfraException("Road at position " + pos + " does not exist");
        }
        road = null;
        isAlpha = false;
        updateLanes();
    }

    @Override
    public void remRoad(Road r) throws InfraException {
        if (r == null) {
            throw new InfraException("Parameter r is null");
        }
        if (road == null) {
            throw new InfraException("No road is connected to this node");
        }
        if (r != road) {
            throw new InfraException("Road not found on this node");
        }
        road = null;
        isAlpha = false;
        updateLanes();
    }

    @Override
    public void remAllRoads() throws InfraException {
        road = null;
        isAlpha = false;
        updateLanes();
    }

    @Override
    public void setSigns(Sign[] s) throws InfraException {
    }

    @Override
    public int getDesiredSignType() throws InfraException {
        return Sign.NO_SIGN;
    }

    /*============================================*/
 /* COMPLEX GET                                */
 /*============================================*/
    @Override
    public boolean isAlphaRoad(Road r) throws InfraException {
        if (r == null) {
            throw new InfraException("Parameter r is null");
        }
        return r == road && isAlpha;
    }

    @Override
    public boolean isConnected(Road r) throws InfraException {
        if (r == null) {
            throw new InfraException("Parameter r is null");
        }
        return r == road;
    }

    @Override
    public int isConnectedAt(Road r) throws InfraException {
        if (r == null) {
            throw new InfraException("Parameter r is null");
        }
        if (r != road) {
            throw new InfraException("Road is not connected to this node");
        }
        return roadPos;
    }

    @Override
    public boolean isConnectionPosFree(int pos) throws InfraException {
        if (pos > 3 || pos < 0) {
            throw new InfraException("Position out of range");
        }
        return (road == null);
    }

    @Override
    public int getNumRoads() {
        return road != null ? 1 : 0;
    }

    @Override
    public int getNumAlphaRoads() {
        return road != null && isAlpha ? 1 : 0;
    }

    @Override
    public int getNumInboundLanes() throws InfraException {
        return road.getNumInboundLanes(this);
    }

    @Override
    public int getNumOutboundLanes() throws InfraException {
        return road.getNumOutboundLanes(this);
    }

    @Override
    public int getNumAllLanes() {
        return road.getNumAllLanes();
    }

    @Override
    public int getNumSigns() {
        return 0;
    }

    @Override
    public int getNumRealSigns() {
        return 0;
    }

    @Override
    public DriveLane[] getLanesLeadingTo(DriveLane lane, int ruType) throws InfraException {
        return new DriveLane[0];
    }

    @Override
    public DriveLane[] getLanesLeadingFrom(DriveLane lane, int ruType) throws InfraException {
        return new DriveLane[0];
    }

    @Override
    public DriveLane[] getOutboundLanes() throws InfraException {
        return road != null ? road.getOutboundLanes(this) : new DriveLane[0];
    }

    @Override
    public DriveLane[] getInboundLanes() throws InfraException {
        return road != null ? road.getInboundLanes(this) : new DriveLane[0];
    }

    @Override
    public DriveLane[] getAllLanes() throws InfraException {
        return (DriveLane[]) Arrayutils.addArray(getInboundLanes(), getOutboundLanes());
    }

    /*============================================*/
 /* Hook methods                               */
 /*============================================*/
 /* Hook method for stuff that has to be done every step in the sim */
    public void doStep(SimModel model) {
    }

    /**
     * Hook method for stuff that has to be done when the sim is started
     */
    public void start() {
    }

    /**
     * Hook method for stuff that has to be done when the sim is stopped
     */
    public void stop() {
    }

    /**
     * Hook method that is called by the infra when a roaduser reaches this node
     */
    public void enter(Roaduser ru) {
        if (ru instanceof CustomRoaduser) {
            CustomFactory.removeCustom((CustomRoaduser) ru);
        }
    }

}
