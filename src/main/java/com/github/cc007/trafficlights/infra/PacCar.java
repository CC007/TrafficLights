
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

import java.awt.Color;

/**
 * PacCar, not standard...
 *
 * @author Group Datastructures
 * @version 1.0
 *
 * PacCar, not standard...
 */

public class PacCar extends Car
{
	public PacCar(Node new_startNode, Node new_destNode, int pos) {
		super(new_startNode, new_destNode, pos);
		color = Color.yellow;
		speed = 3;
	}
	
	/** Empty constructor for loading */
	public PacCar() { }
	
    @Override
	public String getName() { return "PacCar"; }

  // Specific XMLSerializable implementation 
    @Override
 	public String getXMLName ()
 	{ 	return parentName+".roaduser-paccar";
 	}
}