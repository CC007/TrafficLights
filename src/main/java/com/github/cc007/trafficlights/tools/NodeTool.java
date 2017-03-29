
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

import java.awt.*;
import java.awt.event.*;

/**
 * Use this Tool to create Nodes in the infrastructure.
 *
 * @author Group GUI
 * @version 1.0
 */

public class NodeTool extends PopupMenuTool
{
	NodeAction na;
	NodeTypeChoice typePanel;
	
	public NodeTool(EditController c) {
		super(c);
		na = new NodeAction(c.getEditModel());
		typePanel = new NodeTypeChoice();
	}

    @Override
	public void mousePressed(View view, Point p, Tool.Mask mask)
	{
		super.mousePressed(view, p, mask);
		if (mask.isLeft()) {
            na.doCreateNode(view, p, typePanel.getNodeType());
        }
	}
	
    @Override
	public void mouseReleased(View view, Point p, Tool.Mask mask) { }
    @Override
	public void mouseMoved(View view, Point p, Tool.Mask mask) { }
    @Override
	public int overlayType() { return 0; }
    @Override
	public void paint(Graphics g) throws GLDException { }
	
    @Override
	public Panel getPanel() { return typePanel; }

  public class NodeTypeChoice extends Panel implements ItemListener
  {
  	int nodeType = 2;

  	public NodeTypeChoice()
  	{
  		super();
  		setLayout(null);
	  	
	  	Choice nodeTypeSel = new Choice();
  		nodeTypeSel.add("Edge node");
  		nodeTypeSel.add("Traffic lights");
			nodeTypeSel.add("No signs");
			nodeTypeSel.add("Net-tunnel");
  		nodeTypeSel.select(1);
  		nodeTypeSel.addItemListener(this);
  		add(nodeTypeSel);
  		nodeTypeSel.setBounds(0, 0, 100, 20);
  		setSize(200, 24);
  	}
  	
  	public int getNodeType() { return nodeType; }
  	public void setNodeType(int type) { nodeType = type; }

      @Override
		public void itemStateChanged(ItemEvent e) {
			setNodeType(((Choice) e.getSource()).getSelectedIndex() + 1);
		}
  }
}