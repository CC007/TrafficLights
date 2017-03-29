
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
package com.github.cc007.trafficlights;

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.utils.*;
import com.github.cc007.trafficlights.tools.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Abstract ToolBar for simulator and editor contains common elements
 *
 *
 * @author Group GUI
 * @version 1.0
 */
public abstract class GLDToolBar extends ToolBar implements ActionListener, ItemListener {

    protected static final int NEW = 1;
    protected static final int OPEN = 2;
    protected static final int SAVE = 3;
    protected static final int CENTER = 4;
    protected static final int SCROLL = 5;
    protected static final int ZOOM = 6;
    protected static final int SELECT = 7;
    protected static final int EDGENODE = 9;
    protected static final int CONFIG = 10;

    protected static final int APPBUTTON = 100;

    protected static final int HELP = 911;

    protected Controller controller;

    protected Choice zoom;

    public GLDToolBar(Controller c, boolean newicon) {
        super();
        controller = c;
        if (newicon) {
            addButton("images/new.gif", this, NEW);
        }
        addButton("images/open.gif", this, OPEN);
        addButton("images/save.gif", this, SAVE);

        addSeparator();

        zoom = new Choice();
        zoom.add("25%");
        zoom.add("50%");
        zoom.add("75%");
        zoom.add("100%");
        zoom.add("150%");
        zoom.add("200%");
        zoom.add("250%");
        zoom.addItemListener(this);
        zoom.setSize(75, 15);
        zoom.select(3);
        addComponent(zoom);

        addSeparator();
        addButton("images/center.gif", this, CENTER);
        addSeparator();

        addButton("images/scroll.gif", this, SCROLL);
        addButton("images/zoom.gif", this, ZOOM);
        addButton("images/select.gif", this, SELECT);

        addSeparator();

        addTools();

        addSeparator();

        addButton("images/config.gif", this, CONFIG);

        addSeparator();

        addButton("images/help.gif", this, HELP);

        addSeparator();
    }

    protected abstract void addTools();

    public Choice getZoom() {
        return zoom;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int id = ((IconButton) e.getSource()).getId();
        switch (id) {
            case NEW: {
                controller.newFile();
                break;
            }
            case OPEN: {
                controller.openFile();
                break;
            }
            case SAVE: {
                controller.saveFile();
                break;
            }
            case CENTER: {
                controller.getViewScroller().center();
                break;
            }
            case SCROLL: {
                controller.changeTool(new ScrollTool(controller));
                break;
            }
            case ZOOM: {
                controller.changeTool(new ZoomTool(controller));
                break;
            }
            case SELECT: {
                controller.changeTool(new SelectTool(controller));
                break;
            }
            case EDGENODE: {
                controller.changeTool(new EdgeNodeTool(controller));
                break;
            }
            case CONFIG: {
                controller.switchConfigDialog();
                break;
            }
            case HELP: {
                controller.showHelp(HelpViewer.HELP_INDEX);
                break;
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        controller.zoomTo(zoom.getSelectedIndex());
    }
}
