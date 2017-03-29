
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

import com.github.cc007.trafficlights.sim.*;
import com.github.cc007.trafficlights.algo.dp.*;
import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.xml.*;

import java.io.IOException;

/**
 *
 * @author Group Algorithms
 * @version 1.0
 *
 * This is the class for driving policy using colearning.
 */
public class ColearnPolicy extends DrivingPolicy {

    public static final String shortXMLName = "dp-cl";
    protected Colearning tlc;

    /**
     * The constructor for a colearning TC-1 driving policy.
     *
     * @param m The model which is used
     */
    public ColearnPolicy(SimModel _m, TLController _tlc) {
        //TODO check if the tlc magic (field hiding and self casting) actually does what it is supposed to do
        super(_m, _tlc);
        tlc = (Colearning) tlc;
        if (tlc == null) {
            tlc = (Colearning) _m.getTLController();
        }
    }

    /**
     * The lane to which a car continues his trip.
     *
     * @param r The road user being asked.
     * @param allOutgoing All the possible outgoing lanes
     * @param shortest All the lanes which are in a shortest path to the car's
     * destination
     * @return The chosen lane.
     */
    @Override
    public DriveLane getDirectionLane(Roaduser r, DriveLane now_here, DriveLane[] allOutgoing, DriveLane[] shortest) {
        int num_sp = shortest.length;
        int num_out = allOutgoing.length;
        int laneI;
        DriveLane best = null;
        DriveLane lane;
        Node dest = r.getDestNode();
        float bestV = Float.MAX_VALUE;
        float laneV;

        for (int i = 0; i < num_out; i++) {
            for (int j = 0; j < num_sp; j++) {
                if (allOutgoing[i].getId() == shortest[j].getId()) {
                    lane = shortest[j];
                    if (lane.getSign().getType() == Sign.TRAFFICLIGHT) {
                        laneV = tlc.getColearnValue(now_here.getSign(), lane.getSign(), dest, 0);
                        if (laneV < bestV) {
                            bestV = laneV;
                            best = lane;
                        }
                    }
                    if (best == null) {
                        best = lane;
                    }
                }
            }
        }

        return best;
    }

    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
        System.out.println("DP-CL loaded");
    }

    @Override
    public XMLElement saveSelf() throws XMLCannotSaveException {
        return new XMLElement(shortXMLName);
    }

    @Override
    public String getXMLName() {
        return "model." + shortXMLName;
    }

}
