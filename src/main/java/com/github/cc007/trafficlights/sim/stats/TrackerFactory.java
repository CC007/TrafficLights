
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

import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.sim.SimModel;
import com.github.cc007.trafficlights.sim.SimController;
import com.github.cc007.trafficlights.GLDException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * TrackerFactory shows a TrackingController with a TrackingView of a given type.
 *
 * @author Group GUI
 * @version 1.0
 */

public class TrackerFactory
{
	/** Show the average total trip waiting time tracking window. */
	public final static int TOTAL_WAIT = 0;
	/** Show the average junction waiting time tracking window. */
	public final static int TOTAL_JUNCTION = 1;
	/** Show the total waiting queue length tracking window. */
	public final static int TOTAL_QUEUE = 2;
	/** Show the total roadusers tracking window. */
	public final static int TOTAL_ROADUSERS = 3;
	/** Show the single SpecialNode average trip waiting time tracking window. */
	public final static int SPECIAL_WAIT = 4;
	/** Show the single EdgeNode waiting queue length tracking window. */
	public final static int SPECIAL_QUEUE = 5;
	/** Show the single SpecialNode roadusers tracking window. */
	public final static int SPECIAL_ROADUSERS = 6;
	/** Show the single Junction average junction waiting time tracking window. */
	public final static int JUNCTION_WAIT = 7;
	/** Show the single Junction roadusers tracking window. */
	public final static int JUNCTION_ROADUSERS = 8;
	/** Show the single NetTunnel send queue tracking window */
	public final static int NETTUNNEL_SEND = 9;
	/** Show the single NetTunnel receive queue tracking window */
	public final static int NETTUNNEL_RECEIVE = 10;

	/** Show the number of accidents (DOAS 06) */
	public final static int ACCIDENTS_COUNT = 11;
	/** Show the number of accidents (DOAS 06) */
	public final static int REMOVEDCARS_COUNT = 12;

	
	protected static ArrayList<TrackingController> trackingControllers = new ArrayList<>();

	/**
	* Shows one of the 'global' tracking windows.
	* @param type One of the 'TOTAL_' constants.
	*/
 	public static TrackingController showTracker(SimModel model, SimController controller, int type) throws GLDException
	{
		if(type == TOTAL_QUEUE) {
			TrackingView view = new AllQueuesTrackingView(model.getCurCycle(), model);
			return genTracker(model, controller, view);
		}

		ExtendedTrackingView view = null;
        switch (type) {
            case TOTAL_JUNCTION:
                view = new AllJunctionsTrackingView(model.getCurCycle(), model);
                break;
            case TOTAL_WAIT:
                view = new TotalWaitTrackingView(model.getCurCycle(), model);
                break;
            case TOTAL_ROADUSERS:
                view = new TotalRoadusersTrackingView(model.getCurCycle(), model);
                break;
            case ACCIDENTS_COUNT:
                view = new AccidentsCountTrackingView(model.getCurCycle(), model);
                break;
            case REMOVEDCARS_COUNT:
                view = new RemovedCarsTrackingView(model.getCurCycle(), model);
                break;
            default:
                break;
        }


		if(view == null) {
            throw new GLDException("Invalid tracker type!");
        }
		return genExtTracker(model, controller, view);
	}

	/**
	* Shows one of the EdgeNode tracking windows.
	* @param type One of the 'EDGE_' constants.
	*/
 	public static TrackingController showTracker(SimModel model, SimController	controller, SpecialNode node, int type) throws GLDException
	{	if(type == SPECIAL_QUEUE && node instanceof SpecialNode) {
			TrackingView view = new	SpecialNodeQueueTrackingView(model.getCurCycle(),(SpecialNode)node);
			return genTracker(model, controller, view);
		}
		else if (type==NETTUNNEL_SEND && node instanceof NetTunnel )
		{	TrackingView view = new	NetTunnelSendQueueTrackingView(model.getCurCycle(),(NetTunnel)node);
			return genTracker(model, controller, view);
		}
		ExtendedTrackingView view = null;
		if(type == SPECIAL_WAIT) {
            view = new SpecialNodeWaitTrackingView(model.getCurCycle(), node);
    } else if(type == SPECIAL_ROADUSERS) {
        view = new NodeRoadusersTrackingView(model.getCurCycle(), node);
    }

		if(view == null) {
            throw new GLDException("Invalid tracker type!");
    }
		return genExtTracker(model, controller, view);
	}

	/**
	* Shows the specified Junction tracking window.
	*/
 	public static TrackingController showTracker(SimModel model, SimController controller, Junction junction, int type) throws GLDException
	{
		ExtendedTrackingView view = null;
		if(type == JUNCTION_WAIT) {
            view = new JunctionWaitTrackingView(model.getCurCycle(), junction);
        } else if(type == JUNCTION_ROADUSERS) {
            view = new NodeRoadusersTrackingView(model.getCurCycle(), junction);
        }

		if(view == null) {
            throw new GLDException("Invalid tracker type!");
        }
		return genExtTracker(model, controller, view);
	}


	protected static TrackingController genTracker(SimModel model, SimController controller, TrackingView view)
	{
		TrackingController tc = new TrackingController(model, controller, view);
		trackingControllers.add(tc);
		return tc;
	}

	protected static TrackingController genExtTracker(SimModel model, SimController controller, ExtendedTrackingView view)
	{
		TrackingController tc = new ExtendedTrackingController(model, controller, view);
		trackingControllers.add(tc);
		return tc;
	}

	public static void purgeTrackers()
	{
		Iterator it = trackingControllers.iterator();
		while(it.hasNext()) {
            ((TrackingController)it.next()).closeWindow();
        }
	}

	public static void disableTrackerViews()
	{
		Iterator it = trackingControllers.iterator();
		while(it.hasNext()) {
            ((TrackingController)it.next()).setViewEnabled(false);
        }
	}

	public static void resetTrackers()
	{
		Iterator it = trackingControllers.iterator();
		while(it.hasNext()) {
            ((TrackingController)it.next()).reset();
        }
	}

	public static TrackingController[] getTrackingControllers()
	{
		TrackingController[] tca = new TrackingController[trackingControllers.size()];
		Iterator it = trackingControllers.iterator();
		int i=0;
		while(it.hasNext()) {
			tca[i] = ((TrackingController)it.next());
			i++;
		}
		return tca;
	}
}
