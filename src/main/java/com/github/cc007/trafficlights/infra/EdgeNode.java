
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

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.sim.SimModel;
import com.github.cc007.trafficlights.utils.*;
import com.github.cc007.trafficlights.xml.*;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;
import java.io.IOException;
import java.util.*;
import java.util.Vector;
import java.util.SortedSet;

/**
 *
 * EdgeNode, a node used as starting and end point for Roadusers.
 *
 * @author Group Datastructures
 * @version 1.0
 */

public class EdgeNode extends SpecialNode
{
	/** The type of this node */
	protected static final int type = Node.EDGE;
	/** The frequency at which various roadusers spawn */
	protected SpawnFrequency[] spawnFreq = { };
       /** The frequency at which various roadusers spawn at given cycles.*/
	protected Hashtable spawnCyclesHash = new Hashtable();


	/** The frequency with which spawned roadusers choose specific destinations */
	protected DestFrequency[][] destFreq = {{}};

	public EdgeNode() {}

	public EdgeNode(Point _coord)
	{
		super(_coord);
	}


	/*============================================*/
	/* LOAD and SAVE                              */
	/*============================================*/

	public void load (XMLElement myElement,XMLLoader loader) throws XMLTreeException,IOException,XMLInvalidInputException
	{	super.load(myElement,loader);
		spawnFreq=(SpawnFrequency[])XMLArray.loadArray(this,loader);
		destFreq=(DestFrequency[][])XMLArray.loadArray(this,loader);

                try
                {
                  SpawnFrequencyCycles[] dSpawnFreq = (SpawnFrequencyCycles[])XMLArray.loadArray(this,loader);
                  spawnCyclesHash = new Hashtable();
                  for (int i = 0; i < dSpawnFreq.length; i++)
                  {
                    Integer key = new Integer(dSpawnFreq[i].cycle);
                    if (spawnCyclesHash.get(key) == null)
                    {
                      spawnCyclesHash.put(key, new Vector( ));
                    }

                    ((Vector)spawnCyclesHash.get(key)).add((Object)dSpawnFreq[i]);

                  }
                }
                catch (Exception e)
                {
                  System.out.println(e.getMessage()+ "\n Due to new XML entry, safe to ignore the first time when loading older files.");
                }


	}

	public XMLElement saveSelf () throws XMLCannotSaveException {
		XMLElement result=super.saveSelf();
		result.setName("node-edge");
		return result;
	}

	public void saveChilds (XMLSaver saver) throws XMLTreeException,IOException,XMLCannotSaveException
	{	super.saveChilds(saver);

                Vector temp = new Vector();


                for (Enumeration e = spawnCyclesHash.keys(); e.hasMoreElements();)
                {
                    Vector hashelem = (Vector)spawnCyclesHash.get(e.nextElement());
                    temp.addAll(hashelem);
                }
                SpawnFrequencyCycles[] dSpawnArray = new SpawnFrequencyCycles[temp.size()];

                for (int j = 0; j < temp.size(); j++) {
                    dSpawnArray[j] = (SpawnFrequencyCycles)temp.get(j);
                }


		XMLArray.saveArray(spawnFreq,this,saver,"spawn-frequencies");
		XMLArray.saveArray(destFreq,this,saver,"dest-frequencies");
		XMLArray.saveArray(dSpawnArray,this,saver,"dspawn-frequencies");
	}

 	public String getXMLName ()
 	{ 	return parentName+".node-edge";
 	}

	class TwoStageLoaderData {
		int roadId;
	}

        public void addDSpawnCycles (int _rutype, int _cycle, float _freq)
        {

           SpawnFrequencyCycles sf = new SpawnFrequencyCycles(_rutype, _cycle, _freq);
           Integer key = new Integer(_cycle);
           if (spawnCyclesHash.get(key) == null)
           {
             spawnCyclesHash.put(key, new Vector( ));
           }

           ((Vector)spawnCyclesHash.get(key)).add((Object)sf);


        }
        public void deleteDSpawnCycles (int _rutype, int _cycle)
        {
           Vector cyvec = (Vector)spawnCyclesHash.get(new Integer(_cycle));
           for (int i = 0; i < cyvec.size(); i++)
           {
             SpawnFrequencyCycles elem = (SpawnFrequencyCycles)cyvec.get(i);
             if (elem.ruType == _rutype)
               cyvec.remove(i);

           }
        }

        public Vector dSpawnCyclesForRu(int _rutype)
        {
           Vector dSpawnVec = new Vector();
           for (Enumeration e = spawnCyclesHash.keys(); e.hasMoreElements();)
           {
              Vector hashelem = (Vector)spawnCyclesHash.get(e.nextElement());
              for (int i = 0; i < hashelem.size(); i++)
              {
                 SpawnFrequencyCycles sf = (SpawnFrequencyCycles)hashelem.get(i);
                 if (sf.ruType == _rutype)
                 {
                   dSpawnVec.add((Object)sf);
                 }
              }
           }

           return dSpawnVec;
        }

        public void doStep (SimModel model)
        {
           Integer curCycle = new Integer(model.getCurCycle());
           if(spawnCyclesHash.containsKey(curCycle))
           {
              Vector sfcsCurCycle = (Vector)spawnCyclesHash.get(curCycle);
              for (int i = 0; i < sfcsCurCycle.size(); i++)
              {
                 SpawnFrequencyCycles sfcs = (SpawnFrequencyCycles)sfcsCurCycle.get(i);
                 setSpawnFrequency(sfcs.ruType, sfcs.freq);
                 System.out.println("Cycle: "+ sfcs.cycle + " Changed SpawnFrequency for " + getName() + " for type " + sfcs.ruType +" to: " + sfcs.freq  );
              }
           }

        }















	/*============================================*/
	/* Basic GET and SET methods                  */
	/*============================================*/

	/** Returns the type of this node */
	public int getType() { return type; }

	/** Returns the name of this edgenode. */
	public String getName() { return "Edgenode " + nodeId; }

	/** Returns the array of Spawning Frequenties */
	public SpawnFrequency[] getSpawnFrequencies() { return spawnFreq; }
	/** Sets the Spawning Frequencies */
	public void setSpawnFrequencies(SpawnFrequency[] spawns) { spawnFreq = spawns; }

	/** Returns the array of arrays of Destination Frequenties */
	public DestFrequency[][] getDestFrequencies() { return destFreq; }
	/** Sets the Destination Frequencies */
	public void setDestFrequencies(DestFrequency[][] dests) { destFreq = dests; }

	/** Returns the spawn freqeuncy for the Roadusers of type ruType */
	public float getSpawnFrequency(int ruType) {
		for (int i=0; i < spawnFreq.length; i++) {
			if (spawnFreq[i].ruType == ruType)
				return spawnFreq[i].freq;
		}
		return -1;
	}

	/** Sets the spawn frequency for Roadusers of type ruType */
	public void setSpawnFrequency(int ruType, float freq) {
		for (int i=0; i < spawnFreq.length; i++)
			if (spawnFreq[i].ruType == ruType)
				spawnFreq[i].freq=freq;
	}

	/**
	 * Returns the destination frequency for certain destination edgenode and roaduser type.
	 */
	public float getDestFrequency(int edgeId, int ruType)
	{
		for(int i=0; i<destFreq[edgeId].length; i++)
			if(destFreq[edgeId][i].ruType == ruType)
				return destFreq[edgeId][i].freq;
		return -1;
	}

	/**
	 * Sets the destination frequency for certain destination edgenode and roaduser type.
	 */
	public void setDestFrequency(int edgeId, int ruType, float dest)
	{
		for(int i=0; i<destFreq[edgeId].length; i++)
			if(destFreq[edgeId][i].ruType == ruType)
				destFreq[edgeId][i].freq = dest;
	}

	/*============================================*/
	/* Graphics stuff                             */
	/*============================================*/


	public void paint(Graphics g) throws GLDException
	{
		paint(g, 0, 0, 1.0f, 0.0);
	}

	public void paint(Graphics g, int x, int y, float zf) throws GLDException
	{
		paint(g,x,y,zf,0.0);
	}

	public void paint(Graphics g, int x, int y, float zf, double bogus) throws GLDException
	{
		int width = getWidth();
		g.setColor(Color.blue);
		g.drawRect((int)((coord.x + x - 5 * width) * zf), (int)((coord.y + y - 5 * width) * zf), (int)(10 * width * zf), (int)(10 * width * zf));
		if(nodeId != -1)
			g.drawString("" + nodeId,(int)((coord.x + x - 5 * width) * zf) - 10,(int)((coord.y + y - 5 * width) * zf) - 3);
	}
}
