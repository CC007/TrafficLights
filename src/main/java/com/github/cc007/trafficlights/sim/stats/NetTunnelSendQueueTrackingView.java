
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

import com.github.cc007.trafficlights.infra.NetTunnel;

/**
 *
 * TrackingView that tracks the send queue length of one NetTunnel
 *
 * @author  Group GUI
 * @version 1.0
 */

public class NetTunnelSendQueueTrackingView extends TrackingView
{	NetTunnel node;	
	
  public NetTunnelSendQueueTrackingView(int startCycle, NetTunnel tunnel)
  {	super(startCycle);
		node=tunnel;
  }

	/** Returns the next sample to be 'tracked'. */
@Override
	protected float nextSample(int src) 
	{ 	return node.getSendQueueLength();
	}
	
	/** Returns the description for this tracking window. */
@Override
	public String getDescription() { return "NetTunnel " + node.getId() + 
		" - send queue length"; }
	
@Override
	protected String getSourceDesc(int src) { return "length"; }
@Override
	protected String getYLabel() { return "send queue length (roadusers)"; }
}
