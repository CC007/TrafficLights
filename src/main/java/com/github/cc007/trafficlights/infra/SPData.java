
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

import com.github.cc007.trafficlights.utils.Arrayutils;
import com.github.cc007.trafficlights.xml.*;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * Holds data needed to find the shortest path from a node to an exit node.
 *
 * @author Group Datastructures
 * @version 1.0
 */
public class SPData implements XMLSerializable, TwoStageLoader, InstantiationAssistant {

    /**
     * The paths known
     */
    protected ArrayList<Path> paths;
    protected String parentName = "model.infrastructure.node";

    public SPData() {
        paths = new ArrayList<>(2);
    }

    public SPData(int size) {
        paths = new ArrayList<>(size);
    }


    /*============================================*/
 /* GETS                                       */
 /*============================================*/
    /**
     * Returns an array of Drivelanes that are on 1 of the shortest paths from
     * the node this SPData belongs to, to the Node with exiNodeId, for
     * Roadusers with type ruType.
     *
     * @param exitNodeId The Id of the exit node that is your destination.
     * @param ruType The type of Roaduser.
     * @return an array of Drivelanes.
     */
    public DriveLane[] getShortestPaths(int exitNodeId, int ruType) {
        //System.out.println("SPData.Getting shortestPath to:"+exitNodeId+" with type:"+ruType+" from "+paths.size());
        Path p = getPath(exitNodeId, ruType);
        /*System.out.println("SPData.Gotten:"+p.getNodeId()+","+p.getRUType());
		System.out.println("SPData.With "+p.getLanes());*/

        if (p != null) {
            return p.getLanes();
        } else {
            return new DriveLane[0];
        }
    }

    public int[] getShortestPathDestinations(int ruType) {
        Path p;
        int num_paths = paths.size();
        int counter = 0;
        int[] ps = new int[num_paths];
        for (int i = 0; i < num_paths; i++) {
            p = (Path) paths.get(i);
            if (p.getRUType() == ruType) {
                ps[counter] = p.getNodeId();
                counter++;
            }
        }
        if (counter < num_paths) {
            return (int[]) Arrayutils.cropArray(ps, counter);
        } else {
            return ps;
        }
    }
    
     /**
     * Returns an array of Drivelanes that are on 1 of the shortest paths from
     * the node this SPData belongs to, to the Node with exiNodeId, for
     * Roadusers with type ruType.
     *
     * @param exitNodeId The Id of the exit node that is your destination.
     * @param ruType The type of Roaduser.
     * @return an array of Drivelanes.
     */
    public int getShortestPathMinLength(int exitNodeId, int ruType) {
        //System.out.println("SPData.Getting shortestPath to:"+exitNodeId+" with type:"+ruType+" from "+paths.size());
        Path p = getPath(exitNodeId, ruType);
        /*System.out.println("SPData.Gotten:"+p.getNodeId()+","+p.getRUType());
		System.out.println("SPData.With "+p.getLanes());*/

        if (p != null) {
            return p.getMinLength();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /*============================================*/
 /* SETS                                       */
 /*============================================*/
    /**
     * Sets the shortest path to given exit node using roaduser type to given
     * DriveLane
     *
     * @param lane The DriveLane to set the path to
     * @param exitNodeId The Id of the exit node this path leads to
     * @param ruType The type of Roaduser
     */
    public void setShortestPath(DriveLane lane, int exitNodeId, int ruType, int length) {
        Path p = getPath(exitNodeId, ruType);
        if (p == null) {
            paths.add(new Path(exitNodeId, ruType, lane, length));
        } else {
            p.empty();
            DriveLane[] lanes = {lane};
            Integer[] lengths = {length};
            p.setLanes(lanes, lengths);
        }
    }

    /*============================================*/
 /* ADDS                                       */
 /*============================================*/
    /**
     * Adds a DriveLane to the lanes already found for exitNodeId and ruType.
     *
     * @param lane The DriveLane to add to the path
     * @param exitnodeId The Id of the exit node this path leads to
     * @param ruType The type of Roaduser
     */
    public void addShortestPath(DriveLane lane, int exitNodeId, int ruType, int length) {
        Path p = getPath(exitNodeId, ruType);
        if (p == null) {
            paths.add(new Path(exitNodeId, ruType, lane, length));
        } else {
            p.addLane(lane, length);
        }
    }

    /*============================================*/
 /* REMOVES                                    */
 /*============================================*/
    /**
     * Removes all Drivelanes found for exitNodeId and ruType.
     *
     * @param exitnodeId The Id of the exit node this path leads to
     * @param ruType The type of Roaduser
     */
    public void remAllPaths(int exitNodeId, int ruType) {
        Path p = getPath(exitNodeId, ruType);
        if (p != null) {
            p.empty();
        }
    }

    /**
     * Removes all the Drivelanes found for exitNodeId and ruType and length >
     * length
     *
     * @param exitId The Id of the exit node this path leads to
     * @param ruType The type of Roaduser
     * @param length The maximum length a path may have to remain
     */
    public void remPaths(int exitId, int ruType, int length) {
        Path p = getPath(exitId, ruType);
        p.remLanes(length);
    }


    /*============================================*/
 /* PRIVATE                                    */
 /*============================================*/
    /**
     * Gets the Path object for given Node Id and Roaduser type
     */
    private Path getPath(int exitNodeId, int ruType) {
        Path p;
        for (int i = 0; i < paths.size(); i++) {
            p = (Path) paths.get(i);
            if (p.getNodeId() == exitNodeId && p.getRUType() == ruType) {
                return p;
            }
        }
        return null;
    }

    /*============================================*/
 /* Load/save                                  */
 /*============================================*/
    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
        paths = (ArrayList<Path>) XMLArray.loadArray(this, loader, this);
    }

    @Override
    public XMLElement saveSelf() throws XMLCannotSaveException {
        XMLElement result = new XMLElement("spdata");
        result.addAttribute(new XMLAttribute("num-paths", paths.size()));
        return result;
    }

    @Override
    public void saveChilds(XMLSaver saver) throws XMLTreeException, IOException, XMLCannotSaveException {
        XMLArray.saveArray(paths, this, saver, "paths");
    }

    @Override
    public String getXMLName() {
        return parentName + ".spdata";
    }

    @Override
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    @Override
    public void loadSecondStage(Map maps) throws XMLInvalidInputException, XMLTreeException {
        Iterator it = paths.iterator();
        while (it.hasNext()) {
            ((Path) (it.next())).loadSecondStage(maps);
        }
    }

    @Override
    public boolean canCreateInstance(Class request) {
        return Path.class.equals(request);
    }

    @Override
    public Object createInstance(Class request) throws
            ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (Path.class.equals(request)) {
            return new Path();
        } else {
            throw new ClassNotFoundException("SPData IntstantiationAssistant cannot make instances of "
                    + request);
        }
    }


    /*============================================*/
 /* Internal classes                           */
 /*============================================*/
    /**
     * One Path holds all known Drivelanes that are on a shortest path to a
     * given exitNode using a Roaduser with a certain type, starting at the Node
     * SPData belongs to
     */
    protected class Path implements XMLSerializable, TwoStageLoader {

        private int exitNodeId;
        private int min_length = Integer.MAX_VALUE;
        private int max_length = Integer.MAX_VALUE;
        private int ruType;
        private Integer[] lengths;
        private DriveLane[] lanes;
        protected String parentName = "model.infrastructure.node.spdata";
        private TwoStageLoaderData loadData = new TwoStageLoaderData();

        public Path() {// For loading
        }

        Path(int size) {
            lanes = new DriveLane[size];
            lengths = new Integer[size];
        }

        Path(int exitId, int ruT) {
            exitNodeId = exitId;
            ruType = ruT;
            lanes = null;
            lengths = new Integer[0];
        }

        Path(int exitId, int ruT, DriveLane l, int length) {
            exitNodeId = exitId;
            ruType = ruT;
            DriveLane[] nlanes = {l};
            lanes = nlanes;
            Integer[] nlengths = {new Integer(length)};
            lengths = nlengths;
        }

        /**
         * Returns all lanes
         */
        public DriveLane[] getLanes() {
            return lanes;
        }

        /**
         * Sets all lanes
         */
        public void setLanes(DriveLane[] l, Integer[] lens) {
            lanes = l;
            lengths = lens;
        }

        /**
         * Returns the Id of the exitNode
         */
        public int getNodeId() {
            return exitNodeId;
        }

        /**
         * Sets the Id of the exitNode
         */
        public void setNodeId(int id) {
            exitNodeId = id;
        }

        /**
         * Returns the Roaduser type
         */
        public int getRUType() {
            return ruType;
        }

        /**
         * Sets the Roaduser type
         */
        public void setRUType(int t) {
            ruType = t;
        }

        /**
         * Add one DriveLane
         */
        public void addLane(DriveLane l, int length) {
            int oldlen = lanes.length;
            lanes = (DriveLane[]) Arrayutils.addUnique(lanes, l);
            if (oldlen < lanes.length) {
                // Something added
                lengths = (Integer[]) Arrayutils.add(lengths, length);
            }
        }

        /**
         * Remove a DriveLane
         */
        public void remLane(DriveLane l) {
            lanes = (DriveLane[]) Arrayutils.remElement(lanes, l);
        }

        /**
         * Remove all Drivelanes with pathlength > length
         */
        public void remLanes(int length) {
            for (int i = 0; i < lanes.length; i++) {
                if (lengths[i].intValue() > length) {
                    // Removing
                    lanes = (DriveLane[]) Arrayutils.remElement(lanes, i);
                    lengths = (Integer[]) Arrayutils.remElement(lengths, i);
                }
            }
        }
        
        public int getMinLength() {
            int minLength = Integer.MAX_VALUE;
            for (Integer length : lengths) {
                if(length > 0 && length < minLength){
                    minLength = length;
                }
            }
            return minLength;
        }


        /**
         * Remove all Drivelanes
         */
        public void empty() {
            lanes = null;
        }

        //XMLSerializable implementation
        @Override
        public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
            ruType = myElement.getAttribute("ru-type").getIntValue();
            exitNodeId = myElement.getAttribute("exit-node").getIntValue();
            loadData.laneIds = (int[]) XMLArray.loadArray(this, loader);
            lengths = (Integer[]) XMLArray.loadArray(this, loader);
        }

        @Override
        public XMLElement saveSelf() throws XMLCannotSaveException {
            XMLElement result = new XMLElement("path");
            result.addAttribute(new XMLAttribute("ru-type", ruType));
            result.addAttribute(new XMLAttribute("exit-node", exitNodeId));
            return result;
        }

        @Override
        public void saveChilds(XMLSaver saver) throws XMLTreeException, IOException, XMLCannotSaveException {
            XMLArray.saveArray(getLaneIdArray(), this, saver, "lanes");
            XMLArray.saveArray(lengths, this, saver, "lengths");
        }

        @Override
        public String getXMLName() {
            return parentName + ".path";
        }

        @Override
        public void setParentName(String parentName) {
            this.parentName = parentName;
        }

        public int[] getLaneIdArray() {
            int[] result = new int[lanes.length];
            for (int t = 0; t < lanes.length; t++) {
                if (lanes[t] == null) {
                    result[t] = -1;
                } else {
                    result[t] = lanes[t].getSign().getId();
                }
            }
            return result;
        }

        @Override
        public void loadSecondStage(Map maps) throws XMLInvalidInputException, XMLTreeException {
            Map laneMap = (Map) (maps.get("lane"));
            lanes = new DriveLane[loadData.laneIds.length];
            for (int t = 0; t < loadData.laneIds.length; t++) {
                lanes[t] = (DriveLane) (laneMap.get(new Integer(loadData.laneIds[t])));
            }
        }

        public class TwoStageLoaderData {

            int[] laneIds;
        }

    }
}

/**
 * *************
 ** OLD CODE * * *
 *
 * /**
 * Returns an array of all the Drivelanes that are on a shortest path from the
 * node this SPData belongs to, to the Node with exiNodeId.
 *
 * @param exitNodeId The Id of the exit node that is your destination.
 * @return an array of Drivelanes.
 */
/*	public DriveLane[] getShortestPaths(int exitNodeId) {
		
		ArrayList temp_vector = new ArrayList();
		Path p;
		int lane_counter = 0;
		for (int i=0; i < paths.size(); i++) {
			p = (Path)paths.get(i);
			if (p.getNodeId() == exitNodeId)
			{
				lane_counter += p.getLanes().length;
				temp_vector.add(p);
			}
		}
		int temp_length = temp_vector.size();
		int pos_counter = 0;
		
		// duplicates!
		DriveLane[] lanes = new DriveLane[lane_counter];
		DriveLane[] temp_lanes;
		for (int i=0;i<temp_length;i++)
		{
			p = (Path) temp_vector.get(i);
			temp_lanes = p.getLanes();
			System.arraycopy(temp_lanes, 0, lanes, pos_counter, temp_lanes.length);
			pos_counter += temp_lanes.length;
		}
		temp_lanes  = null;
		temp_vector = null;
		return lanes;
	} */
