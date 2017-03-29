
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

import java.io.*;
import java.util.NoSuchElementException;

import com.github.cc007.trafficlights.xml.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.sim.*;
import com.github.cc007.trafficlights.sim.stats.*;
import com.github.cc007.trafficlights.config.*;
import com.github.cc007.trafficlights.utils.ResourceUtils;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the general settings
 */
public class GeneralSettings implements Settings {

    public static final int COMPRESSION_NO = 0,
            COMPRESSION_GZIP = 1;
    public static final String settingsFileName = "settings.conf";
    public static Settings currentSettings;

    /**
     * Indicates if we should use our (slow) internal browser to show the help
     * files
     */
    protected boolean useInternalHelpBrowser = true;

    /**
     * Sound on/off
     */
    protected boolean sound = true;

    /**
     * Name and command for starting an external browser. These values only have
     * meaning if the internal browser is off
     */
    protected String browserName = "", browserCommand = "";

    /**
     * Compression method to use on the XML files we generate (see constants
     * above
     */
    protected int compressionMethod = COMPRESSION_NO;

    /**
     * The standard path for file dialogs
     */
    protected String standardPath = ".";

    /**
     * Create empty GeneralSettings (for loading)
     */
    public GeneralSettings() {
        currentSettings = this;
    }

    /**
     * Gets the current settings
     */
    public static Settings getCurrentSettings() {
        return currentSettings;
    }

    @Override
    public void doLoad() throws IOException, XMLTreeException, XMLInvalidInputException {
        File file = null;
        try {
            String jarFolder = new File(GeneralSettings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            file = new File(jarFolder + "/" + settingsFileName);
            if (!file.exists()) {
                ResourceUtils.exportResource(settingsFileName);
            }
        } catch (IOException ex) {
            Logger.getLogger(GeneralSettings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(GeneralSettings.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (file != null && file.exists()) {
            XMLLoader loader = new XMLLoader(file);
            load(loader.getNextElement(null, "settings"), loader);
            loader.close();
        } else {
            System.out.println("Settings file not found. Loading default settings");
        }
    }

    @Override
    public void doSave() throws IOException, XMLTreeException, XMLCannotSaveException {
        String jarFolder = null;
        try {
            jarFolder = new File(GeneralSettings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
        } catch (URISyntaxException ex) {
            Logger.getLogger(GeneralSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
        File file = new File(jarFolder + "/" + settingsFileName);
        XMLSaver saver = new XMLSaver(file);
        saver.saveObject(this);
        saver.close();
    }

    @Override
    public boolean isPredefined() {
        return true;
    }

    // Set methods
    @Override
    public void setProperty(String name, String value) throws NoSuchElementException {
        if ("browser-name".equals(name)) {
            browserName = value;
        } else if ("browser-command".equals(name)) {
            browserCommand = value;
        } else if ("standard-path".equals(name)) {
            standardPath = value;
        } else {
            throw new NoSuchElementException("GeneralSettings has no String property named " + name);
        }
    }

    @Override
    public void setProperty(String name, int value) throws NoSuchElementException {
        if ("compression".equals(name)) {
            compressionMethod = value;
        } else {
            throw new NoSuchElementException("GeneralSettings has no int property named " + name);
        }
    }

    @Override
    public void setProperty(String name, boolean value) throws NoSuchElementException {
        if ("use-jbrowser".equals(name)) {
            useInternalHelpBrowser = value;
        } else if ("sound".equals(name)) {
            sound = value;
        } else {
            throw new NoSuchElementException("GeneralSettings has no boolean property named " + name);
        }
    }

    public void setPropertyFloatValue(String name, float value) throws NoSuchElementException {
        throw new NoSuchElementException("GeneralSettings has no float property named " + name);
    }

    // Get methods
    @Override
    public String getPropertyStringValue(String name) throws NoSuchElementException {
        if ("browser-name".equals(name)) {
            return browserName;
        } else if ("browser-command".equals(name)) {
            return browserCommand;
        } else if ("standard-path".equals(name)) {
            return standardPath;
        } else {
            throw new NoSuchElementException("GeneralSettings has no String property named " + name);
        }
    }

    @Override
    public boolean getPropertyBooleanValue(String name) throws NoSuchElementException {
        if ("use-jbrowser".equals(name)) {
            return useInternalHelpBrowser;
        } else if ("sound".equals(name)) {
            return sound;
        } else {
            throw new NoSuchElementException("GeneralSettings has no boolean property named " + name);
        }
    }

    @Override
    public int getPropertyIntValue(String name) throws NoSuchElementException {
        if ("compression".equals(name)) {
            return compressionMethod;
        } else {
            throw new NoSuchElementException("GeneralSettings has no int property named " + name);
        }
    }

    @Override
    public float getPropertyFloatValue(String name) throws NoSuchElementException {
        throw new NoSuchElementException("GeneralSettings has no float property named " + name);
    }

    // XMLSerializable implementation
    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
        useInternalHelpBrowser = myElement.getAttribute("use-jbrowser").getBoolValue();
        sound = myElement.getAttribute("sound").getBoolValue();
        browserName = myElement.getAttribute("browser-name").getValue();
        browserCommand = myElement.getAttribute("browser-command").getValue();
        compressionMethod = myElement.getAttribute("compression").getIntValue();
        standardPath = myElement.getAttribute("standard-path").getValue();

        RoaduserFactory.PacChance = myElement.getAttribute("paccar-prob").getFloatValue();
        ConfigDialog.AlwaysOnTop = myElement.getAttribute("confd-alwaysontop").getBoolValue();
        SignController.CrossNodesSafely = myElement.getAttribute("signc-safenodecross").getBoolValue();
        SimModel.CrossNodes = myElement.getAttribute("simm-crossnodes").getBoolValue();
        TrackingView.SEP = myElement.getAttribute("stats-sep").getValue();
        RoaduserFactory.UseCustoms = myElement.getAttribute("infra-usecustoms").getBoolValue();

        StatisticsModel.SEP = TrackingView.SEP;
    }

    @Override
    public XMLElement saveSelf() throws XMLCannotSaveException {
        XMLElement result = new XMLElement(getXMLName());
        result.addAttribute(new XMLAttribute("use-jbrowser", useInternalHelpBrowser));
        result.addAttribute(new XMLAttribute("sound", sound));
        result.addAttribute(new XMLAttribute("browser-name", browserName));
        result.addAttribute(new XMLAttribute("browser-command", browserCommand));
        result.addAttribute(new XMLAttribute("compression", compressionMethod));
        result.addAttribute(new XMLAttribute("standard-path", standardPath));

        result.addAttribute(new XMLAttribute("paccar-prob", RoaduserFactory.PacChance));
        result.addAttribute(new XMLAttribute("confd-alwaysontop", ConfigDialog.AlwaysOnTop));
        result.addAttribute(new XMLAttribute("signc-safenodecross", SignController.CrossNodesSafely));
        result.addAttribute(new XMLAttribute("simm-crossnodes", SimModel.CrossNodes));
        result.addAttribute(new XMLAttribute("stats-sep", TrackingView.SEP));
        result.addAttribute(new XMLAttribute("infra-usecustoms", RoaduserFactory.UseCustoms));

        return result;
    }

    @Override
    public void saveChilds(XMLSaver saver) throws XMLTreeException, IOException, XMLCannotSaveException { 	// GeneralSettings has no child objects. childSettings are saved to
        // other settings files
    }

    @Override
    public String getXMLName() {
        return "settings";
    }

    @Override
    public void setParentName(String parentName) throws XMLTreeException {
        throw new XMLTreeException("GeneralSettings does not support setParentName(String)");
    }

}
