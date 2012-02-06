package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.getDocument()</code>.
 */
public class DocReader implements AsyncCallback<UnlockedDocument> {

	private Collaborator collaborator;
	private String side; // is the current doc on the left or the right?
	private int index; // which tab is the current doc on?
	private final static int maxStrLen = 25;


	public static DocReader readDoc(Collaborator collaborator, String key,
			String side, int ind) {
		DocReader r = new DocReader(collaborator);
		r.getDocument(key, side, ind);
		return r;
	}

	public DocReader(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	public void getDocument(String key, String side, int ind) {
		collaborator.statusUpdate("Fetching document " + key + ".");
		collaborator.waitingKey = key;
		this.index = ind;
		this.side = side;
		collaborator.collabService.getDocument(key, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error retrieving document"
				+ "; caught exception " + caught.getClass() + " with message: "
				+ caught.getMessage());
		GWT.log("Error getting document lock.", caught);
	}

	@Override
	public void onSuccess(UnlockedDocument result) {
		if (result.getKey().equals(collaborator.waitingKey)) {
			String title = result.getTitle();
			if (title.length() > maxStrLen) {
				title = title.substring(0, maxStrLen - 3) + "...";
			}
			collaborator.statusUpdate("Document '" + title
					+ "' successfully retrieved.");

			collaborator.setDoc(result, index, side);
		} else
			collaborator
					.statusUpdate("Returned document that is no longer expected; discarding.");
	}
}
