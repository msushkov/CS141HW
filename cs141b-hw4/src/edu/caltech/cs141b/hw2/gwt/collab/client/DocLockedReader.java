package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
 */
public class DocLockedReader implements AsyncCallback<LockedDocument> {

	private Collaborator collaborator;
	String side; // is the current doc on the left or the right?
	private int index; // which tab is the current doc on?

	/**
	 * This function creates a new DocLocker and calls it. This function
	 * basically allows for multiple requests to be sent at the same time with
	 * different internal state associated with them.
	 * @param collaborator
	 * @param key
	 * @param side
	 * @param ind
	 * @return
	 */
	public static void getLockedDoc(Collaborator collab, String key, String side,
			int ind) {
		DocLockedReader dl = new DocLockedReader(collab);
		dl.getLockDocument(key, side, ind);
	}

	public DocLockedReader(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	/**
	 * Sends a request to fetch a locked doc
	 * @param key
	 * @param side
	 * @param index
	 */
	public void getLockDocument(String key, String side, int index) {
		this.side = side;
		this.index = index;

		collaborator.collabService.getLockedDocument(collaborator.clientID, key, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error retrieving document");
		GWT.log("Error retrieving document", caught);

		// set and enable/disable correct buttons
		lockFailed();
	}

	@Override
	public void onSuccess(LockedDocument result) {
		collaborator.statusUpdate("Document Retrieved");
		
		// if we are in simulation mode (or stopping), then go into the
		// "eating" part of the simulation (if we are stopping then at least
		// finish eating)
		if (collaborator.simulation || collaborator.simulationStopping)
			collaborator.simulateEating(result, index, side);
		else
			collaborator.setDoc(result, index, side);
	}

	/**
	 * Called when docLockerReader fails to fetch the document.
	 */
	protected void lockFailed() {
		if (side.equals("left"))
			collaborator.setGenericObjects(true);
		else
			collaborator.setGenericObjects(false);

		TextBox box = collaborator.titleList.get(index);
		TextArea area = collaborator.contentsList.get(index);

		// the user cannot edit the title and the contents of this doc
		box.setEnabled(false);
		area.setEnabled(false);

		// we need lock, removeTab, and refresh buttons
		collaborator.hPanel.clear();
		collaborator.hPanel.add(collaborator.lockButton);
		collaborator.hPanel.add(collaborator.refresh);
		collaborator.hPanel.add(collaborator.removeTabButton);

		collaborator.lockButton.setEnabled(true);
		collaborator.removeTabButton.setEnabled(true);
		collaborator.refresh.setEnabled(true);
	}

	/**
	 * If we successfully acquired the locker - can now edit the doc.
	 * 
	 * @param result
	 * @param side
	 * @param index
	 */
	protected void gotDoc(LockedDocument result, String side, int index) {
		if (side.equals("left"))
			collaborator.setGenericObjects(true);
		else
			collaborator.setGenericObjects(false);

		TextBox box = collaborator.titleList.get(index);
		TextArea area = collaborator.contentsList.get(index);

		collaborator.docList.set(index, result);

		// set the title and contents of this doc to be the current thing on the
		// page
		box.setValue(result.getTitle());
		area.setText(result.getContents());

		// if not in simulation mode, the user can now edit the 
		// title and the contents of this doc
		if (!collaborator.simulation && !collaborator.simulationStopping)
		{
			box.setEnabled(true);
			area.setEnabled(true);
		}
		else
		{
			box.setEnabled(false);
			area.setEnabled(false);
		}

		// we need save, removeTab, and refresh buttons
		collaborator.hPanel.clear();
		collaborator.hPanel.add(collaborator.saveDocButton);
		collaborator.hPanel.add(collaborator.refresh);
		collaborator.hPanel.add(collaborator.removeTabButton);

		collaborator.enableButton(collaborator.saveDocButton);
		collaborator.enableButton(collaborator.removeTabButton);
		collaborator.disableButton(collaborator.refresh);
	}
}
