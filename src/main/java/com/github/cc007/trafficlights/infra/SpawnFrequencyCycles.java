

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

import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.xml.*;
import java.io.IOException;
import java.util.Hashtable;

/**
* Contains a destination frequency for a certain roaduser type.
*
* @author Group Datastructures
* @version 1.0
*/
public class SpawnFrequencyCycles implements XMLSerializable
{
        public int ruType;
        public float freq;
        public int cycle;
        protected String parentName="model.infrastructure.node";

        /** Empty constructor for loading */
        public SpawnFrequencyCycles()
        {

        }

        /**
        * Creates an instance initiated with given parameters.
        * @param _ruType Roaduser type.
        * @param _freq Initial frequency.
        * @param _cycle The cycle for witch edge should change the frequency _freq for roadusertype _ruType
        */
        public SpawnFrequencyCycles(int _ruType, int _cycle, float _freq)
        {
                ruType = _ruType;
                freq = _freq;
                cycle = _cycle;
        }

        // XML Serializable implementation

        public void load (XMLElement myElement,XMLLoader loader) throws XMLTreeException,IOException,XMLInvalidInputException
        { 	ruType=myElement.getAttribute("ru-type").getIntValue();
                  freq=myElement.getAttribute("freq").getFloatValue();
                  cycle=myElement.getAttribute("cycle").getIntValue();
        }
        public XMLElement saveSelf () throws XMLCannotSaveException
        { 	XMLElement result=new XMLElement("dspawnfreq");
                  result.addAttribute(new XMLAttribute("ru-type",ruType));
                  result.addAttribute(new XMLAttribute("freq",freq));
                  result.addAttribute(new XMLAttribute("cycle",cycle));
                 return result;
        }

        public void saveChilds (XMLSaver saver) throws XMLTreeException,IOException,XMLCannotSaveException
        { 	// A spawnfrequencycycle has no child objects
        }

        public String getXMLName ()
        { 	return parentName+".dspawnfreq";
        }

        public void setParentName (String newParentName)
        {	this.parentName=parentName;
        }

        public String toString()
        {
               return new String("At Cycle " + cycle + ": " + freq);
        }

}
