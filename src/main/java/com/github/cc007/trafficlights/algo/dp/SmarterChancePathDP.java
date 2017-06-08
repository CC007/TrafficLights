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
 * @author Rik Schaaf
 */
public class SmarterChancePathDP extends DrivingPolicy {

    public static final String shortXMLName = "dp-cp2";
    public double averageDriveLaneScore = 0.5;
    public double averageLenghtScore = 0.5;

    /**
     * Constructor for the Chance Path driving policy
     *
     * @param sim The model which is used
     * @param _tlc The traffic light controller which is used
     */
    public SmarterChancePathDP(SimModel sim, TLController _tlc) {
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
            DriveLane current = subset.get(i);
            double driveLaneScore = current.getLength() == 0 ? 0 : current.getNumRoadusersWaiting() * r.getLength() / current.getLength();
            double lengthScore = 1 / (double) current.getNodeLeadsTo().getShortestPathMinLength(r.getDestNode().getId(), r.getType());
            subsetLengths[i] = (lengthScore * averageDriveLaneScore + driveLaneScore * averageLenghtScore) / 2;
            averageDriveLaneScore = (averageDriveLaneScore * 49 + driveLaneScore)/50;
            averageLenghtScore = (averageLenghtScore * 49 + lengthScore)/50;

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
