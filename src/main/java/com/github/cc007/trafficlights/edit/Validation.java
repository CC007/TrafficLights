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
package com.github.cc007.trafficlights.edit;

import com.github.cc007.trafficlights.algo.edit.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.utils.*;
import java.util.*;

/* TO DO:
 in checkConnected moet de 2 vervangen worden door een constante die aangeeft hoeveel typen Roadusers er zijn
 andere validaties
 testen, er is momenteel geen garantie dat het geheel goed werkt.
 */
/**
 *
 * This method validates the entire infrastructure It adds IDs for nodes
 * (starting with edgenodes). It adds IDs for Roads and Drivelanes. It checks
 * wheter there are any 'empty' nodes It checks the number of EdgeNodes (should
 * be >2) It adds SPData and SCData. It checks the connectedness of the graph
 * for each type.
 *
 * @author Group GUI & Algo
 * @version 1.0
 */
public class Validation {

    Infrastructure infra;

    public Validation(Infrastructure i) {
        infra = i;
    }

    protected void sortNodeArray(Node[] nodes) {
        int num_nodes = nodes.length;

        for (int i = 0; i < num_nodes; i++) {
            if (nodes[i].getId() != i) {
                // Node at [i] isnt at the right position in the array.
                Node temp = nodes[i];
                nodes[i] = nodes[temp.getId()];
                nodes[temp.getId()] = temp;
            }
        }
    }

    public ArrayList<String> validate() throws InfraException {
        ArrayList<String> errors = new ArrayList<>();

        SpecialNode[] specialNodes = infra.getSpecialNodes();
        Node[] nodes = infra.getAllNodes();
        int num_nodes = nodes.length;
        int num_special = specialNodes.length;

        // Make sure the EdgeNodes are up front.
        int special_index = 0;
        for (int i = 0; i < num_nodes; i++) {
            if (nodes[i] instanceof SpecialNode) {
                if (i != special_index) {
                    Node temp = nodes[special_index];
                    nodes[special_index] = nodes[i];
                    nodes[i] = temp;
                }
                special_index++;
            }
        }

        // Add ID's etc.
        ArrayList<String> addErrors = addIDs();
        errors.addAll(addErrors);

        // ** Describe the infrastructure for descriptive-purposes:
        /*System.out.println(":: Validating Infrastructure of "+num_nodes+" Nodes");
           System.out.println(":: All Nodes:");
           // Putting all Nodes out:
           for(int i=0;i<num_nodes;i++)
           {
         String roads = "";
         if(nodes[i] instanceof EdgeNode)
         {
          roads = " road:"+((EdgeNode)nodes[i]).getRoad();
         }
         else
         {
          Road[] roadar = nodes[i].getAllRoads();
         roads = " roads:("+roadar[0]+","+roadar[1]+","+roadar[2]+","+roadar[3]+")";
         }
         System.out.println(nodes[i].getClass()+" with Id:"+nodes[i].getId()+roads);
           }*/

 /*System.out.println(":: All Roads:");
           // Putting all Roads out:
           for(int i=0;i<num_nodes;i++) {
         Node n = nodes[i];
         Road[] roads = n.getAlphaRoads();
         if(roads!=null) {
          int num_roads = roads.length;

          for(int j=0;j<num_roads;j++) {
           Road r = roads[j];
           Node alpha = r.getAlphaNode();
           Node beta = r.getBetaNode();
           Road[] brs = beta.getAllRoads();
           Road[] ars = alpha.getAllRoads();
           int arp = 0;
           int brp = 0;
           for(int k=0;k<brs.length;k++) {
            if(brs[k]==r)
             brp = k;
           }
           for(int k=0;k<ars.length;k++) {
            if(ars[k]==r)
             arp = k;
           }

           System.out.println("Road with "+r.getAlphaLanes().length+" lanes towards pos:"+arp+" Node:"+r.getAlphaNode().getId()+" and "+r.getBetaLanes().length+" lanes towards pos:"+brp+" Node:"+r.getBetaNode().getId());
          }
         }
           }*/

 /*System.out.println(":: All Drivelanes:");
           for(int i=0;i<num_nodes;i++)
           {
         Node n = nodes[i];
         Road[] nroads = n.getAlphaRoads();
         int num_nroads = nroads.length;

         for(int j=0;j<num_nroads;j++)
         {
          Road road = nroads[j];
          DriveLane[] dlanes = road.getAllLanes();
          int num_lanes = dlanes.length;

          for(int k=0;k<num_lanes;k++)
          {
           Node danode = dlanes[k].getNodeLeadsTo();
           Road droad = dlanes[k].getRoad();
           Road[] dnroads = danode.getAllRoads();
           int road_index = -1;
           for(int r=0;r<dnroads.length;r++)
            if(dnroads[r]==droad)
             road_index=r;

           boolean[] targets = dlanes[k].getTargets();
           System.out.println("DriveLane towards Node:"+danode.getId()+" at pos "+road_index+" with targets ("+targets[0]+","+targets[1]+","+targets[2]+")");
          }
         }
           }*/
        // Check the infrastructure:
        //  numspecialNodesodes >=2
        //  nodes should have at least 1 road connected to it
        ArrayList<String> chInfErrors = checkInfra();
        if (chInfErrors.size() > 0) {
            errors.add("ERROR(S) found in infrastructure-checking:");
            errors.addAll(chInfErrors);
        }

        ArrayList<String> spFreqErrors = checkSpawnFreqs();

        if (spFreqErrors.size() > 0) {
            errors.add("ERROR(S) found in spawn frequency-checking:");
            errors.addAll(spFreqErrors);
        }

        ArrayList<String> turnJustErrors = turnJustification();
        if (turnJustErrors.size() > 0) {
            errors.add("ERROR(S) found in turn Justification:");
            errors.addAll(turnJustErrors);
        }

        // Add SignConfigurations
        addSCData();

        // Add ShortestPathdata
        addSPData();

        // check connectedness of the graph, this should be done after the shortestpathdata is added.
        ArrayList<String> checkConnectedErrors = checkConnected();
        if (checkConnectedErrors.size() > 0) {
            errors.add("ERROR(S) found in Connected-checking:");
            errors.addAll(checkConnectedErrors);
        }

        // Now combining with addFreqs
        ArrayList<String> connectedAndFreqErrors = addFrequencies();
        if (connectedAndFreqErrors.size() > 0) {
            errors.add("ERROR(S) found in Connected and or Frequency checking/setting:");
            errors.addAll(connectedAndFreqErrors);
        }
        return errors;
    }

    /**
     * Add default spawning and destination frequencies to all EdgeNodes.
     */
    private ArrayList<String> addFrequencies() throws InfraException {
        ArrayList<String> errors = new ArrayList<>();
        SpecialNode[] specialNodes = infra.getSpecialNodes();
        int numSpecialNodes = infra.getNumSpecialNodes();
        float edgeChance = (float) 1.0 / (infra.getNumSpecialNodes() - 1);

        int ruTypes[] = RoaduserFactory.getConcreteTypes();
        int numRuTypes = ruTypes.length;

        Iterator edgeNodes = Arrays.asList(infra.getEdgeNodes()).iterator();
        while (edgeNodes.hasNext()) {
            EdgeNode edge = (EdgeNode) (edgeNodes.next());
            DriveLane[] lanes = edge.getOutboundLanes();
            int lanetypes = 0;
            for (DriveLane lane : lanes) {
                lanetypes |= lane.getType();
            }
            // now we've got an array of roadusers that can get on the road from here
            int[] types = Typeutils.getTypes(lanetypes);

            if (edge.getSpawnFrequencies().length == 0) {
                SpawnFrequency[] spawn = new SpawnFrequency[numRuTypes];
                int ruTypeCount = 0;
                for (int j = 0; j < types.length; j++) {
                    int[] dests = edge.getShortestPathDestinations(types[j]);
                    //System.out.println("Now checking for type:"+types[j]+" there are "+dests.length);
                    if (dests.length > 0) {
                        float freq = types[j] == RoaduserFactory.BUS ? 0.05f
                                : 0.25f;
                        spawn[ruTypeCount] = new SpawnFrequency(types[j], freq);
                        //System.out.println("Added a spawn freq for type:"+types[j]+"(num_types:"+types.length+") from "+edges[i]);
                        ruTypeCount++;
                    }
                }
                spawn = (SpawnFrequency[]) Arrayutils.cropArray(spawn, ruTypeCount);
                edge.setSpawnFrequencies(spawn);
            }

            SpecialNode[] spNodes = infra.getSpecialNodes();
            int totalEdgeCount = 0;
            float totalEdgeChance = 0;

            // calc the average value of edge nodes (used to predict for net tunnels)
            for (int j = 0; j < numSpecialNodes; j++) {
                for (int k = 0; k < numRuTypes; k++) {
                    if (spNodes[j] instanceof EdgeNode) {
                        EdgeNode edgeNode = (EdgeNode) spNodes[j];
                        totalEdgeChance += Math.max(edgeNode.getSpawnFrequency(ruTypes[k]), 0.0f);
                        totalEdgeCount++;
                    }
                }
            }
            float avgEdgeChance = totalEdgeCount == 0 ? 0 : totalEdgeChance / totalEdgeCount;

            // get the total value for all edge nodes based on the spawn freq
            float edgeChances[] = new float[numRuTypes];
            for (int j = 0; j < numSpecialNodes; j++) {
                for (int k = 0; k < numRuTypes; k++) {
                    if (spNodes[j] instanceof EdgeNode) {
                        EdgeNode edgeNode = (EdgeNode) spNodes[j];
                        edgeChances[k] += Math.max(edgeNode.getSpawnFrequency(ruTypes[k]), 0.0f);
                    } else {
                        edgeChances[k] += avgEdgeChance;
                    }
                }
            }

            // set the edge chance of a destination based on its spawn freq and the total edge dest
            DestFrequency[][] dest = new DestFrequency[numSpecialNodes][];
            for (int j = 0; j < numSpecialNodes; j++) {
                dest[j] = new DestFrequency[numRuTypes];
                float edgeChanceDest = 0;
                for (int k = 0; k < numRuTypes; k++) {
                    if (spNodes[j] instanceof EdgeNode) {
                        EdgeNode edgeNode = (EdgeNode) spNodes[j];
                        edgeChanceDest = Math.max(edgeNode.getSpawnFrequency(ruTypes[k]), 0.0f);
                    } else {
                        edgeChanceDest = avgEdgeChance;
                    }
                    edgeChanceDest = edgeChances[k] == 0 ? 0 : edgeChanceDest / edgeChances[k];
                    dest[j][k] = new DestFrequency(ruTypes[k], edgeChanceDest);
                }
            }
            edge.setDestFrequencies(dest);

        }
        return errors;
    }

    private ArrayList<String> addIDs() throws InfraException {
        ArrayList<String> errors = new ArrayList<>();

        int nodeIDCount = 0;
        int roadID = 0;
        int laneID = 0;

        Node[] allNodes = infra.getAllNodes();
        int num_nodes = allNodes.length;

        // Setting all the NodeIDs
        for (int i = 0; i < num_nodes; i++) {
            Node node = allNodes[i];
            node.setId(nodeIDCount++);
        }

        // Setting all the RoadIDs
        for (int i = 0; i < num_nodes; i++) {
            Node node = allNodes[i];
            Road[] alphaRoads = node.getAlphaRoads();
            int num_aroads = alphaRoads.length;

            for (int j = 0; j < num_aroads; j++) {
                Road road = alphaRoads[j];
                road.setId(roadID++);
            }
        }

        // Setting all the SignIDs
        for (int i = 0; i < num_nodes; i++) {
            Node node = allNodes[i];
            DriveLane[] inboundlanes = node.getInboundLanes();
            int num_ibl = inboundlanes.length;

            for (int j = 0; j < num_ibl; j++) {
                DriveLane lane = inboundlanes[j];
                lane.setId(laneID++);

                //als het goed is, is een sign nu nooit meer null
                if (lane.getSign() == null) {
                    errors.add("ERROR: lane " + lane.getId()
                            + " has sign == null");
                }
            }
        }

        return errors;
    }

    // Check for each type wheter the turns are correct or not
    private ArrayList<String> turnJustification() throws InfraException {
        ArrayList<String> errors = new ArrayList<>();
        Node[] nodes = infra.getAllNodes();
        int num_nodes = nodes.length;

        //System.out.println(":: Turn Justification:");
        for (int i = 0; i < num_nodes; i++) {
            Node n = nodes[i];
            Road[] nroads = n.getAlphaRoads();
            int num_nroads = nroads.length;

            for (int j = 0; j < num_nroads; j++) {
                Road road = nroads[j];
                DriveLane[] dlanes = road.getAllLanes();
                int num_lanes = dlanes.length;

                for (int k = 0; k < num_lanes; k++) {
                    Node danode = dlanes[k].getNodeLeadsTo();
                    Road droad = dlanes[k].getRoad();
                    Road[] dnroads = danode.getAllRoads();
                    int road_index = -1;
                    for (int r = 0; r < dnroads.length; r++) {
                        if (dnroads[r] == droad) {
                            road_index = r;
                        }
                    }

                    boolean[] targets = dlanes[k].getTargets();
                    boolean[] dtargets = new boolean[3];
                    dtargets[0] = targets[0];
                    dtargets[1] = targets[1];
                    dtargets[2] = targets[2];
                    int check_index;
                    int num_turns = 0;

                    if (danode instanceof Junction) {
                        String tg1 = dtargets[0]
                                ? danode.getAllRoads()[(road_index + 1) % 4]
                                + "" : "-";
                        String tg2 = dtargets[1]
                                ? danode.getAllRoads()[(road_index + 2) % 4]
                                + "" : "-";
                        String tg3 = dtargets[2]
                                ? danode.getAllRoads()[(road_index + 3) % 4]
                                + "" : "-";

                        //System.out.println("DriveLane towards Node:"+danode.getId()+" at pos "+road_index+" orig targets ("+dtargets[0]+","+dtargets[1]+","+dtargets[2]+") ("+tg1+","+tg2+","+tg3+")");
                        if (dtargets[0]) {
                            num_turns++;
                            check_index = (road_index + 1) % 4;
                            //System.out.println("Checking turn to nodepos:"+check_index);
                            if (dnroads[check_index] == null) {
                                dtargets[0] = false;
                                num_turns--;
                            }
                        }
                        if (dtargets[1]) {
                            num_turns++;
                            check_index = (road_index + 2) % 4;
                            //System.out.println("Checking turn to nodepos:"+check_index);
                            if (dnroads[check_index] == null) {
                                dtargets[1] = false;
                                num_turns--;
                            }
                        }
                        if (targets[2]) {
                            num_turns++;
                            check_index = (road_index + 3) % 4;
                            //System.out.println("Checking turn to nodepos:"+check_index);
                            if (dnroads[check_index] == null) {
                                dtargets[2] = false;
                                num_turns--;
                            }
                        }

                        //System.out.println("DriveLane towards Node:"+danode.getId()+" at pos "+road_index+"  new targets ("+dtargets[0]+","+dtargets[1]+","+dtargets[2]+") "+num_turns);
                        if (num_turns == 0) {
                            //System.out.println("No turns left, trying to find at least one possible turn for this lane.");
                            int check_left = (road_index + 1) % 4;
                            int check_straight = (road_index + 2) % 4;
                            int check_right = (road_index + 3) % 4;
                            if (dnroads[check_left] != null) {
                                //System.out.println("Possible left turn");
                                dtargets[0] = true;
                            } else if (dnroads[check_straight] != null) {
                                //System.out.println("Possible straight");
                                dtargets[1] = true;
                            } else if (dnroads[check_right] != null) {
                                //System.out.println("Possible right turn");
                                dtargets[2] = true;
                            }
                        }
                    } else {
                        //System.out.println("DriveLane towards EdgeNode:"+danode.getId()+" at pos "+road_index+" orig targets ("+dtargets[0]+","+dtargets[1]+","+dtargets[2]+")");
                        dtargets[0] = false;
                        dtargets[1] = true;
                        dtargets[2] = false;
                        //System.out.println("DriveLane towards EdgeNode:"+danode.getId()+" at pos "+road_index+"  new targets ("+dtargets[0]+","+dtargets[1]+","+dtargets[2]+")");
                    }
                    dlanes[k].setTargets(dtargets);
                }
            }
        }
        return errors;
    }

    private int getRUTypes(SpecialNode mn) throws InfraException {
        int type = 0;
        DriveLane[] dls = mn.getOutboundLanes();
        for (int i = 0; i < dls.length; i++) {
            type |= dls[i].getType();
        }
        return type;
    }

    private ArrayList<String> checkConnectedType(int type) throws InfraException {
        ArrayList<String> errors = new ArrayList<>();

        SpecialNode en1, en2;
        SpecialNode[] specialNodes = infra.getSpecialNodes();
        for (int i = 0; i < specialNodes.length; i++) {
            en1 = specialNodes[i];

            if ((getRUTypes(en1) & type) != 0) {
                for (int j = i + 1; j < specialNodes.length; j++) {
                    en2 = specialNodes[j];
                    if ((getRUTypes(en2) & type) != 0) {
                        DriveLane[] dl = en1.getShortestPaths(en2.getId(), type);
                        if (dl.length == 0) {
                            errors.add("ERROR: specialNode " + en1.getId()
                                    + " is not connected to specialNode "
                                    + en2.getId() + " in type "
                                    + RoaduserFactory.getDescByType(
                                            type));
                        }
                    }
                }
            }

        }
        return errors;
    }

    private ArrayList<String> checkConnected() throws InfraException {
        ArrayList<String> errors = new ArrayList<>();

        // Check for every type wheter it is connected or not.
        int type = 1;
        int[] concreteTypes = RoaduserFactory.getConcreteTypes();

        // Should be way different than this.. not sure how.
        for (int i = 0; i < concreteTypes.length; i++) {
            errors.addAll(checkConnectedType(type));
            type *= 2;
        }

        return errors;
    }

    private ArrayList<String> checkInfra() throws InfraException {
        ArrayList<String> errors = new ArrayList<>();
        if (infra.getNumSpecialNodes() < 2) {
            errors.add(
                    "ERROR: infrastructure should contain at least 2 special nodes.");
        }

        Node[] allNodes = infra.getAllNodes();
        for (int i = 0; i < allNodes.length; i++) {
            Node nd = allNodes[i];
            if (nd.getNumAllLanes() == 0) {
                errors.add("ERROR: Node " + nd.getId()
                        + " doesn't have any road connected to it.");
            }
        }

        return errors;
    }

    private void addSCData() throws InfraException {
        // Create the tool
        SignConfigCalculator sc = new SignConfigCalculator();

        // get all nodes
        Node[] allNodes = infra.getAllNodes();
        Sign[][] result;

        // calculate the SC's for each node
        for (int i = 0; i < allNodes.length; i++) {
            // .getType()==Node.TL faster? nicer?
            if (allNodes[i] instanceof Junction) {
                result = sc.calcSC(allNodes[i]);

                ((Junction) allNodes[i]).setSignConfigs(result);
            }
        }
    }

    private void addSPData() throws InfraException {
        Node[] nodes = infra.getAllNodes();
        int num_nodes = nodes.length;
        for (int i = 0; i < num_nodes; i++) {
            nodes[i].zapShortestPaths();
        }
        ShortestPathCalculator sp = new ShortestPathCalculator();
        sp.calcAllShortestPaths(infra);
    }

    protected SpawnFrequency[] spawnFreq
            = {new SpawnFrequency(0, 0.0f),
                new SpawnFrequency(1, 0.0f)};

    private ArrayList<String> checkSpawnFreqs() throws InfraException {
        ArrayList<String> errors = new ArrayList<>();

        EdgeNode[] edgnds = infra.getEdgeNodes();
        for (int i = 0; i < edgnds.length; i++) {
            EdgeNode en = edgnds[i];
            SpawnFrequency[] spfreq = en.getSpawnFrequencies();
            for (int j = 0; j < spfreq.length; j++) {
                if (spfreq[j].freq > 1) {
                    errors.add("ERROR: Edge-Node " + en.getId()
                            + " Spawn-frequency >1 ");
                }
                if (spfreq[j].freq < 0) {
                    errors.add("ERROR: Edge-Node " + en.getId()
                            + " Spawn-frequency <0 ");
                }
            }
        }
        return errors;
    }

}
