package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

public class DocSaver implements AsyncCallback<UnlockedDocument> {

	private Collaborator collaborator;
	private LockedDocument lockedDocument;
	private String side; // is the current doc on the left or the right?
	private int index; // which tab is the current doc on?
	private final static int maxStrLen = 25;

	public static void saveDoc(Collaborator col, LockedDocument ld,
			String side, int ind) {
		DocSaver ds = new DocSaver(col);
		ds.saveDocument(ld, side, ind);
	}

	public DocSaver(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	public void saveDocument(LockedDocument lockedDoc, String side, int ind) {
		this.lockedDocument = lockedDoc;
		collaborator.statusUpdate("Attemping to save document.");
		collaborator.waitingKey = lockedDoc.getKey();
		collaborator.collabService.saveDocument(lockedDoc, this);
		this.side = side;
		this.index = ind;

		if (side.equals("left"))
			collaborator.setGenericObjects(true);
		else
			collaborator.setGenericObjects(false);				
		
		// the user cannot edit the title and the contents of this doc
		TextBox box = collaborator.titleList.get(index);
		TextArea area = collaborator.contentsList.get(index);
		box.setEnabled(false);
		area.setEnabled(false);
		
		// disable the save doc button for this side (until this doc is saved)
		collaborator.saveDocButton.setEnabled(false);
	}

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockExpired)
			collaborator.statusUpdate("Lock had already expired; save failed.");
		else {
			collaborator.statusUpdate("Error saving document"
					+ "; caught exception " + caught.getClass()
					+ " with message: " + caught.getMessage());
			GWT.log("Error saving document.", caught);
			collaborator.releaser.releaseLock(lockedDocument);
		}

		if (lockedDocument != null)
			collaborator.setDoc(lockedDocument.unlock(), index, side);
	}

	@Override
	public void onSuccess(UnlockedDocument result) {
		String title = result.getTitle();
		if (title.length() > maxStrLen) {
			title = title.substring(0, maxStrLen - 3) + "...";
		}
		collaborator.statusUpdate("Document '" + title
				+ "' successfully saved.");
		if (collaborator.waitingKey == null
				|| result.getKey().equals(collaborator.waitingKey)) {
			collaborator.setDoc(result, index, side);

			// Refresh list in case title was changed.
			collaborator.lister.getDocumentList();
		} else
			GWT.log("Saved document is not the anticipated document.");
	}
}
