
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

/**
 *
 * The main class that start the GLD simulator
 *
 * @author Group Model
 * @version 1.0
 */

public class GLDSim
{
        public static int seriesSeedIndex = 0;
        public final static long [] seriesSeed = {
                                           0, 1000, 8000, 6000, 7000, 4000, 9000, 10000, 11000, 12000,13000,14000
                                           // 0 wordt gebruikt om het programma op te starten,
                                           //   eerste run heeft dus seed index 1, tweede 2 enz.
                                           //3000, 4000 lopen vast
        };

        public static void main (String[] params)
	{	(new GLDStarter(params,GLDStarter.SIMULATOR)).start();
	}
}
