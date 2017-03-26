
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

package com.github.cc007.trafficlights.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * This enumeration can walk 1 or 2 dimensional arrays
 *
 * @author Joep Moritz
 * @version 1.0
 */

public class ArrayIterator implements Iterator
{
	Object[][] ar;
	int i, j;

	public ArrayIterator(Object[] _ar) {
		ar = new Object[1][];
		ar[0] = _ar;
		i = 0;
		j = 0;
	}

	public ArrayIterator(Object[][] _ar) {
		ar = _ar;
		i = 0;
		j = 0;
	}
	
	public ArrayIterator(Object[] _ar1, Object[] _ar2) {
		ar = new Object[2][];
		ar[0] = _ar1;
		ar[1] = _ar2;
		i = 0;
		j = 0;
	}

    @Override
	public boolean hasNext() {
		return i < ar.length && j < ar[i].length;
	}

    @Override
	public Object next() throws NoSuchElementException {
		if (!hasNext()) throw new NoSuchElementException();
		Object o = ar[i][j++];
		if (j >= ar[i].length) {
			i++;
			j = 0;
		}
		return o;
	}

    
}