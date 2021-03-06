
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

package com.github.cc007.trafficlights.algo.tlc;

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.xml.*;
import java.util.* ;

/**
 * This controller will switch TrafficLights at random.
 *
 * @author Group Algorithms
 * @version 1.0
 */
public class RandomTLC extends TLController
{	protected int num_nodes;
	protected Random seed;
	protected final static String shortXMLName="tlc-random";

	public RandomTLC(Infrastructure i, Random _seed) {
		super(i);
		seed = _seed;
		num_nodes = tld.length;
	}

	public RandomTLC(Infrastructure infra)
	{ 	super(infra);
	  	seed=new Random(GLDSim.seriesSeed[GLDSim.seriesSeedIndex]);
		num_nodes = tld.length;
	}

@Override
	public void setInfrastructure(Infrastructure i) {
		super.setInfrastructure(i);
		num_nodes = tld.length;
	}

	/**
	 * Calculates how every traffic light should be switched
	 * @param The TLDecision is a tuple consisting of a traffic light and a reward (Q) value, for it to be green
	 * @see gld.algo.tlc.TLDecision
	 */
@Override
	public TLDecision[][] decideTLs()
	{
		//System.out.println("RandomTLC.decideTLs");
		int num_lanes;
		for (int i=0; i < num_nodes; i++) {
			num_lanes = tld[i].length;
			for (int j=0; j < num_lanes; j++)
				tld[i][j].setGain(seed.nextFloat());
		}
		return tld;
	}

@Override
	public void updateRoaduserMove(Roaduser _ru, DriveLane _prevlane, Sign _prevsign, int _prevpos, DriveLane _dlanenow, Sign _signnow, int _posnow, PosMov[] posMovs, DriveLane desired)
	{
	}

	// XMLSerializable implementation

@Override
	public XMLElement saveSelf () throws XMLCannotSaveException
	{ 	XMLElement result=super.saveSelf();
		result.setName(shortXMLName);
		return result;
	}

@Override
	public String getXMLName ()
 	{ 	return "model."+shortXMLName;
 	}

}
