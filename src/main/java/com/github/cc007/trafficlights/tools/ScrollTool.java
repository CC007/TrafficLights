
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

import java.awt.*;

/**
 * Uses the ScrollAction to scroll the view
 *
 * @author Group GUI
 * @version 1.0
 */

public class ScrollTool extends PopupMenuTool
{
	protected ScrollAction sa;

	public ScrollTool(Controller c) {
		super(c);
		sa = new ScrollAction(c.getViewScroller());
	}

    @Override
	public void mousePressed(View view, Point p, Tool.Mask mask)
	{
		super.mousePressed(view, p, mask);
		if (mask.isLeft()) {
            sa.startScroll(view, p);
        }
	}

    @Override
	public void mouseMoved(View view, Point p, Tool.Mask mask) {
		if (mask.isLeft()) {
            sa.doScroll(view, p);
        }
	}
    @Override
	public void mouseReleased(View view, Point p, Tool.Mask mask) {
		if (mask.isLeft()) {
            sa.endScroll(view, p);
        }
	}
    @Override
	public int overlayType() { return 0; }
    @Override
	public void paint(Graphics g) throws GLDException { }

    @Override
	public Panel getPanel() { return new Panel(null); }
}