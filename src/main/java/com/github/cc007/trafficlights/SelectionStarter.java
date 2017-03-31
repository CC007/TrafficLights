
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

import java.util.*;

/**
 *
 *
 * @author Group Datastructures
 * @version 1.0
 */

public interface SelectionStarter
{
	/** @return true if this Selectable has any children */
	public boolean hasChildren();
	/** @return all Selectables held by this Selectable */
	public List<? extends Selectable> getChildren();
}