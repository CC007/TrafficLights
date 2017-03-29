
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

package com.github.cc007.trafficlights.sim.stats;

import com.github.cc007.trafficlights.infra.Node;
import com.github.cc007.trafficlights.infra.EdgeNode;
import com.github.cc007.trafficlights.infra.Node.NodeStatistics;

/**
 *
 * TrackingView that tracks total roadusers arrived at a certain node.
 *
 * @author  Group GUI
 * @version 1.0
 */

public class NodeRoadusersTrackingView extends ExtendedTrackingView
{
	NodeStatistics[] stats;
	String desc;
	
  public NodeRoadusersTrackingView(int startCycle, Node node)
  {
		super(startCycle);
		stats = node.getStatistics();
		desc = "node " + node.getId() + " - roadusers ";
		if(node instanceof EdgeNode) {
            desc += "arrived";
        } else {
            desc += "crossed";
        }
  }

	/** Returns the next sample to be 'tracked'. */
    @Override
	protected float nextSample(int src) 
	{ 
		return stats[src].getTotalRoadusers();
	}
	
	/** Returns the description for this tracking window. */
    @Override
	public String getDescription() { return desc; }
	
    @Override
	protected String getYLabel() { return "(roadusers)"; }
	
    @Override
	public boolean useModes() { return false; }
}