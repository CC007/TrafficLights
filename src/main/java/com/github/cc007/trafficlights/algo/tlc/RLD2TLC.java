
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


import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.xml.*;

/**
 * Red Light District 2 TLC...
 *
 * You're in a hurry? You better fear Red Light District 2. 
 * This TLC is specially intended for the non-time-pressed and relaxed roaduser.
 *
 * @author Group Algorithms
 * @version 1.0
 */

public class RLD2TLC extends TLController
{	
	protected int num_nodes;
	protected final static String shortXMLName="tlc-rld2";
	
	/**
	 * The constructor for TL controllers
	 * @param The model being used.
	 */
	public RLD2TLC(Infrastructure i) {
		super(i);
		num_nodes = tld.length;
	}
	
    @Override
	public void setInfrastructure(Infrastructure i) 
	{ 	super.setInfrastructure(i); 
		num_nodes = tld.length;
	}

	/**
	 * This implementation sets the Q-values according to the length
	 * of the waiting queue. The longer the queue, the higher the Q-value.
	 */	

    @Override
	public TLDecision[][] decideTLs()
	{
		for (int i=0; i < tld.length; i++) {
			for(int j=0; j < tld[i].length; j++) {
				tld[i][j].setGain(-tld[i][j].getTL().getLane().getNumRoadusersWaiting());
			}
		}
		return tld;
	}

    @Override
	public void updateRoaduserMove(Roaduser _ru, DriveLane _prevlane, Sign _prevsign, int _prevpos, DriveLane _dlanenow, Sign _signnow, int _posnow, PosMov[] posMovs, DriveLane desired)
	{   // No needed
	}
	
	// XMLSerializable implementation
    @Override
	public XMLElement saveSelf () throws XMLCannotSaveException
	{	XMLElement result=super.saveSelf();
		result.setName(shortXMLName);
		return result;
	}

    @Override
 	public String getXMLName ()
	{ 	return "model."+shortXMLName;
	}
}

