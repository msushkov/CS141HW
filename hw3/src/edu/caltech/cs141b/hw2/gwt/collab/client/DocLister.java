package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;

/**
 * Used in conjunction with <code>CollaboratorService.getDocumentList()</code>.
 */
public class DocLister implements AsyncCallback<List<DocumentMetadata>> {

	private Collaborator collaborator;

	public DocLister(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	public void getDocumentList() {
		// collaborator.statusUpdate("Fetching document list.");

		// disable the refresh button while we are getting the doc list
		collaborator.refreshDoc.setEnabled(false);

		// get the list of docs
		collaborator.collabService.getDocumentList(this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error retrieving document list. "
				+ "Please refresh the page!");
		GWT.log("Error getting document list.", caught);

		// we can press the refresh button again
		collaborator.refreshDoc.setEnabled(true);
	}

	@Override
	public void onSuccess(List<DocumentMetadata> result) {
		if (result != null && result.size() > 0) {
			GWT.log("Got " + result.size() + " documents.");

			collaborator.documentList.clear();

			// iterate through the docs list and add each to the listbox as well
			// as our lists
			for (DocumentMetadata meta : result)
				collaborator.documentList.addItem(meta.getTitle(), meta
						.getKey());

			// after doc list refreshes, select the more recent
			// document (at the very bottom)
			int num = collaborator.documentList.getItemCount();
			collaborator.documentList.setSelectedIndex(num - 1);
		}

		// we can press the refresh button again
		collaborator.refreshDoc.setEnabled(true);
	}

}
