
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
import com.github.cc007.trafficlights.config.SimEdgeNodePanel;

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An Automobile can say toot(), that is what distuingishes it from other
 * Roadusers.
 *
 * @author Group Datastructures
 * @version 1.0
 */
public abstract class Automobile extends Roaduser {

    protected static final int type = RoaduserFactory.getTypeByDesc("Automobiles");
    protected static final int speed = 2;
    protected static final int passengers = 1;
    protected static final String soundFileName = "gld/infra/carhorn.wav";

    public Automobile(Node new_startNode, Node new_destNode, int pos) {
        super(new_startNode, new_destNode, pos);
    }

    public Automobile() { // Empty constructor for loading
    }

    @Override
    public void paint(Graphics g, int x, int y, float zf) {
    }

    @Override
    public void paint(Graphics g, int x, int y, float zf, double angle) {
        g.setColor(Color.black);
        g.fillRect((int) ((x - 3) * zf), (int) ((y - 3) * zf), (int) (7 * zf), (int) (7 * zf));
    }

    public void toot() {
        if (!GeneralSettings.getCurrentSettings().getPropertyBooleanValue("sound")) {
            return; // Sound is disabled 
        }
        try {
            (Applet.newAudioClip((new File(soundFileName)).toURI().toURL())).play();
        } catch (MalformedURLException e) {
            Logger.getLogger(SimEdgeNodePanel.class.getName()).log(Level.SEVERE, "unable to toot", e);
            // Why can't I toot ???
            // Stupid java....
            // Well... I'll honk via stdout ....
            System.out.println("TOOOOOT!");
        }
    }

    // Specific XMLSerializable implementation 
    @Override
    public String getXMLName() {
        return parentName + ".roaduser-auto";
    }
}
