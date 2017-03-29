
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

import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.sim.*;
import com.github.cc007.trafficlights.xml.*;
import java.io.IOException;
import java.util.Random;

/**
 *
 * This extension of {
 *
 * @see gld.DrivingPolicy} selects the next lane by finding one which on the
 * shortest path to road user's destination.
 *
 * @author Group Algorithms
 * @version 1.0
 */
public class ShortestPathDP extends DrivingPolicy {

    public static final String shortXMLName = "dp-sp";

    /**
     * The constructor for a shortest driving policy.
     *
     * @param m The model which is used
     */
    public ShortestPathDP(SimModel sim, TLController _tlc) {
        super(sim, _tlc);
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
    public DriveLane getDirectionLane(Roaduser r, DriveLane lane_now, DriveLane[] allOutgoing, DriveLane[] shortest) {
        //Create a subset from the 2 sets allOutgoing and shortest
        DriveLane[] subset;
        int index = 0;
        int num_outgoing = allOutgoing.length;
        int num_shortest = shortest.length;

        if (num_shortest < num_outgoing) {
            subset = new DriveLane[num_shortest];
        } else {
            subset = new DriveLane[num_outgoing];
        }

        for (int i = 0; i < num_outgoing; i++) {
            for (int j = 0; j < num_shortest; j++) {
                if (allOutgoing[i].getId() == shortest[j].getId()) {
                    subset[index] = allOutgoing[i];
                    index++;
                }
            }
        }

        if (index > 0) {
            Random generator = model.getRNGen();
            int i = (int) Math.floor(generator.nextFloat() * index);
            while (i == index) {
                i = (int) Math.floor(generator.nextFloat() * index);
            }
            return subset[i];
        } else {
            //System.out.println ("Couldnt't get direction in DP");
            return null;  //Something very funny is going on now
        }
    }

    // Trivial XMLSerializable implementation
    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
        System.out.println("DP SP loaded");
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
