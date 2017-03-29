
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

import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Serves as MouseListener and MouseMotionListener for View. Asks Controller for
 * current Tool and invokes a method of this Tool when necessary.
 *
 * Created and set by Controller.
 *
 * @author Group GUI
 * @version 1.0
 */
public class ToolListener implements MouseListener, MouseMotionListener {

    Controller controller;
    View view;

    /**
     * Creates a ToolListener.
     *
     * @param con The Controller maintaining the currently selected Tool.
     * @param con The View.
     */
    public ToolListener(Controller con, View v) {
        controller = con;
        view = v;
    }

    /**
     * Invoked when a mouse button is pressed on the View.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        controller.getCurrentTool().mousePressed(view, view.toInfra(e.getPoint()),
                new Tool.Mask(e.getModifiers()));
    }

    /**
     * Invoked when a mouse button is released on the View.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        controller.getCurrentTool().mouseReleased(view, view.toInfra(e.getPoint()),
                new Tool.Mask(e.getModifiers()));
    }

    /**
     * Invoked when the mouse cursor is moved over the View.
     * @param e
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        try {
            controller.getCurrentTool().mouseMoved(view, view.toInfra(e.getPoint()),
                    new Tool.Mask(e.getModifiers()));
        } catch (Exception exc) {
            Logger.getLogger(ToolListener.class.getName()).log(Level.SEVERE, null, exc);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        controller.getCurrentTool().mouseMoved(view, view.toInfra(e.getPoint()),
                new Tool.Mask(e.getModifiers()));
    }

    /**
     * Empty implementation, required by the MouseListener interface.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Empty implementation, required by the MouseListener interface.
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Empty implementation, required by the MouseListener interface.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }
}
