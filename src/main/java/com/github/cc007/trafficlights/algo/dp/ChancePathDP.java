/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.cc007.trafficlights.algo.dp;

import static com.github.cc007.trafficlights.algo.dp.ColearnPolicy.shortXMLName;
import com.github.cc007.trafficlights.algo.tlc.TLController;
import com.github.cc007.trafficlights.infra.DriveLane;
import com.github.cc007.trafficlights.infra.Roaduser;
import com.github.cc007.trafficlights.infra.RoaduserFactory;
import com.github.cc007.trafficlights.sim.SimModel;
import com.github.cc007.trafficlights.xml.XMLCannotSaveException;
import com.github.cc007.trafficlights.xml.XMLElement;
import java.util.Random;

/**
 *
 * @author Rik
 */
public class ChancePathDP extends DrivingPolicy{

    public static final String shortXMLName = "dp-cp";
    
    public ChancePathDP(SimModel m, TLController _tlc) {
        super(m, _tlc);
    }

    @Override
    public DriveLane getDirectionLane(Roaduser r, DriveLane lane_now, DriveLane[] allOutgoing, DriveLane[] shortest) {
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
        int total = 0;
        int[] subsetLengths = new int[subset.length];
        for (int i=0;i<subset.length;i++) {
            subsetLengths[i] =  subset[i].getNodeComesFrom().getShortestPathMinLength(r.getDestNode().getId(), r.getType());
            total += subsetLengths[i];
        }
        
        if (index > 0) {
            Random generator = model.getRNGen();
            int i = generator.nextInt(total);
            return subset[i];
        } else {
            //System.out.println ("Couldnt't get direction in DP");
            return null;  //Something very funny is going on now
        }
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
