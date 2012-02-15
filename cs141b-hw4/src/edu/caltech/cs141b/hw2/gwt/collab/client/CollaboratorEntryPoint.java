package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This sets up the basic infrastructure, takes down the loading sign, adds a
 * collaborator widget, and starts history management.  History management is
 * done here, rather than elsewhere, in case at some point the Collaborator
 * widget is not the only involved widget.
 */
public class CollaboratorEntryPoint implements EntryPoint {
	
	Collaborator collab;

	/**
	 * Create a remote service proxy to talk to the server-side service.
	 */
	private final CollaboratorServiceAsync collabService =
			GWT.create(CollaboratorService.class);

	private final TokenServiceAsync tokenService =
		GWT.create(TokenService.class);
	

	/**
	 * This is the entry point method, meaning the first method called when
	 * this module is initialized.
	 */
	public void onModuleLoad() {
		collab = new Collaborator(collabService, tokenService);
		
		// Make the loading display invisible and the application visible.
		RootPanel.get("application").add(collab);
		RootPanel.get("loading").setVisible(false);
	}

}

