
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
package com.github.cc007.trafficlights.tools;

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.edit.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.sim.stats.TrackingController;

import java.awt.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This implements create road user action
 *
 * @author Group GUI
 * @version 1.0
 */
public class RoadAction implements ToolAction {

    protected EditModel model;

    protected Node alphaNode = null;
    protected Point startPoint = null;
    protected Point mousePoint = null;
    protected Point alphaPoint = null;
    protected ArrayList<Point> turnPoints = null;
    protected int alphaConPos = -1, betaConPos = -1;

    protected static final int MIN_TURN_DISTANCE = 50;

    public RoadAction(EditModel em) {
        model = em;
        turnPoints = new ArrayList<>();
    }

    @Override
    public boolean beingUsed() {
        return alphaNode != null;
    }

    public void reset() {
        alphaNode = null;
        startPoint = null;
        mousePoint = null;
        alphaPoint = null;
        turnPoints.clear();
        alphaConPos = -1;
        betaConPos = -1;
    }

    /**
     * Starts creating a road at given point
     *
     * @param view The main view. Needed to convert coordinates
     * @param p The point to start the new road at
     * @return False if no node was found at the given point (and the action
     * could not be started consequently)
     */
    public boolean startAction(View view, Point p) {
        Class[] sf = {Node.class};
        Node clicked = (Node) Selection.selectObject(sf, model.getInfrastructure(), p);

        if (clicked != null) {
            alphaNode = clicked;
            startPoint = alphaPoint = alphaNode.getCoord();
            mousePoint = p;
            return true;
        }
        return false;
    }

    /**
     * Performs a step in creating a road at given point. If a node was found at
     * the given point, the road is created and the action ends. Otherwise a
     * turn is added using the given point.
     *
     * @param p The point
     * @return False if the action was ended without creating a road.
     */
    public boolean nextAction(View view, Point p) {
        Class[] sf = {Node.class};
        Node clicked = (Node) Selection.selectObject(sf, model.getInfrastructure(), p);

        try {

            if (clicked == alphaNode) {
                return true; // click the alpha node again does not end the action
            }
            if (clicked == null) {

                // Add a turn
                Point prevp = alphaPoint;
                if (!turnPoints.isEmpty()) {
                    prevp = (Point) turnPoints.get(turnPoints.size() - 1);
                }
                if (prevp.distance(p) < MIN_TURN_DISTANCE) {
                    return true; // new turn to close to previous
                }
                if (alphaConPos == -1) {
                    try {
                        alphaConPos = getBestConPosition(alphaNode, alphaPoint, p);
                    } catch (CannotConnectException e) {
                        Logger.getLogger(RoadAction.class.getName()).log(Level.SEVERE, null, e);
                        reset();
                        return false;
                    }
                }

                turnPoints.add(p);
            } else {

                // Connect road and end action
                Node betaNode = clicked;
                Point betaPoint = betaNode.getCoord();

                try {
                    if (alphaConPos == -1) {
                        alphaConPos = getBestConPosition(alphaNode, alphaPoint, betaPoint);
                    }
                    Point cp = alphaPoint;
                    if (!turnPoints.isEmpty()) {
                        cp = (Point) turnPoints.get(turnPoints.size() - 1);
                    }
                    betaConPos = getBestConPosition(betaNode, betaPoint, cp);
                } catch (CannotConnectException e) {
                    Logger.getLogger(RoadAction.class.getName()).log(Level.SEVERE, null, e);
                    reset();
                    return false;
                }

                model.addRoad(createRoad(alphaNode, betaNode), alphaNode, alphaConPos, betaNode, betaConPos, turnPoints);

                reset();
            }
        } catch (InfraException e) {
            Logger.getLogger(RoadAction.class.getName()).log(Level.SEVERE, null, e);
            reset();
            Controller.reportError(e);
            return false;
        }
        return true;
    }

    /**
     * Moves the current mouse point
     *
     * @param p The new position of the mouse cursor
     */
    public void moveAction(View view, Point p) {
        mousePoint = p;
    }

    /**
     * Paints a graphical representation of the status of this action
     *
     * @param g The Graphics object to paint on
     */
    public void paint(Graphics g) {
        g.setXORMode(Color.darkGray);
        g.setColor(Color.lightGray);
        Point p1 = alphaPoint;
        Point p2 = mousePoint;
        for (int i = 0; i < turnPoints.size(); i++) {
            p2 = (Point) turnPoints.get(i);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
            p1 = p2;
        }
        g.drawLine(p1.x, p1.y, mousePoint.x, mousePoint.y);
    }

    protected int getBestConPosition(Node node, Point pa, Point pb)
            throws CannotConnectException, InfraException {
        // TODO: add old RoadTool code to allow more flexible road connecting

        int dx = pb.x - pa.x;
        int dy = pb.y - pa.y;
        int adx = Math.abs(dx);
        int ady = Math.abs(dy);
        int pos = -1;

        if (dx < 0 && ady <= adx) {
            pos = 3;
        }
        if (dx >= 0 && ady <= adx) {
            pos = 1;
        }
        if (dy < 0 && adx < ady) {
            pos = 0;
        }
        if (dy >= 0 && adx < ady) {
            pos = 2;
        }

        if (node.isConnectionPosFree(pos)) {
            return pos;
        }
        throw new CannotConnectException("Cannot connect to: " + pos);
    }

    protected Road createRoad(Node alpha, Node beta) throws InfraException {
        int dx = alpha.getCoord().x - beta.getCoord().x;
        int dy = alpha.getCoord().y - beta.getCoord().y;

        Road road = new Road(alpha, beta, (int) (Math.sqrt(dx * dx + dy * dy) / Infrastructure.blockLength));
        int ruType = RoaduserFactory.getTypeByDesc("Automobiles");

        DriveLane lane0 = new DriveLane(road);
        lane0.setType(ruType);
        lane0.setTarget(0, true);
        lane0.setTarget(1, false);
        lane0.setTarget(2, false);
        road.addLane(lane0, alpha);

        DriveLane lane1 = new DriveLane(road);
        lane1.setType(ruType);
        lane1.setTarget(0, false);
        lane1.setTarget(1, true);
        lane1.setTarget(2, true);
        road.addLane(lane1, alpha);

        DriveLane lane2 = new DriveLane(road);
        lane2.setType(ruType);
        lane2.setTarget(0, true);
        lane2.setTarget(1, false);
        lane2.setTarget(2, false);
        road.addLane(lane2, beta);

        DriveLane lane3 = new DriveLane(road);
        lane3.setType(ruType);
        lane3.setTarget(0, false);
        lane3.setTarget(1, true);
        lane3.setTarget(2, true);
        road.addLane(lane3, beta);

        return road;
    }
}
