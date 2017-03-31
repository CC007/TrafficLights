
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

package com.github.cc007.trafficlights.edit;

import com.github.cc007.trafficlights.*;
import com.github.cc007.trafficlights.infra.*;
import com.github.cc007.trafficlights.xml.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.util.List;

/**
 *
 * The main controller for the editor part of the application.
 *
 * @author Group GUI
 * @version 1.0
 */

public class EditController extends Controller
{
	/** The grid overlay */
	GridOverlay gridOverlay;
	
	/**
	 * Creates the main frame
	 *
	 * @param m The program can't run without this =]
     * @param splash
	 */
	public EditController(EditModel m, boolean splash) 
	{
		super(m, splash);
		MousePosListener mpl = new MousePosListener();
		view.addMouseListener(mpl);
		view.addMouseMotionListener(mpl);
		Class[] sf = { Node.class, Road.class };
		currentSelection.setSelectionFilter(sf);
	}













	/*============================================*/
	/* GET and SET methods                        */
	/*============================================*/


	/** Returns the current <code>EditModel</code> */
  public EditModel getEditModel() { return (EditModel)model; }
	/** Sets the current <code>EditModel</code> */
  public void setEditModel(EditModel m) { model = m; }

	/** Returns the current infrastructure's filename */
	public String getCurrentFilename() { return currentFilename; }



















	/*============================================*/
	/* Loading and saving                         */
	/*============================================*/

    @Override
 	public XMLElement saveSelf() throws XMLCannotSaveException
	{
		XMLElement result = super.saveSelf();
		result.addAttribute(new XMLAttribute("saved-by","editor"));
		return result;
	}


    @Override
	protected void doSave(String filename) throws InvalidFilenameException, Exception
	{	boolean valid = validateInfra();
		if (!valid) {
            throw new InvalidFilenameException("Can't save invalid infrastructure to file.");
    }
		if (!filename.endsWith(".infra")) {
            throw new InvalidFilenameException("Filename must have .infra extension.");
    }
		setStatus("Saving infrastructure to " + filename);	
		XMLSaver saver=new XMLSaver(new File(filename));
		saveAll(saver,getEditModel());
		setStatus("Saved infrastructure to " + filename);
		saver.close();
	}
 
    @Override
	protected void doLoad(String filename) throws InvalidFilenameException, Exception
	{  if (!filename.endsWith(".infra")) {
        throw new InvalidFilenameException("Only able to load .infra files.");
    }
		XMLLoader loader=new XMLLoader(new File(filename));
		loadAll(loader,getEditModel());
		newInfrastructure(model.getInfrastructure());
	}

















	/*============================================*/
	/* Miscellanous                               */
	/*============================================*/

    @Override
	protected String appName() { return "editor"; }

	protected void enableGrid() {
		view.addOverlay(gridOverlay = new GridOverlay(view.getSize()));
	}

	protected void disableGrid() {
		view.remOverlay(gridOverlay);
	}

	/** Creates the menubar for the editor */
    @Override
  protected MenuBar createMenuBar() {
  	return new EditMenuBar(this);
  }

	/** Creates the toolbar for the editor */  
    @Override
  protected GLDToolBar createToolBar() {
  	return new EditToolBar(this);
  }

	/** Creates a right-click popup-menu for the givens object */
    @Override
	public PopupMenu getPopupMenuFor(Selectable obj) throws PopupException {
		EditPopupMenuFactory pmf = new EditPopupMenuFactory(this);
		return pmf.getPopupMenuFor(obj);
	}
	

























	/*============================================*/
	/* Invoked by listeners                       */
	/*============================================*/

	/** Shows the file properties dialog */
    @Override
	public void showFilePropertiesDialog()
	{
		Infrastructure infra = getEditModel().getInfrastructure();
		String title = infra.getTitle();
		String author = infra.getAuthor();
		String comments = infra.getComments();
		
		EditPropDialog propDialog = new EditPropDialog(this, title, author, comments);
		
		propDialog.setVisible(true);
		if (propDialog.ok())
		{
			infra.setTitle(propDialog.getInfraname());
			infra.setAuthor(propDialog.getAuthor());
			infra.setComments(propDialog.getComments());
		}
		this.setStatus("\"" + infra.getTitle() + "\" by " + infra.getAuthor());
	}
	
	/** Shows the change size dialog */
	public void showChangeSizeDialog() {
		Dimension dim = model.getInfrastructure().getSize();
		EditSizeDialog esd = new EditSizeDialog(this, dim.width, dim.height);
		esd.setVisible(true);
		if (esd.ok()) {
			dim = new Dimension(esd.getWidthI(), esd.getHeightI());
			model.getInfrastructure().setSize(dim);
			viewScroller.resizeInfra(dim);
		}
	}


	/** Removes all objects from the infrastructure that are currently selected */
	public void deleteSelection() {
		try {
			List<Selectable> objects = currentSelection.getSelectedObjects();
			currentSelection.setSelectedObjects(new LinkedList<>());
			getEditModel().remObjects(objects);
		}
		catch (GLDException e) {
			reportError(e.fillInStackTrace());
		}
	}
	
	/** Selects all objects in the infrastructure */
	public void selectAll() {
		currentSelection.selectAll();
	}
	
	/** Empties the current selection (deselects) */
	public void deselectAll() {
		currentSelection.deselectAll();
	}
	












	/*============================================*/
	/* Mouse listeners                            */
	/*============================================*/

	/**
	 * Shows the mouse position in the status bar.
	 */
	protected class MousePosListener implements MouseListener, MouseMotionListener
	{
		/** Sets the status bar text to be the current mouse position. */
		protected void posToScreen(Point p) {
			p = getView().toInfra(p);
			setStatus("Mouse at position ("+p.x+","+p.y+")");
		}

		/** Invoked when a mouse button is pressed on the View. */
        @Override
		public void mousePressed(MouseEvent e) { posToScreen(e.getPoint()); }
		/** Invoked when a mouse button is released on the View. */
        @Override
		public void mouseReleased( MouseEvent e ) { posToScreen(e.getPoint()); }
		/** Invoked when the mouse cursor is moved over the View. */
        @Override
		public void mouseMoved( MouseEvent e ) { posToScreen(e.getPoint()); }
		/** Invoked when the mouse cursor is dragged over the View. */
        @Override
		public void mouseDragged( MouseEvent e ) { posToScreen(e.getPoint()); }
		/** Invoked when the mouse cursor enters the View. */
        @Override
		public void mouseEntered( MouseEvent e ) {	posToScreen(e.getPoint());	}
		/** Invoked when the mouse cursor exits the View. */
        @Override
		public void mouseExited( MouseEvent e ) { setStatus("Ready."); }
		/** Empty implementation, required by the MouseListener interface. */
        @Override
		public void mouseClicked( MouseEvent e ) { }
	}
}