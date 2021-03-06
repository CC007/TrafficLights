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

import java.awt.*;
import java.io.IOException;
import java.util.*;

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.algo.edit.ShortestPathCalculator;
import com.github.cc007.trafficlights.infra.Node.NodeStatistics;
import com.github.cc007.trafficlights.utils.*;
import com.github.cc007.trafficlights.xml.*;
import com.github.cc007.trafficlights.edit.Validation;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * The encapsulating class
 *
 * @author Group Datastructures
 * @version 1.0
 */
public class Infrastructure implements XMLSerializable, SelectionStarter {

    /**
     * All nodes in this infrastructure, including edge nodes
     */
    protected Node[] allNodes;
    /**
     * All exit/entry nodes in this infrastructure
     */
    protected SpecialNode[] specialNodes;
    /**
     * All nodes that are not EdgeNodes
     */
    protected Junction[] junctions;
    /**
     * Meta-data provided by the user
     */
    protected String title, author, comments;
    /**
     * The infrastructure version of this implementation. For debugging.
     */
    protected final int version = 1;
    /**
     * The size of this infrastructure, in pixels
     */
    protected Dimension size;
    /**
     * All the inbound lanes on all the Nodes in our Infrastructure
     */
    protected ArrayList<DriveLane> allLanes;
    /**
     * Number dispenser for sign id's
     */
    protected NumberDispenser signNumbers = new NumberDispenser();
    /**
     * The current cycle we're in, manely for Nodes to have access to this data
     */
    protected int curCycle;
    protected int curSeries;
    /**
     * DOAS 06: tracking the number of cars removed
     */
    protected int removedCars = 0;
    /**
     * DOAS 06: tracking the number of cars entered
     */
    protected int enteredCars = 0;

    /**
     * List of disabled Lanes
     */
    protected static ArrayList<DriveLane> disabledLanes = new ArrayList<>();
    protected static ArrayList<DriveLane> notYetDisabledLanes = new ArrayList<>();
    /**
     * Accidents rate (DOAS 06)
     */
    protected static int accidentsRate = 200;
    /**
     * Random class
     */
    protected Random rnd = new Random(GLDSim.seriesSeed[GLDSim.seriesSeedIndex]);
    public static final int blockLength = 10;
    public static final int blockWidth = 10;

    private Validation validator;

    protected String parentName = "model";

    // This one is temporary (for debugging TC-3)
    public static HashMap laneMap;

    /**
     * Creates a new infrastructure object.
     *
     * @param dim The dimension of the new infrastructure
     */
    public Infrastructure(Dimension dim) {
        size = dim;
        allNodes = new Node[0];
        specialNodes = new SpecialNode[0];
        junctions = new Junction[0];
        title = "untitled";
        author = "unknown";
        comments = "";
        curCycle = 0;
        validator = new Validation(this);
    }

    /**
     * Creates a new infrastructure object.
     *
     * @param nodes The Nodes this Infrastructure should contain.
     * @param edge The exit/entry nodes this Infrastructure should contain.
     * @param new_size The size of this Infrastructure in pixels x pixels
     */
    public Infrastructure(Node[] nodes, SpecialNode[] special,
            Dimension new_size) {
        allNodes = nodes;
        specialNodes = special;
        junctions = new Junction[allNodes.length - specialNodes.length];
        copyJunctions();
        size = new_size;
        title = "untitled";
        author = "unknown";
        comments = "";
        validator = new Validation(this);
    }

    /**
     * Constructor for loading
     */
    public Infrastructure() {
        allNodes = new Node[0];
        specialNodes = new SpecialNode[0];
        junctions = new Junction[0];
        allLanes = new ArrayList<>();
        title = "untitled";
        author = "";
        comments = "";
        size = new Dimension(5000, 3000);
        validator = new Validation(this);
    }

    // Function that disabls a random lane, and updates all targets to that lane so that
    // cars wont be able to go there anymore. It will store the default values in the particular
    // drivelanes so that when the lane becomes available again the original target will be restored
    // thus letting cars be able to go there again. (DOAS 05)
    /// @return True, if any lane was actually disabled (DOAS 06)
    public boolean disableRandomLane(double derivationFactor) {
        if (rnd.nextInt(accidentsRate) != 0) {
            return false;
        }

        // there must be a finite number of trials, so that this does not end in an infinite loop,
        // when there is no accident possible (DOAS 06)
        if (notYetDisabledLanes.size() == 0) {
            return false;
        }
        int maxTrialsCount = java.lang.Math.max(notYetDisabledLanes.size() / 4, 1);
        //while (true)
        for (int trial = 0; trial < maxTrialsCount; trial++) {
            DriveLane toBeDisabledLane = null;
            // probability of an accident goes higher with the number of cars on the lane (DOAS 06)
            // upper bound of the cycles count because the simulation must not be slown down by infinite cycles
            for (int tr = 0; tr < 16; tr++) {
                int randint = rnd.nextInt(notYetDisabledLanes.size());
                toBeDisabledLane = (DriveLane) notYetDisabledLanes.get(randint);
                if (rnd.nextInt(toBeDisabledLane.getLength()) < toBeDisabledLane.getNumBlocksTaken()) {
                    break;
                }
            }

            int disabledLaneIndex = disabledLanes.size();   //index of the first disabled lane in the disabledLanes vector
            try {
                // TODO: check if kruispunt is junction, get rid catch in the end of function
                // Junctions only So no roads going from an edgenode or a non junction node will be able to be
                //disabled
                Junction kruispunt = (Junction) toBeDisabledLane.
                        getNodeComesFrom();

                // Throws exception if the lane is leading to an edgenode, disabling the possibility that access
                // to an edge Node will be blocked
                Junction nextKruispunt = (Junction) toBeDisabledLane.
                        getNodeLeadsTo();

                // Throws an exception if one more accident would block the junction
                // (there would be no way out for incoming cars - dead-end) (DOAS 06)
                kruispunt.increaseAccidentsCount();

                Road ro = toBeDisabledLane.getRoad();
                //System.out.println(ro.getName() + " has been disabled [" + curCycle + "]");
                DriveLane[] otherlanes;

                // Determine if shared lanes (in the same direction) are on the alpha or beta Lane towards kruispunt
                if (ro.getAlphaNode() == toBeDisabledLane.getNodeComesFrom()) {
                    otherlanes = ro.getBetaLanes();
                } else {
                    otherlanes = ro.getAlphaLanes();
                }

                // Disable all lanes on the road shared by the target lane to be disabled
                for (int i = 0; i < otherlanes.length; i++) {
                    disabledLanes.add(otherlanes[i]);
                    notYetDisabledLanes.remove(otherlanes[i]);
                }

                Road[] incomingRoads = kruispunt.getAllRoads();

                // check which numbers or outgoing roads is the disabled one
                int ro_num = 0;
                for (int i = 0; i < 4; i++) {
                    if (incomingRoads[i] != null && incomingRoads[i] == ro) {
                        ro_num = i;
                        break;  //(DOAS 06)
                    }
                }

                // disable for all non outgoing roads the target to the disabled one
                for (int i = 0; i < 4; i++) {
                    if (incomingRoads[i] != null && i != ro_num) {
                        // this incoming road ri.
                        Road ri = incomingRoads[i];

                        // check relative direction between ri and ro
                        int dir = Node.getDirection(i, ro_num); // DIR: 1: left, 2: streight, 3: right

                        // be sure only to get the lanes leading to the crossing to have their targets disabled
                        DriveLane[] il;
                        if (ri.getAlphaNode() == kruispunt) {
                            //Road heeft kruispunt als Alpha node
                            il = ri.getAlphaLanes();
                        } else {
                            il = ri.getBetaLanes();
                        }

                        // change the correct targets.
                        for (int j = 0; j < il.length; j++) {
                            il[j].setTarget(dir - 1, false, false); // DIR: 0: left, 1: streight, 2: right

                            //check if there is still a way out of the road, or if we need to make another route
                            int ndir = dir - 1;
                            boolean allFalse = true;
                            for (int k = 0; k < 3; k++) {
                                if (k != ndir && il[j].getTarget(k) == true) {
                                    //Only if DL is already leading to another junction allFalse be set to false
                                    int d = (k + 1);
                                    //no. of target lane:
                                    int t = (i + d) % 4;
                                    Node nextnode;
                                    if (incomingRoads[t].getAlphaNode() == kruispunt) {
                                        nextnode = incomingRoads[t].getBetaNode();
                                    } else {
                                        nextnode = incomingRoads[t].getAlphaNode();
                                    }
                                    if (nextnode.getType() == Node.JUNCTION) {
                                        allFalse = false;
                                    }
                                }
                            }
                            //Cars can go nowhere now, we need to open another way
                            if (allFalse) {
                                for (int k = 0; k < 3; k++) {
                                    if (k != ndir) {

                                        //k ranges from 0-2, while dirs range from 1-3: 1: left, 2: streight, 3: right
                                        int d = (k + 1);

                                        //no of target road:
                                        int t = (i + d) % 4;

                                        //check if target road is not diabled we are still on lane nr i.
                                        //possible lanes:
                                        DriveLane[] pl;
                                        if (incomingRoads[t].getAlphaNode()
                                                == kruispunt) {
                                            pl = incomingRoads[t].getBetaLanes();
                                        } else {
                                            pl = incomingRoads[t].getAlphaLanes();
                                        }

                                        //check if drivelanes are not disabled yet (DOAS 05)
                                        if (!disabledLanes.contains(pl[0])) {
                                            il[j].setTarget(k, true, false); //create target to destinantion lane at drivelane j.
                                        }
                                    }
                                }
                            } // All False
                        } // Change targets

                    }
                }// Everything disabled towards disabled road

                // the results of the check must be used (DOAS 06)
                boolean infraOK = true;
                try {
                    //Check if everything is still okay about the Infrastructure
                    ArrayList errors = validator.validate();
                    if (errors.size() > 0) {
                        infraOK = false;
                    }
                } catch (InfraException e) {
                    Logger.getLogger(Infrastructure.class.getName()).log(Level.SEVERE, null, e);
                    infraOK = false;
                }

                if (!infraOK) {
                    //System.out.println(ro.getName() + " cannot be disabled. Enabled again.");
                    enableLane(disabledLaneIndex, derivationFactor);
                    continue;   //try another lane
                }

                return true;
            } catch (Exception e) {// If there is an exception The drivelane will not be disabled
                Logger.getLogger(Infrastructure.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        return false;
    }

    // oposite of the disabled function (DOAS 05)
    /// @return True, if some lane was enabled (DOAS 06)
    public boolean enableRandomLane(double derivationFactor) {
        if (disabledLanes.size() == 0
                || (rnd.nextInt(300) + 1 > 1 * disabledLanes.size())) {
            return false;
        }
        int numLanes = disabledLanes.size();

        // in case something goes weird, only the finite number of trials is to be performed (DOAS 06)
        for (int i = 0; i < 10; i++) {
            try {
                enableLane(rnd.nextInt(numLanes), derivationFactor);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    protected void enableLane(int disabledLaneIndex, double derivationFactor) throws InfraException {
        // Junctions only
        DriveLane toBeEnabledLane = disabledLanes.get(disabledLaneIndex);

        Junction junction;
        Node node = toBeEnabledLane.getNodeComesFrom();
        if (!(node instanceof Junction)) {
            throw new InfraException("This node isn't a junction!");
        } else {
            junction = (Junction) node;
        }

        junction.decreaseAccidentsCount();

        Road ro = toBeEnabledLane.getRoad();
        //System.out.println(ro.getName() + " has been enabled [" + curCycle + "]");
        DriveLane[] otherlanes;
        if (ro.getAlphaNode() == toBeEnabledLane.getNodeComesFrom()) {
            otherlanes = ro.getBetaLanes();
        } else {
            otherlanes = ro.getAlphaLanes();
        }

        for (int i = 0; i < otherlanes.length; i++) {
            disabledLanes.remove(otherlanes[i]);
            notYetDisabledLanes.add(otherlanes[i]);
        }
        Road[] incomingRoads = junction.getAllRoads();
        int ro_num = 0;
        for (int i = 0; i < 4; i++) {
            if (incomingRoads[i] != null && incomingRoads[i] == ro) {
                ro_num = i;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (incomingRoads[i] != null && i != ro_num) {
                Road ri = incomingRoads[i];
                int dir = Node.getDirection(i, ro_num);
                DriveLane[] il;

                if (ri.getAlphaNode() == junction) { //Road heeft kruispunt als Alpha node
                    il = ri.getAlphaLanes();
                } else { //Road heeft kruispunt als Beta node
                    il = ri.getBetaLanes();
                }
                for (int j = 0; j < il.length; j++) {
                    for (int k = 0; k < 3; k++) //Recover all original targets
                    {
                        il[j].setTarget(k, false, true); // DIR: 3: left, 2: streight, 1: right

                        //k ranges from 0-2, while dirs range from 1-3: 1: left, 2: streight, 3: right
                        int d = (k + 1);

                        //no of target lane:
                        int t = (i + d) % 4;

                        //check if that road exists - there may exist a junction with only three roads (DOAS 06)
                        if (incomingRoads[t] == null) {
                            continue;
                        }

                        //check if target lane is not diabled we are still on lane nr i.
                        //possible lanes:
                        DriveLane[] pl;
                        if (incomingRoads[t].getAlphaNode()
                                == junction) {
                            pl = incomingRoads[t].getBetaLanes();
                        } else {
                            pl = incomingRoads[t].getAlphaLanes();
                        }
                        // If there was another disabled lane and the original points were reset
                        // It will get disabled again.
                        if (il[j].getTarget(k) && disabledLanes.contains(pl[0])) {
                            il[j].setTarget(k, false, false);
                        }

                    }
                }
            }
        }
        //Check if everything is still okay about the Infrastructure
        validator.validate();

        ShortestPathCalculator calc = new ShortestPathCalculator();
        calc.calcAllShortestPaths(this, derivationFactor);

    }

    /**
     * Checks if the lane leads to the accident area. (DOAS 06) Accident area
     * means that at least one of the lanes reachable from the lane is disabled.
     */
    public boolean leadsToAccidentArea(Roaduser ru, DriveLane lane) {
        try {
            DriveLane[] lanes = lane.getNodeLeadsTo().getLanesLeadingFrom(lane, ru.getType());

            for (int i = 0; i < lanes.length; i++) {
                if (disabledLanes.contains(lanes[i])) {
                    return true;
                }
            }
        } catch (InfraException e) {
            e.printStackTrace();
        }

        return false;
    }


    /*============================================*/
 /* Basic GET and SET methods                  */
 /*============================================*/
    /**
     * Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     */
    public void setTitle(String s) {
        title = s;
    }

    /**
     * Returns the author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     */
    public void setAuthor(String s) {
        author = s;
    }

    /**
     * Returns the comments.
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the comments.
     */
    public void setComments(String s) {
        comments = s;
    }

    /**
     * Returns all exit/entry nodes
     */
    public SpecialNode[] getSpecialNodes() {
        return specialNodes;
    }

    /**
     * Sets all exit/entry nodes
     */
    public void setSpecialNodes(SpecialNode[] nodes) {
        specialNodes = nodes;
    }

    /**
     * Returns the Junctions of this infrastructure.
     */
    public Junction[] getJunctions() {
        return junctions;
    }

    /**
     * Sets all junctions.
     */
    public void setJunctions(Junction[] _junctions) {
        junctions = junctions;
    }

    /**
     * Returns all nodes (including edge nodes)
     */
    public Node[] getAllNodes() {
        return allNodes;
    }

    /**
     * Sets all nodes (including edge nodes)
     */
    public void setAllNodes(Node[] nodes) {
        allNodes = nodes;
    }

    /**
     * Returns the size of this infrastructure in pixels
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * Sets the size of this infrastructure in pixels
     */
    public void setSize(Dimension s) {
        size = s;
    }

    /**
     * Returns the number of nodes
     */
    public int getNumNodes() {
        return allNodes.length;
    }

    /**
     * Returns the number of edgenodes
     */
    public int getNumSpecialNodes() {
        return specialNodes.length;
    }

    /**
     * Returns the number of junctions
     */
    public int getNumJunctions() {
        return junctions.length;
    }

    public static ArrayList getDisabledLanes() {
        return disabledLanes;
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
    public int getCurCycle() {
        return curCycle;
    }

    /**
     * Returns the total number of signs in the infrastructure
     */
    public int getTotalNumSigns() {
        //count signs
        int result = 0;
        int num_nodes = allNodes.length;
        for (int i = 0; i < num_nodes; i++) {
            result += allNodes[i].getNumSigns();
        }
        return result;
    }

    /**
     * Returns an array containing all statistics of the infrastructure. The
     * index in the array corresponds to the Node id.
     */
    public NodeStatistics[][] getNodeStatistics() {
        NodeStatistics[][] stats = new NodeStatistics[allNodes.length][];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = allNodes[i].getStatistics();
        }
        return stats;
    }

    /**
     * Returns an array containing all statistics of all EdgeNodes. The index in
     * the array corresponds to the EdgeNode id.
     */
    public NodeStatistics[][] getEdgeNodeStatistics() {
        NodeStatistics[][] stats = new NodeStatistics[specialNodes.length][];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = specialNodes[i].getStatistics();
        }
        return stats;
    }

    /**
     * Returns an array containing all statistics of all Junctions. The index in
     * the array corresponds to (Junction_id - edgeNodes.length).
     */
    public NodeStatistics[][] getJunctionStatistics() {
        NodeStatistics[][] stats = new NodeStatistics[junctions.length][];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = junctions[i].getStatistics();
        }
        return stats;
    }

    /**
     * Returns the current count of accidents. (DOAS 06)
     */
    public int getAccidentsCount() {
        return disabledLanes.size() / 2;    //there are two lanes disabled per accident
    }

    /**
     * DOAS 06: Returns the total number of cars removed. (used for statistics
     * purposes)
     */
    public int getRemovedCarsCount() {
        return removedCars;
    }

    /**
     * DOAS 06: Returns the total number of cars entered into the network (used
     * for statistics purposes)
     */
    public int getEnteredCarsCount() {
        return enteredCars;

    }

    /**
     * DOAS 06: increment number of cars removed from network (used for
     * statistics purposes)
     */
    public void removedCarsIncrement() {
        removedCars++;
    }

    /**
     * DOAS 06: increment number of cars entered the network (used for
     * statistics purposes)
     */
    public void enteredCarsIncrement() {
        enteredCars++;
    }

    /**
     * Calculates the total size of this infrastructure and adds a small border
     */
    // TODO needs updating to move turn coords
    private Dimension calcSize() {
        Rectangle rect = new Rectangle();
        Node node;
        Road[] roads;
        Point p;
        for (int i = 0; i < allNodes.length; i++) {
            node = allNodes[i];
            roads = node.getAlphaRoads();
            rect.add(node.getBounds());
            for (int j = 0; j < roads.length; j++) {
                rect.add(roads[j].getBounds());
            }
        }
        int dx = (int) (-rect.width / 2 - rect.x);
        int dy = (int) (-rect.height / 2 - rect.y);
        for (int i = 0; i < allNodes.length; i++) {
            p = allNodes[i].getCoord();
            p.x += dx;
            p.y += dy;
        }
        return new Dimension(rect.width + 100, rect.height + 100);
    }

    /**
     * Gets the EdgeNodes in this Infrastructure. Before using this method think
     * twice if you don't actually need the getSpecialNodes() method. The
     * underscore in the function name was added to emphasize that you probably
     * need another method now.
     */
    public EdgeNode[] getEdgeNodes() {
        ArrayList<EdgeNode> result = new ArrayList<>();
        for (Node tmp : new ArrayList<>(Arrays.asList(specialNodes))) {
            if (tmp instanceof EdgeNode) {
                result.add((EdgeNode) tmp);
            }
        }
        EdgeNode[] resultArray = new EdgeNode[result.size()];
        result.toArray(resultArray);
        return resultArray;
    }

    /**
     * Gets the number of EdgeNodes in the infrastructure
     */
    public int getNumEdgeNodes() {
        return getEdgeNodes().length;
    }

    /**
     * Get the accidents rate (DOAS 06)
     */
    public static int getAccidentsRate() {
        return accidentsRate;
    }

    /**
     * Set the accidents rate (DOAS 06)
     */
    public static void setAccidentsRate(int rate) {
        accidentsRate = rate;
    }


    /*============================================*/
 /* Selectable                                 */
 /*============================================*/
    @Override
    public boolean hasChildren() {
        return getNumNodes() > 0;
    }

    @Override
    public List<Selectable> getChildren() {
        return new ArrayList<>(Arrays.asList(getAllNodes()));
    }


    /*============================================*/
 /* MODIFYING DATA                             */
 /*============================================*/
    /**
     * Adds a node to the infrastructure
     */
    public void addNode(Node node) {
        node.setId(allNodes.length);
        allNodes = (Node[]) Arrayutils.add(allNodes, node);
        if (node instanceof SpecialNode) {
            specialNodes = (SpecialNode[]) Arrayutils.add(specialNodes,
                    node);
        }
        if (node instanceof Junction) {
            junctions = (Junction[]) Arrayutils.add(junctions, node);
        }
    }

    /**
     * Removes a node from the infrastructure
     */
    public void remNode(Node node) throws InfraException {
        allNodes = (Node[]) Arrayutils.remElement(allNodes, node);
        if (node instanceof SpecialNode) {
            specialNodes = (SpecialNode[]) Arrayutils.remElement(specialNodes,
                    node);
        }
        if (node instanceof Junction) {
            junctions = (Junction[]) Arrayutils.remElement(junctions, node);
        }
    }

    /**
     * Resets the entire data structure to allow a new simulation to start This
     * will remove all Roadusers and set all Signs to their default positions,
     * as well as reset all cycleMoved and cycleAsked counters.
     *
     * @see Node#reset()
     */
    public void reset(double derivationFactor) {
        CustomFactory.reset();
        for (int i = 0; i < allNodes.length; i++) {
            allNodes[i].reset();
        }

        //(DOAS 06)
        synchronized (disabledLanes) {
            while (disabledLanes.size() > 0) {    //Because of some race condition issues only this kind of cycle seems to work
                try {
                    enableLane(disabledLanes.size() - 1, derivationFactor);
                } catch (InfraException e) {
                    e.printStackTrace();
                }
            }
        }
        this.removedCars = 0; //DOAS 06 (reset the number of cars removed from model)
        this.enteredCars = 0; //DOAS 06 (reset the number of cars entered into model)
    }

    /**
     * Resets the shortes paths informations (DOAS 06)
     */
    public void resetShortestPaths() {
        for (int i = 0; i < allNodes.length; i++) {
            allNodes[i].zapShortestPaths();
        }
    }

    public void cacheInboundLanes() throws InfraException {
        int num_nodes = allNodes.length;
        allLanes = new ArrayList<>(num_nodes * 3);
        DriveLane[] temp;
        int num_temp;

        for (int i = 0; i < num_nodes; i++) {
            temp = allNodes[i].getInboundLanes();
            num_temp = temp.length;
            for (int j = 0; j < num_temp; j++) {
                allLanes.add(temp[j]);
            }
        }
    }

    public ArrayList getAllInboundLanes() throws InfraException {
        if (allLanes == null) {
            cacheInboundLanes();
        }
        return (ArrayList) allLanes.clone();
    }


    /*============================================*/
 /* LOAD AND SAVE                              */
 /*============================================*/
    public void prepareSave() throws GLDException {
        cacheInboundLanes();
        size = calcSize();
    }

    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws
            XMLTreeException, IOException, XMLInvalidInputException { // Load parameters
        title = myElement.getAttribute("title").getValue();
        author = myElement.getAttribute("author").getValue();
        comments = myElement.getAttribute("comments").getValue();
        size = new Dimension(myElement.getAttribute("width").getIntValue(),
                myElement.getAttribute("height").getIntValue());

        allLanes = (ArrayList<DriveLane>) XMLArray.loadArray(this, loader);
        notYetDisabledLanes = new ArrayList<>(allLanes);
        allNodes = (Node[]) XMLArray.loadArray(this, loader);
        specialNodes = new SpecialNode[myElement.getAttribute("num-specialnodes").getIntValue()];
        junctions = new Junction[allNodes.length - specialNodes.length];
        copySpecialNodes();
        copyJunctions();
        // Internal second stage load of child objects
        Map<String, Map<Integer, TwoStageLoader>> mainMap;
        try {
            mainMap = getMainMap();
        } catch (InfraException e) {
            throw new XMLInvalidInputException("Problem with internal 2nd stage load of infra :" + e);
        }
        XMLUtils.loadSecondStage(allLanes, mainMap);
        XMLUtils.loadSecondStage(new ArrayList<>(Arrays.asList(allNodes)), mainMap);
    }

    @Override
    public XMLElement saveSelf() {
        XMLElement result = new XMLElement("infrastructure");
        result.addAttribute(new XMLAttribute("title", title));
        result.addAttribute(new XMLAttribute("author", author));
        result.addAttribute(new XMLAttribute("comments", comments));
        result.addAttribute(new XMLAttribute("height", size.height));
        result.addAttribute(new XMLAttribute("width", size.width));
        result.addAttribute(new XMLAttribute("file-version", version));
        result.addAttribute(new XMLAttribute("num-specialnodes", specialNodes.length));
        laneMap = (HashMap) (getLaneSignMap());
        return result;
    }

    @Override
    public void saveChilds(XMLSaver saver) throws XMLTreeException, IOException,
            XMLCannotSaveException {
        XMLArray.saveArray(allLanes, this, saver, "lanes");
        XMLArray.saveArray(allNodes, this, saver, "nodes");
    }

    @Override
    public String getXMLName() {
        return parentName + ".infrastructure";
    }

    @Override
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Map<String, Map<Integer, TwoStageLoader>> getMainMap() throws InfraException {
        Map<String, Map<Integer, TwoStageLoader>> result = new HashMap<>();
        result.put("lane", getLaneSignMap());
        result.put("node", getNodeMap());
        result.put("road", getRoadMap());
        return result;
    }

    protected Map<Integer, TwoStageLoader> getLaneSignMap() {
        Map<Integer, TwoStageLoader> result = new HashMap<>();
        Iterator lanes = allLanes.iterator();
        DriveLane tmp;
        while (lanes.hasNext()) {
            tmp = (DriveLane) (lanes.next());
            result.put(tmp.getId(), tmp);
        }
        return result;
    }

    protected Map<Integer, TwoStageLoader> getNodeMap() {
        Map<Integer, TwoStageLoader> result = new HashMap<>();
        Iterator nodes = new ArrayIterator(allNodes);
        Node tmp;
        while (nodes.hasNext()) {
            tmp = (Node) (nodes.next());
            result.put(tmp.getId(), tmp);
        }
        return result;
    }

    protected Map<Integer, TwoStageLoader> getRoadMap() throws InfraException {
        Map<Integer, TwoStageLoader> result = new HashMap<>();
        for (Node tmp : Arrays.asList(allNodes)) {
            if (tmp instanceof SpecialNode) {
                addAlphaRoads(result, (SpecialNode) (tmp));
            } else if (tmp instanceof Junction) {
                addAlphaRoads(result, (Junction) (tmp));
            } else {
                throw new InfraException("Unknown type of node : " + tmp.getName());
            }
        }
        return result;
    }

    protected void copySpecialNodes() {
        int specialNodePos = 0;
        for (Node node : allNodes) {
            if (node instanceof SpecialNode) {
                specialNodes[specialNodePos++] = (SpecialNode) (node);
            }
        }
    }

    protected void copyJunctions() {
        int junctionPos = 0;
        for (Node node : allNodes) {
            if (node instanceof Junction) {
                junctions[junctionPos++] = (Junction) (node);
            }
        }
    }

    protected void addAlphaRoads(Map<Integer, TwoStageLoader> d, SpecialNode n) {
        if (n.getAlpha()) {
            d.put(n.getRoad().getId(), n.getRoad());
        }
    }

    protected void addAlphaRoads(Map<Integer, TwoStageLoader> d, Junction n) {
        for (Road road : Arrays.asList(n.getAlphaRoads())) {
            d.put(road.getId(), road);
        }
    }
}
