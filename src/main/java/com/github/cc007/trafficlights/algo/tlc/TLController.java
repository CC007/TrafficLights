
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
package com.github.cc007.trafficlights.algo.tlc;

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.algo.tlc.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.utils.*;
import com.github.cc007.trafficlights.xml.*;

import java.awt.*;
import java.awt.Panel;
import java.awt.event.*;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * This is the abstract class for Traffic light algorithms. It is informed about
 * every movement made by road users. In this way not every road user has to be
 * iterated. By using this information it provides a table containing
 * Q-values(reward values) for each trafficlight in it's 'Green' setting.
 *
 * @author Group Algorithms
 * @version 1.0
 */
public abstract class TLController implements XMLSerializable, TwoStageLoader {

    protected Infrastructure infra;
    protected TLDecision[][] tld;
    public int trackNode = -1;
    protected int num_tls = 0;

    /**
     * The constructor for TL controllers
     *
     * @param The infrastructure being used.
     */
    TLController() {
    }

    TLController(Infrastructure i) {
        setInfrastructure(i);
    }

    public Infrastructure getInfrastructure() {
        return infra;
    }

    public void setInfrastructure(Infrastructure i) {
        tld = createDecisionArray(i);
        this.infra = i;
    }

    /**
     * Calculates how every traffic light should be switched
     *
     * @param The TLDecision is a tuple consisting of a traffic light and a
     * reward (Q) value, for it to be green
     * @see gld.algo.tlc.TLDecision
     */
    public abstract TLDecision[][] decideTLs();

    /**
     * Creates a TLDecision[][] for the given infrastructure. All Q values are
     * set to 0
     */
    public TLDecision[][] createDecisionArray(Infrastructure infra) {
        Node[] nodes = infra.getAllNodes();
        int num_nodes = nodes.length;

        Sign[] signs = null;
        int num_signs = 0;
        int counter;

        TLDecision[][] tld = new TLDecision[num_nodes][];
        TLDecision[] dec = null;
        Node node = null;

        for (int i = 0; i < num_nodes; i++) {
            node = nodes[i];
            counter = 0;

            if (node.getType() == Node.JUNCTION) {
                signs = ((Junction) node).getSigns();
            } else {
                signs = new Sign[0];
            }

            num_signs = signs.length;
            dec = new TLDecision[num_signs];

            for (int j = 0; j < num_signs; j++) {
                if (signs[j].getType() == Sign.TRAFFICLIGHT) {
                    dec[counter] = new TLDecision((TrafficLight) signs[j], 0);
                    counter++;
                    num_tls++;
                }
            }

            if (counter < num_signs) {
                dec = (TLDecision[]) Arrayutils.cropArray(dec, counter);
            }

            tld[i] = dec;
        }

        return tld;
    }

    /**
     * Extracts the Gain-values of a decision array for load/save
     */
    protected float[][] getGainValuesFromDecisionArray(TLDecision[][] array) {
        float[][] result = new float[array.length][array[0].length];
        for (int t = 0; t < array.length; t++) {
            result[t] = new float[array[t].length];
            for (int u = 0; u < array[t].length; u++) {
                result[t][u] = array[t][u].getGain();
            }
        }
        return result;
    }

    /**
     * Apply an array of Gain-values to an array of TLDecisions Assumes that the
     * dimensions of the two arrays are equal.
     */
    protected void applyGainValues(TLDecision[][] array, float[][] value) {
        for (int t = 0; t < array.length; t++) {
            for (int u = 0; u < array[t].length; u++) {
                array[t][u].setGain(value[t][u]);
            }
        }
    }

    /**
     * Resets the Algorithm
     */
    public void reset() {
    }

    /**
     * Sets the Node that can be tracked during excecution of a TLC
     */
    public void trackNode(int i) {
        trackNode = i;
    }

    /**
     * Returns the number of TrafficLights in this Infrastructure
     */
    public int getNumTLs() {
        return num_tls;
    }

    public abstract void updateRoaduserMove(
            Roaduser _ru, DriveLane _prevlane, Sign _prevsign, int _prevpos, DriveLane _dlanenow,
            Sign _signnow, int _posnow, PosMov[] _posMovs,
            DriveLane _desiredLane);

    /**
     * Loads arguments provided on command line (DOAS 06). This function should
     * be overloaded and the overloading function should call
     * super.loadArgs(args). HashMap is in the form <name, value>, where both
     * name and value are strings. Name is lower case.
     */
    public void loadArgs(HashMap args) throws Exception {
        String track = (String) args.remove("tracknode");
        if (track != null) {
            trackNode = Integer.parseInt(track);
        }
    }

    // XMLSerializable implementation
    @Override
    public void load(XMLElement myElement, XMLLoader loader) throws XMLTreeException, IOException, XMLInvalidInputException {
        trackNode = myElement.getAttribute("track-node").getIntValue();
    }

    @Override
    public XMLElement saveSelf() throws XMLCannotSaveException {
        XMLElement result = new XMLElement("tlc");
        result.addAttribute(new XMLAttribute("track-node", trackNode));
        return result;
    }

    @Override
    public void saveChilds(XMLSaver saver) throws XMLTreeException, IOException, XMLCannotSaveException { 	// A TLController has no child objects
    }

    @Override
    public void setParentName(String parentName) throws XMLTreeException {
        throw new XMLTreeException("Attempt to change fixed parentName of a TLC class.");
    }

    // Empty TwoStageLoader (standard)
    @Override
    public void loadSecondStage(Map<String, Map<Integer, TwoStageLoader>> maps) throws XMLInvalidInputException, XMLTreeException {
    }

    //////////// TLC settings ///////////
    /**
     * To be overridden by subclasses if TLC settings are to be modified.
     */
    public void showSettings(Controller c) {
        return;
    }

    /**
     * Shows the TLC settings dialog for the given TLCSettings.
     */
    protected TLCSettings doSettingsDialog(Controller c, TLCSettings settings) {
        TLCDialog tlcDialog;
        tlcDialog = new TLCDialog(c, settings);
        tlcDialog.setVisible(true);
        return tlcDialog.getSettings();
    }

    /**
     *
     * Class used in combination with TLCDialog to modify TLC-specific settings.
     *
     * @author Group GUI
     * @version 1.0
     */
    protected class TLCSettings {

        public String[] descriptions;
        public int[] ints;
        public float[] floats;

        public TLCSettings(String[] _descriptions, int[] _ints, float[] _floats) {
            descriptions = _descriptions;
            ints = _ints;
            floats = _floats;
        }
    }

    /**
     *
     * The dialog used to set <code>TLController</code> properties.
     *
     * @author Group GUI
     * @version 1.0
     */
    protected class TLCDialog extends Dialog {

        TextField[] texts;
        TLCSettings settings;

        /**
         * Creates a <code>TLCDialog</code>.
         */
        public TLCDialog(Controller c, TLCSettings _settings) {
            super(c, "TLC properties...", true);
            settings = _settings;

            setResizable(false);
            setSize(500, 250);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                }
            });
            setLayout(new BorderLayout());

            ActionListener al = new TLCActionListener();
            this.add(new TLCPropPanel(), BorderLayout.CENTER);
            this.add(new OkCancelPanel(al), BorderLayout.SOUTH);
        }

        /*============================================*/
 /* GET                                        */
 /*============================================*/
        public TLCSettings getSettings() {
            return settings;
        }

        /*============================================*/
 /* Listeners                                  */
 /*============================================*/
        /**
         * Listens to the buttons of the dialog.
         */
        public class TLCActionListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                String sel = ((Button) e.getSource()).getLabel();
                if (sel.equals("Ok")) {
                    int tc = 0;
                    try {
                        if (settings.ints != null) {
                            for (int i = 0; i < settings.ints.length; i++, tc++) {
                                settings.ints[i] = Integer.parseInt(texts[tc].getText());
                            }
                        }
                        if (settings.floats != null) {
                            for (int i = 0; i < settings.floats.length; i++, tc++) {
                                settings.floats[i] = Float.valueOf(texts[tc].getText()).floatValue();
                            }
                        }
                    } catch (NumberFormatException nfe) {
                        String s = settings.ints == null ? "float" : (tc < settings.ints.length ? "int" : "float");
                        texts[tc].setText("Enter a valid " + s + "! (Or press cancel)");
                        return;
                    }
                }
                setVisible(false);
            }
        }

        /*============================================*/
 /* Panels                                     */
 /*============================================*/
        /**
         * Panel containing the necessary components to set the TLC properties.
         */
        public class TLCPropPanel extends Panel {

            public TLCPropPanel() {
                GridBagLayout gridbag = new GridBagLayout();
                this.setLayout(gridbag);

                texts = new TextField[settings.descriptions.length];
                int tc = 0;
                if (settings.ints != null) {
                    for (int i = 0; i < settings.ints.length; i++, tc++) {
                        texts[tc] = makeRow(gridbag, settings.descriptions[tc], texts[tc], settings.ints[i] + "");
                    }
                }
                if (settings.floats != null) {
                    for (int i = 0; i < settings.floats.length; i++, tc++) {
                        texts[tc] = makeRow(gridbag, settings.descriptions[tc], texts[tc], settings.floats[i] + "");
                    }
                }
            }

            private TextField makeRow(GridBagLayout gridbag, String label, TextField textField, String text) {
                GridBagConstraints c = new GridBagConstraints();
                Label lbl;

                c.fill = GridBagConstraints.BOTH;
                c.weightx = 1.0;
                lbl = new Label(label);
                gridbag.setConstraints(lbl, c);
                this.add(lbl);
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.weightx = 1.0;
                textField = new TextField(text, 10);
                gridbag.setConstraints(textField, c);
                this.add(textField);
                return textField;
            }
        }

        /**
         * Panel containing buttons "Ok" and "Cancel".
         */
        public class OkCancelPanel extends Panel {

            public OkCancelPanel(ActionListener action) {
                this.setLayout(new FlowLayout(FlowLayout.CENTER));
                String[] labels = {"Ok", "Cancel"};
                Button b;
                for (int i = 0; i < labels.length; i++) {
                    b = new Button(labels[i]);
                    b.addActionListener(action);
                    this.add(b);
                }
            }
        }

    }

}
