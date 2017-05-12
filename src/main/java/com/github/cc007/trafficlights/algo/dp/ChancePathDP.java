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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Rik
 */
public class ChancePathDP extends DrivingPolicy {

    public static final String shortXMLName = "dp-cp";

    /**
     * Constructor for the Chance Path driving policy
     *
     * @param sim The model which is used
     * @param _tlc The traffic light controller which is used
     */
    public ChancePathDP(SimModel sim, TLController _tlc) {
        super(sim, _tlc);
        sim.setDerivationFactor(2.0);
    }

    @Override
    public DriveLane getDirectionLane(Roaduser r, DriveLane lane_now, DriveLane[] allOutgoing, DriveLane[] shortest) {
        List<DriveLane> subset = new ArrayList<>();
        int index = 0;
        int num_outgoing = allOutgoing.length;
        int num_shortest = shortest.length;

        for (int i = 0; i < num_outgoing; i++) {
            for (int j = 0; j < num_shortest; j++) {
                if (allOutgoing[i] != null && allOutgoing[i].getId() == shortest[j].getId()) {
                    subset.add(allOutgoing[i]);
                    index++;
                }
            }
        }
        double total = 0;
        double[] subsetLengths = new double[subset.size()];
        for (int i = 0; i < subset.size(); i++) {
            subsetLengths[i] = 1 / (double)subset.get(i).getNodeLeadsTo().getShortestPathMinLength(r.getDestNode().getId(), r.getType());
            
            total += subsetLengths[i];
        }

        if (index > 0) {
            Random generator = model.getRNGen();
            double wantedLength = generator.nextDouble() * total;
            for (int i = 0; wantedLength >= 0; i++) {
                if (subsetLengths[i] >= wantedLength) {
                    return subset.get(i);
                }
                wantedLength -= subsetLengths[i];
            }

        }
        //System.out.println ("Couldnt't get direction in DP");
        return null;  //Something very funny is going on now

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
