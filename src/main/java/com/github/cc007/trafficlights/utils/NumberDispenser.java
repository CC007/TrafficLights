
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

import com.github.cc007.trafficlights.xml.*;
import java.io.IOException;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;

// A very simple number dispenser for ID's. It sucks. It also works.
public class NumberDispenser implements XMLSerializable {

    Stack<Integer> stack;
    int counter;
    String parentName = "model";

    public NumberDispenser() {
        stack = new Stack<>();
        counter = 0;
    }

    public int get() {
        if (stack.isEmpty()) {
            return counter++;
        } else {
            return stack.pop();
        }
    }

    public void giveBack(int number) {
        if (number == counter - 1) {
            counter--;
        } else if (number < counter && !stack.contains(number)) {
            stack.push(number);
        }
    }

    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
        stack = new Stack<>();
        stack.addAll((ArrayList<Integer>) XMLArray.loadArray(this, loader));
        counter = myElement.getAttribute("counter").getIntValue();
    }

    @Override
    public XMLElement saveSelf() throws XMLCannotSaveException {
        XMLElement result = new XMLElement("dispenser");
        result.addAttribute(new XMLAttribute("counter", counter));
        return result;
    }

    @Override
    public void saveChilds(XMLSaver saver) throws XMLTreeException, IOException, XMLCannotSaveException {

        XMLArray.saveArray(new ArrayList<>(Arrays.asList(stack.toArray())), this, saver, "stack");
    }

    @Override
    public String getXMLName() {
        return parentName + ".dispenser";
    }

    @Override
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

}
