package com.github.cc007.trafficlights.config;

import com.github.cc007.trafficlights.sim.SimController;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StuckCarsDialog extends Dialog {

    TextField[] texts;
    SimController c;

    public StuckCarsDialog(SimController c) {
        super(c, "Car removal properties...", true);
        this.c = c;

        // Window properties
        setResizable(false);
        setSize(250, 150);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        setLayout(new BorderLayout());

        // Window content
        ActionListener al = new StuckCarsActionListener();
        this.add(new StuckCarsPanel(), BorderLayout.CENTER);
        this.add(new OkCancelPanel(al), BorderLayout.SOUTH);

    }

    //public TLCSettings getSettings() { return settings; }
    /**
     * Listens to the buttons of the dialog.
     */
    public class StuckCarsActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String sel = ((Button) e.getSource()).getLabel();

            if (sel.equals("Ok")) {

                try {
                    c.setMaxWaitingTime(Integer.parseInt(texts[0].getText()));
                    c.setPenalty(Integer.parseInt(texts[1].getText()));
                } catch (NumberFormatException ex) {
                    Logger.getLogger(StuckCarsDialog.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            setVisible(false);
        }
    }

    public class StuckCarsPanel extends Panel {

        public StuckCarsPanel() {
            GridBagLayout gridbag = new GridBagLayout();
            this.setLayout(gridbag);

            texts = new TextField[2];
            texts[0] = makeRow(gridbag, "Maximum waiting time", texts[0], c.getMaxWaitingTime() + "");
            texts[1] = makeRow(gridbag, "Penalty", texts[1], c.getPenalty() + "");
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
