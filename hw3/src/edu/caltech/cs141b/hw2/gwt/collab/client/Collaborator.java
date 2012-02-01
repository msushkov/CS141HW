package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.Iterator;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabBar.Tab;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Main class for a single Collaborator widget.
 */
public class Collaborator extends Composite implements ClickHandler, ChangeHandler {

	protected CollaboratorServiceAsync collabService;

	// Track document information.
	protected UnlockedDocument readOnlyDoc = null;
	protected LockedDocument lockedDoc = null;

	// Managing available documents.
	protected ListBox documentList = new ListBox();
	private Button refreshList = new Button("Refresh Document List");
	private Button createNew = new Button("Create New Document");

	// For displaying document information and editing document content.
	//protected TextBox docTitle = new TextBox();
	//protected RichTextArea docContents = new RichTextArea();
	protected String docTitle = null; // currently selected tab - title
	protected String docContents = null; // currently selected tab - contents
	protected Button refreshDoc = new Button("Refresh Document");
	protected Button lockButton = new Button("Get Document Lock");
	protected Button saveButton = new Button("Save Document");

	// Buttons for adding to the tab panels
	//protected Button showLeft = new Button("Show Left");
	//protected Button showRight = new Button("Show Right");

	// The panels that show the open documents.
	//protected TabPanel documentsL = new TabPanel();
	//protected TabPanel documentsR = new TabPanel();
	
	// for now, just trying to get this work with 1 tabpanel
	protected TabPanel documents = new TabPanel();

	// Callback objects.
	protected DocLister lister = new DocLister(this);
	protected DocReader reader = new DocReader(this);
	private DocLocker locker = new DocLocker(this);
	protected DocReleaser releaser = new DocReleaser(this);
	private DocSaver saver = new DocSaver(this);
	protected String waitingKey = null;

	// Status tracking.
	private VerticalPanel statusArea = new VerticalPanel();
	
	// which tab do we currently have open?
	private int currTab;

	/**
	 * UI initialization.
	 * 
	 * @param collabService
	 */
	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public Collaborator(CollaboratorServiceAsync collabService) {
		this.collabService = collabService;

		// the outer panel where everything resides
		HorizontalPanel outerPanel = new HorizontalPanel();
		outerPanel.setWidth("100%");
		outerPanel.setStyleName("outerPanel");

		// LEFT SIDE

		// the vertical panel that holds the docs list and the console
		VerticalPanel docsAndConsole = new VerticalPanel();
		docsAndConsole.setSpacing(20);
		docsAndConsole.setStyleName("docsAndConsole"); 

		// the vertical panel that holds the available docs list
		VerticalPanel docListPanel = new VerticalPanel();
		docListPanel.setStyleName("docListPanel");
		docListPanel.setHeight("100%");
		docListPanel.setSpacing(10);
		docListPanel.add(new HTML("<h2>Available Documents</h2>"));
		documentList.setWidth("100%");
		docListPanel.add(documentList);

		// the horiz panel that holds the 'refresh' and 'new' buttons
		HorizontalPanel mainButtonPanel = new HorizontalPanel();
		mainButtonPanel.setStyleName("mainButtonPanel");
		mainButtonPanel.setSpacing(10);
		mainButtonPanel.add(refreshList);
		mainButtonPanel.add(createNew);
		//mainButtonPanel.add(showLeft);
		//mainButtonPanel.add(showRight);

		docListPanel.add(mainButtonPanel); // the button panel is part of the docs panel

		// panel to hold the 'docs and console' panel
		DecoratorPanel dp = new DecoratorPanel();
		dp.setWidth("100%");
		dp.add(docsAndConsole);

		docsAndConsole.add(docListPanel);

		// the console panel
		DecoratorPanel console = new DecoratorPanel();
		console.setWidth("100%");
		statusArea.setSpacing(10);
		statusArea.add(new HTML("<h2>Console</h2>"));

		console.add(statusArea);
		docsAndConsole.add(console);

		outerPanel.add(docsAndConsole);

		// RIGHT SIDE

		// the outermost documents panel
		VerticalPanel outerTabbedDocsPanel = new VerticalPanel();
		outerTabbedDocsPanel.setSpacing(20);
		outerTabbedDocsPanel.setStyleName("outerTabbedDocsPanel");

		// holds the documents tab panels
		DecoratorPanel tabs = new DecoratorPanel();
		tabs.setWidth("100%");
		VerticalPanel docsOuterVP = new VerticalPanel();
		docsOuterVP.add(new HTML("<h2>Selected Documents</h2>"));
		HorizontalPanel docsInnerHP = new HorizontalPanel();
		docsInnerHP.setSpacing(10);

		/*
		documentsL.add(new VerticalPanel(), "crap");
		documentsL.add(new VerticalPanel(), "lolz");
		documentsL.add(new VerticalPanel(), "blah");
		documentsR.add(new VerticalPanel(), "yo");
		documentsR.add(new VerticalPanel(), "lolzzz");
		documentsR.add(new VerticalPanel(), "blah2");


		// add the two tab panels to the docs panel
		docsInnerHP.add(documentsL);
		docsInnerHP.add(documentsR);
		 */

		// add the tab panel
		documents.setWidth("100%");
		RichTextArea cont = new RichTextArea();
		cont.setHTML(docContents);
		TextBox box = new TextBox();
		box.setText(docTitle);
		documents.add(cont, box);
		//documents.add(docContents, docTitle.getText());
		
		// every time we select a tab, record update the current tab variable
		documents.addSelectionHandler(new SelectionHandler() {
			@Override
			public void onSelection(@SuppressWarnings("rawtypes") SelectionEvent event) 
			{
				currTab = Integer.parseInt(event.getSelectedItem().toString());
			}
		});
		
		docsInnerHP.add(documents);
		docsOuterVP.add(docsInnerHP);
		tabs.add(docsOuterVP);
		outerTabbedDocsPanel.add(tabs);

		// add the individual doc buttons
		HorizontalPanel docButtons = new HorizontalPanel();
		docButtons.setSpacing(10);
		docButtons.add(refreshDoc);
		docButtons.add(lockButton);
		docButtons.add(saveButton);

		outerTabbedDocsPanel.add(docButtons);

		// add the left side of our page to the main outer panel
		outerPanel.add(outerTabbedDocsPanel);

		// Add click handlers to buttons
		refreshList.addClickHandler(this);
		createNew.addClickHandler(this);
		refreshDoc.addClickHandler(this);
		lockButton.addClickHandler(this);
		saveButton.addClickHandler(this);
		//showLeft.addClickHandler(this);
		//showRight.addClickHandler(this);

		documentList.addChangeHandler(this);
		documentList.setVisibleItemCount(10);

		setDefaultButtons();
		initWidget(outerPanel);
		lister.getDocumentList();
	}

	/**
	 * Resets the state of the buttons and edit objects to their default.
	 * 
	 * The state of these objects is modified by requesting or obtaining locks
	 * and trying to or successfully saving.
	 */
	protected void setDefaultButtons() {
		refreshDoc.setEnabled(true);
		lockButton.setEnabled(true);
		saveButton.setEnabled(false);
		
		//docTitle.setEnabled(false);
		//docContents.setEnabled(false);
		setContentsEnabled(false);
		setTitleEnabled(false);
	}

	/**
	 * Behaves similarly to locking a document, except without a key/lock obj.
	 */
	private void createNewDocument() {
		discardExisting(null);
		lockedDoc = new LockedDocument(null, null, null,
				"Enter the document title.",
				"Enter the document contents.");
		locker.gotDoc(lockedDoc);
		History.newItem("new");
	}

	/**
	 * Returns the currently active token.
	 * 
	 * @return history token which describes the current state
	 */
	protected String getToken() {
		if (lockedDoc != null) {
			if (lockedDoc.getKey() == null) {
				return "new";
			}
			return lockedDoc.getKey();
		} else if (readOnlyDoc != null) {
			return readOnlyDoc.getKey();
		} else {
			return "list";
		}
	}

	/**
	 * Modifies the current state to reflect the supplied token.
	 * 
	 * @param args history token received
	 */
	protected void receiveArgs(String args) {
		if (args.equals("list")) 
		{
			readOnlyDoc = null;
			lockedDoc = null;

			//docTitle.setValue("");
			//docContents.setHTML("");
			docTitle = "";
			docContents = "";
			
			setDefaultButtons();
		} 
		
		else if (args.equals("new")) 
			createNewDocument();
		else 
			reader.getDocument(args);
	}

	/**
	 * Adds status lines to the console window to enable transparency of the
	 * underlying processes.
	 * 
	 * @param status the status to add to the console window
	 */
	protected void statusUpdate(String status) {
		// only keep 22 previous status updates
		while (statusArea.getWidgetCount() > 22) {
			statusArea.remove(1);
		}
		final HTML statusUpd = new HTML(status);
		statusArea.add(statusUpd);
	}

	/* (non-Javadoc)
	 * Receives button events.
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	@Override
	public void onClick(ClickEvent event) 
	{
		if (event.getSource().equals(refreshList)) 
		{
			History.newItem("list");
			lister.getDocumentList();
		} 
		else if (event.getSource().equals(createNew)) 
		{
			//documentsL.add(new VerticalPanel(), "yo");
			documents.add(new RichTextArea(), new TextBox());
			createNewDocument();
		} 
		else if (event.getSource().equals(refreshDoc)) 
		{
			if (readOnlyDoc != null)
				reader.getDocument(readOnlyDoc.getKey());
		} 
		else if (event.getSource().equals(lockButton)) 
		{
			if (readOnlyDoc != null) 
				locker.lockDocument(readOnlyDoc.getKey());
		} 
		else if (event.getSource().equals(saveButton)) 
		{
			//String currTitle = docTitle.getValue();
			//String currContents = docContents.getHTML();
			
			// currTitle is the title of the currently selected tab
			// currContents is the contents of the currently selected tab
			String currTitle = docTitle;
			String currContents = docContents;
			
			
			if (lockedDoc != null) 
			{
				if (lockedDoc.getTitle().equals(currTitle) &&
						lockedDoc.getContents().equals(currContents)) 
					statusUpdate("No document changes; not saving.");

				else 
				{
					lockedDoc.setTitle(currTitle);
					lockedDoc.setContents(currContents);				
					saver.saveDocument(lockedDoc);
				}
			}
		} 
	}


	/* (non-Javadoc)
	 * Intercepts events from the list box.
	 * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
	 */
	@Override
	public void onChange(ChangeEvent event) {
		if (event.getSource().equals(documentList)) {
			String key = documentList.getValue(documentList.getSelectedIndex());
			discardExisting(key);
			reader.getDocument(key);
		}
	}

	/**
	 * Used to release existing locks when the active document changes.
	 * 
	 * @param key the key of the new active document or null for a new document
	 */
	private void discardExisting(String key) {
		if (lockedDoc != null) {
			if (lockedDoc.getKey() == null) {
				statusUpdate("Discarding new document.");
			}
			else if (!lockedDoc.getKey().equals(key)) {
				releaser.releaseLock(lockedDoc);
			}
			else {
				// Newly active item is the currently locked item.
				return;
			}
			lockedDoc = null;
			setDefaultButtons();
		} else if (readOnlyDoc != null) {
			if (readOnlyDoc.getKey().equals(key)) 
				return;
			readOnlyDoc = null;
		}
	}

	
	
	public void addToTabPanel(String contents, String title) 
	{
		// TODO: if this doc is already open on tabpanel, dont add it
		
		boolean isAlreadyThere = false;

		// TODO
		
		// not there, so add the new tab to the panel
		RichTextArea a = new RichTextArea();
		a.setHTML(contents);
		TextBox box = new TextBox();
		box.setText(title);
		documents.add(a, box);
	}

	public void setTabPanelContents(String contents, String title) 
	{
		// set the tab bar tab title
		documents.getTabBar().insertTab(title, currTab);
		statusUpdate("title: " + title);
		
		// set the contents of the current tab
		((RichTextArea) documents.getWidget(currTab)).setHTML(contents);
	}

	public void setTitleEnabled(boolean b) 
	{
		// TODO
		// set the appropriate docTitle in the tabpanel to be enabled/disabled
		
		// how do i select a particular object within the tabbar??
	}

	public void setContentsEnabled(boolean b) 
	{
		// set the appropriate docContents in the tabpanel to be enabled/disabled
		((RichTextArea) documents.getWidget(currTab)).setEnabled(b);
	}
}
