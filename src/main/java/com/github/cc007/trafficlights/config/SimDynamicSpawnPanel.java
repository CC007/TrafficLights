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

package com.github.cc007.trafficlights.config;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.edit.*;
import com.github.cc007.trafficlights.utils.*;


/**
 *
 * @author Group GUI
 * @version 1.0
 */

public class SimDynamicSpawnPanel extends ConfigPanel implements ActionListener, ItemListener
{
        EdgeNode eNode;

        List[] ruList;
        int numItems = 0;
        Choice spawnTypes;

        TextField spawnFreq;
        TextField cycleInput;

        Button setSpawn;
        Button deleteSpawn;

        Vector[] ruCyclesLists;

        int deleteType = -1;
        int deleteCycle = -1;



        public SimDynamicSpawnPanel(ConfigDialog cd, EdgeNode e)
        {
                super(cd);
                eNode = e;

                String[] descs = RoaduserFactory.getConcreteTypeDescs();
                numItems = descs.length;
                ruList = new List[numItems];

                for (int i=0; i < numItems; i++)
                {
                  int pos = 0;
                  if (i % 2 != 0) pos = 200;
                  Label lab = new Label("Cycles list for " + descs[i]);
                  lab.setBounds(pos, ((int)i / 2) * 70, 200, 20);
                  add(lab);

                  ruList[i] = new List();
                  ruList[i].setBounds(pos, ((int)i / 2) * 70 + 25, 150, 40);
                  ruList[i].addItemListener(this);
                  add(ruList[i]);

                }

                int vpos = 70 * ((int) Math.ceil((double)numItems/ 2));

                Label lab = new Label("Set Dynamic spawnfrequency for");
                lab.setBounds(0, vpos + 10, 200, 20);
                add(lab);

                spawnTypes = new Choice();
                spawnTypes.addItemListener(this);


                for (int i=0; i < descs.length; i++)
                        spawnTypes.addItem(descs[i]);

                spawnTypes.setBounds(0, vpos + 35, 100, 20);
                add(spawnTypes);

                lab = new Label("on cycle");
                lab.setBounds(105, vpos + 35, 50, 20);
                add(lab);

                cycleInput = new TextField();
                cycleInput.setBounds(160, vpos + 35, 60, 20);
                cycleInput.addActionListener(this);
                add(cycleInput);

                lab = new Label("is");
                lab.setBounds(225, vpos + 35, 20, 20);
                add(lab);

                spawnFreq = new TextField();
                spawnFreq.setBounds(250, vpos + 35, 40, 20);
                spawnFreq.addActionListener(this);
                add(spawnFreq);

                setSpawn = new Button("Add");
                setSpawn.addActionListener(this);
                setSpawn.setBounds(295, vpos + 35, 50, 20);
                add(setSpawn);

                deleteSpawn = new Button("Delete cycle 0 from type " + descs[0]);
                deleteSpawn.setVisible(false);
                deleteSpawn.addActionListener(this);
                deleteSpawn.setBounds(0,vpos + 70, 250,25);
                add(deleteSpawn);
                reset();

        }

        public void paint(Graphics g) {
                super.paint(g);
                g.setColor(Color.black);
                int vpos = 70 * ((int) Math.ceil((double)numItems/ 2));
                g.drawLine(0, vpos, ConfigDialog.PANEL_WIDTH, vpos);
        }






        public int getSpawnType() {
                int[] types = RoaduserFactory.getConcreteTypes();
                return types[spawnTypes.getSelectedIndex()];
        }


        public void reset() {
               int [] types = RoaduserFactory.getConcreteTypes();
               ruCyclesLists = new Vector[types.length];
               boolean containsAnyItem = false;
               for (int i = 0; i < types.length; i++) {
                   ruList[i].removeAll();
                   Vector dSpawnList = eNode.dSpawnCyclesForRu(types[i]);
                   for (int j = 0; j < dSpawnList.size(); j++) {
                     SpawnFrequencyCycles sf = (SpawnFrequencyCycles)dSpawnList.get(j);
                     ruList[i].add(sf.toString());
                     containsAnyItem = true;
                   }
                   ruCyclesLists[i] = dSpawnList;
               }
               if (containsAnyItem == false)
               {
                 deleteSpawn.setVisible(false);
               }

        }

        public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();

                if (source == setSpawn)
                {
                   int cy;
                   try {
                       cy = Integer.parseInt(cycleInput.getText());
                       try {
                         float fr = Float.parseFloat(spawnFreq.getText());
                         eNode.addDSpawnCycles(getSpawnType(), cy, fr);
                       }
                       catch (NumberFormatException ex) {
                         confd.showError("You must enter a float in the Spawn frequencies box.");
                       }
                   }
                   catch (NumberFormatException ex) {
                       confd.showError("You must enter an Integer in the Cycles box.");
                   }
                }
                else if (source == deleteSpawn)
                {
                   SpawnFrequencyCycles sf = (SpawnFrequencyCycles)ruCyclesLists[deleteType].get(deleteCycle);
                   eNode.deleteDSpawnCycles(sf.ruType, sf.cycle);
                }
                reset();
        }

        public void itemStateChanged(ItemEvent e) {
                ItemSelectable es = e.getItemSelectable();

                for (int i = 0; i < ruList.length; i++) {
                      if (es == ruList[i])
                      {
                         if(deleteType > -1 && deleteCycle > -1)
                            ruList[deleteType].deselect(deleteCycle);
                         deleteType = i;
                         deleteCycle = ruList[i].getSelectedIndex();
                         if (deleteCycle > -1)
                         {
                           int cycle = ( (SpawnFrequencyCycles) ruCyclesLists[deleteType].get(
                               deleteCycle)).cycle;
                           String[] descs = RoaduserFactory.getConcreteTypeDescs();
                           deleteSpawn.setLabel("Delete spawn at cycle " + cycle + " for type " +
                                                descs[deleteType]);
                           deleteSpawn.setVisible(true);
                         }
                         else
                           deleteSpawn.setVisible(false);
                      }
                }

        }
}
