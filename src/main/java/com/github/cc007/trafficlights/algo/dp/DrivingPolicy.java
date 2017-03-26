
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

package com.github.cc007.trafficlights.algo.dp;

import com.github.cc007.trafficlights.GLDSim;
import com.github.cc007.trafficlights.algo.dp.*;
import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.sim.*;
import com.github.cc007.trafficlights.xml.*;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Random;

/**
 *
 * This is the abstract class for each driving policy.
 *
 * @author Group Algorithms
 * @version 1.0
 *
 */
public abstract class DrivingPolicy implements XMLSerializable,TwoStageLoader
{
	protected SimModel model;
	protected TLController tlc;
        protected Random random = new Random(GLDSim.seriesSeed[GLDSim.seriesSeedIndex]);    //random number generator must be independent on other generators (to avoid magic influence) (DOAS 06)

	DrivingPolicy(SimModel m, TLController _tlc) {
		model = m;
		tlc = _tlc;
	}
        
        /** Reset neccessary stuff (DOAS 06)
         */
        public void reset(){
            random = new Random(GLDSim.seriesSeed[GLDSim.seriesSeedIndex]);
        }

	/**
	 * The lane to which a car continues his trip.
	 * @param r The road user being asked.
	 * @param allOutgoing All the possible outgoing lanes
	 * @param shortest All the lanes which are in a shortest path to the car's destination
	 * @return The chosen lane.
	 */
	public DriveLane getDirection(Roaduser r, DriveLane lane_now, Node node_now) throws InfraException {
                DriveLane[] lanesleadingfrom = node_now.getLanesLeadingFrom(
                        lane_now, r.getType());
                DriveLane[] shortestpaths = node_now.getShortestPaths(r.
                        getDestNode().getId(), r.getType());
                
                DriveLane result = getDirectionLane(r, lane_now, lanesleadingfrom,
                                        shortestpaths);

                //Because of the accidents the shortest path might not be among the possible directions (DOAS 06)
                //But if rerouting is off, we like the "null way"
                //if(lanesleadingfrom.length == 0) System.out.println("Uh: " + lane_now.getName() + " " + node_now.getName());
                if(result == null && this.model.getSimController().getRerouting()){
                    if(lanesleadingfrom.length == 0){
                        return null;    //f. e. no line leads from the EdgeNode
                    } else {
                        //random lane; TODO: find smarter solution
                        //look for the line which does not lead to the EndgeNode (not even two steps ahead)
                        //return lanesleadingfrom[0];
                        DriveLane[] possible = new DriveLane[lanesleadingfrom.length];
                        int cnt = 0;
                        for(int i = 0; i < lanesleadingfrom.length; i++){
                            if(lanesleadingfrom[i].getNodeLeadsTo().getType() != Node.EDGE){
                                DriveLane[] lanesTwoStepsAhead = lanesleadingfrom[i].getNodeLeadsTo().getLanesLeadingFrom(lanesleadingfrom[i], r.getType());
                                for(int j = 0; j < lanesTwoStepsAhead.length; j++){
                                    if(lanesTwoStepsAhead[j].getNodeLeadsTo().getType() != Node.EDGE){
                                        possible[cnt] = lanesleadingfrom[i];
                                        cnt++;
                                        break;  //we do not need to look for other excuse for lanesleadingfrom[i]
                                    }
                                }
                            }
                        }
                        if(cnt > 0){
                            DriveLane selectedLane = possible[random.nextInt(cnt)];
                            //System.out.println("Forced way out [" + node_now.getId() + ":" + selectedLane.getId() + "]" + i);
                            return selectedLane;
                        } else {
                            /*if(lanesleadingfrom[0].getNodeLeadsTo().getId() != r.getDestNode().getId()){
                                //now it is critical: TODO: this car must leave the city in the wrong EdgeNode
                                System.out.println("Found no way out to " + r.getDestNode().getId() + "[" + node_now.getId() + ":"
                                                    + lane_now.getId()
                                                    + "]: possibilities " + lanesleadingfrom.length
                                                    + " - first one (" + lanesleadingfrom[0].getName()
                                                    + ") leads to " + lanesleadingfrom[0].getNodeLeadsTo().getName());
                            }*/
                            return lanesleadingfrom[0];
                        }
                    }
                } else {
                    return result;
                }
	}

	public abstract DriveLane getDirectionLane(Roaduser r, DriveLane lane_now, DriveLane[] allOutgoing, DriveLane[] shortest);

	// Generic XMLSerializable implementation
	/**Empty for Drivingpolicy*/
	public void load (XMLElement myElement,XMLLoader loader) throws XMLTreeException,IOException,XMLInvalidInputException
	{ 	// Empty
	}
	/**Empty for Drivingpolicy*/
	public void saveChilds (XMLSaver saver) throws XMLTreeException,IOException,XMLCannotSaveException
	{ 	// Empty
	}


	public void setParentName (String parentName) throws XMLTreeException
	{	throw new XMLTreeException
		("Attempt to change fixed parentName of a DP class.");
	}

	// Empty TwoStageLoader (standard)

	public void loadSecondStage (Dictionary dictionaries) throws XMLInvalidInputException,XMLTreeException
	{}
}
