package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.appengine.channel.client.ChannelFactory.ChannelCreatedCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.caltech.cs141b.hw2.gwt.collab.shared.AbstractDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * Main class for a single Collaborator widget.
 */
public class Collaborator extends Composite implements ClickHandler {

	// A reference to the current Collaborator object -
	// used for channel communication.
	private Collaborator self = this;

	// Length of channel ID identifier
	private static final int CLIENT_ID_LEN = 24;

	// Possible characters for channel ID
	private static final String POSS_LOGIN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

	// Constants.
	final private static int maxTabTextLen = 13;
	final private int maxConsoleEnt = 5;
	final private int maxTabsOnOneSide = 5;
	final private int maxTitleLength = 100;
	final private int maxContentsLength = 10000;
	final private int maxListItems = 15;
	final private int NOT_IN_TAB = -1;

	final private String defaultDocTitle = "Enter the document title.";
	final private String defaultDocContents = "Enter the document contents.";
	final protected String simulateDocTitle = "Simulation Document.";
	final protected String simulateDocContents = "Client IDs:\n";
	final private String disabledCSS = "Disabled";
	private static final String POSITION_MSG_PREFIX = "position: ";

	// Boolean indicating whether a simulation is taking place
	protected boolean simulation = false;

	// Simulation data.
	protected boolean simulationStopping = false;
	private int simulationTab = 0;
	private String simulationSide;
	private boolean simulateLeft = true; // is the simulate doc on L or R
	private int thinkTimeMin = 1500;
	private int thinkTimeMax = 2500;
	private int eatTimeMin = 3000;
	private int eatTimeMax = 5000;
	private int simulationWaitTimeUntilLockReq = 1000;
	private int lockTime = 35 * 1000;

	// A hashmap from document keys to timers.
	private Map<String, Timer> timerMap = new HashMap<String, Timer>();

	// Keeps track of the shared simulation document.
	protected LockedDocument simulateDoc;

	// Set the first time we open the current simulation doc, so we
	// dont open more than one.
	private boolean isSimDocAlreadyOpen;

	// The connection to the server.
	protected CollaboratorServiceAsync collabService;

	// For using the Channel API.
	protected String channelID;
	protected String clientID;

	// Track document information.
	protected ArrayList<AbstractDocument> documentsLeftList = new ArrayList<AbstractDocument>();
	protected ArrayList<AbstractDocument> documentsRightList = new ArrayList<AbstractDocument>();

	// Managing available documents.
	protected ListBox documentList = new ListBox();
	private Button refreshList = new Button("Refresh List");
	private Button createNew = new Button("New Document");

	// For displaying document information and editing document content.
	protected ArrayList<TextBox> titleL = new ArrayList<TextBox>();
	protected ArrayList<TextArea> contentsL = new ArrayList<TextArea>();
	protected ArrayList<TextBox> titleR = new ArrayList<TextBox>();
	protected ArrayList<TextArea> contentsR = new ArrayList<TextArea>();
	protected Button refreshDoc = new Button("Refresh Document");
	protected Button lockButtonL = new Button("Get Document Lock");
	protected Button saveButtonL = new Button("Save Document");
	protected Button lockButtonR = new Button("Get Document Lock");
	protected Button saveButtonR = new Button("Save Document");
	protected TabPanel documentsL = new TabPanel();
	protected TabPanel documentsR = new TabPanel();
	protected Button showButtonL = new Button("Show Left");
	protected Button showButtonR = new Button("Show Right");
	protected Button removeTabL = new Button("Remove Tab");
	protected Button removeTabR = new Button("Remove Tab");
	protected Button refreshButtonL = new Button("Refresh Doc");
	protected Button refreshButtonR = new Button("Refresh Doc");
	protected Button simulateButton = new Button("Simulate");
	protected Button stopSimulateButton = new Button("Stop simulation");

	// Panels
	VerticalPanel leftPanel = new VerticalPanel();
	VerticalPanel rightPanel = new VerticalPanel();
	HorizontalPanel leftHPanel = new HorizontalPanel();
	HorizontalPanel rightHPanel = new HorizontalPanel();

	// Buttons under the doc list
	HorizontalPanel docListButtonPanel = new HorizontalPanel();

	// Callback objects.
	protected DocReader reader = new DocReader(this);
	protected DocLister lister = new DocLister(this);
	protected DocReleaser releaser = new DocReleaser(this);
	protected DocCloser closer = new DocCloser(this);

	// Generic objects used on key handlers.
	protected TabPanel tabPanel = null;
	protected ArrayList<AbstractDocument> docList = null;
	protected ArrayList<TextArea> contentsList = null;
	protected ArrayList<TextBox> titleList = null;
	protected Button lockButton = null;
	protected Button removeTabButton = null;
	protected Button saveDocButton = null;
	protected Button refresh = null;
	protected HorizontalPanel hPanel = null;
	protected String side = null;

	// Status tracking.
	private VerticalPanel statusArea = new VerticalPanel();

	// The main outer panel where everything lives.
	private HorizontalPanel mainOuterPanel;

	// =====================================================================
	// TIMERS

	/**
	 * Calls simulateHungry() when the timer fires.
	 */
	private Timer thinkingTimer = new Timer() {
		// after the timer goes off, go to the hungry state
		public void run() {
			statusUpdate("Thinking");
			simulateHungry();
		}
	};

	/**
	 * Timer which will contact server once the max lock time has expired. Calls
	 * cleanLock().
	 */
	private void clearLock(final String docKey) {
		Timer cleanLocksTimer = new Timer() {
			public void run() {
				releaser.cleanLock(docKey);
			}
		};
		cleanLocksTimer.schedule(lockTime);

		// Add to the docKey->Timer hashmap for bookkeeping
		timerMap.put(docKey, cleanLocksTimer);
	}

	/**
	 * Calls editSimulateDoc() when the timer fires.
	 */
	private Timer eatingTimer = new Timer() {
		// when the timer goes off, we have waited some time, so can now
		// edit the doc
		public void run() {
			editSimulateDoc();
		}
	};

	/**
	 * Calls simulateHungryLock() when the timer fires.
	 */
	private Timer delayProblemTimer = new Timer() {
		// when the timer goes off, we have waited the needed time before
		// requesting the lock, so now go ahead and request it
		public void run() {
			simulateHungryLock();
		}
	};

	// END TIMERS
	// =====================================================================

	/**
	 * UI initialization.
	 * 
	 * @param collabService
	 */
	public Collaborator(CollaboratorServiceAsync collabService) {
		this.collabService = collabService;

		// initialize the UI
		initUI();

		addClickHandlersToButtons();

		// add selection handler to both tab panels
		addSelectionHandlerToTabPanel(true);
		addSelectionHandlerToTabPanel(false);

		// the list of documents
		documentList.addClickHandler(this);
		documentList.setVisibleItemCount(maxListItems);
		documentList.setHeight("100%");

		// the 'get lock' button should be initially disabled
		// since there are no tabs open
		disableButton(lockButtonL);
		disableButton(lockButtonR);

		// cant refresh doc since no docs open yet
		disableButton(refreshButtonL);
		disableButton(refreshButtonR);

		// disable the save buttons
		disableButton(saveButtonL);
		disableButton(saveButtonR);

		// disable the refresh buttons
		disableButton(removeTabL);
		disableButton(removeTabR);

		// nothing selected on list yet, so disable these
		disableButton(showButtonL);
		disableButton(showButtonR);

		initWidget(mainOuterPanel);

		Random r = new Random();
		clientID = "";

		for (int i = 0; i < CLIENT_ID_LEN; i++) {
			clientID += POSS_LOGIN_CHARS.charAt(r.nextInt(POSS_LOGIN_CHARS
					.length()));
		}

		collabService.login(clientID, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				statusUpdate("Unable to login. Please refresh the browser.");
			}

			@Override
			public void onSuccess(String result) {
				loginComplete(result);

				// this call is now made in loginComplete()
				// lister.getDocumentList();
			}

		});
	}

	/**
	 * Called after the user loads the app - when the "login" is done.
	 * 
	 * @param id
	 *            The id of the current channel.
	 */
	private void loginComplete(String id) {
		this.channelID = id;

		// defines how we receives messages from the server
		ChannelFactory.createChannel(id, new ChannelCreatedCallback() {

			@Override
			public void onChannelCreated(Channel channel) {

				// open the channel
				channel.open(new SocketListener() {

					@Override
					public void onOpen() {
						// Do nothing
					}

					/**
					 * Receives messages from the server.
					 */
					@Override
					public void onMessage(String key) {
						// Setting side to left per default
						String side = "left";
						int tabId = NOT_IN_TAB;

						// Remove whitespace characters to help identify
						// equality
						key = key.trim();

						// what if we have a message of the type position: n?
						// update this client of its place in line
						if (key.startsWith(POSITION_MSG_PREFIX))
							statusUpdate("Position in line: "
									+ key.substring(POSITION_MSG_PREFIX
											.length()));

						// our message is the actual lock
						else {
							// see if the doc is on the left tab panel
							for (int i = 0; i < documentsLeftList.size(); i++) {
								if (documentsLeftList.get(i).getKey().equals(
										key)) {
									tabId = i;
									break;
								}
							}

							// If still not found look at the right documents
							if (tabId == NOT_IN_TAB) {
								for (int i = 0; i < documentsRightList.size(); i++) {
									if (documentsRightList.get(i).getKey()
											.equals(key)) {
										tabId = i;
										side = "right";
										break;
									}
								}

							}

							// if we found the doc on either the right or left
							// tab panel
							if (tabId != NOT_IN_TAB) {
								statusUpdate("Locked reader.");

								// get the locked document from the datastore
								DocLockedReader.getLockedDoc(self, key, side,
										tabId);

								// Add a timer to report back to server after
								// the time has expired.
								clearLock(key);
							}

							else
								statusUpdate("Lock acquired for closed document.");
						}
					}

					@Override
					public void onClose() {
						// Do nothing
					}

					@Override
					public void onError(SocketError error) {
						statusUpdate("Error: " + error.getDescription());

					}
				});
			}
		});

		// refresh document list on login
		lister.getDocumentList();
		
		Window.addWindowClosingHandler(new ClosingHandler() {
			/*@Override
			public void onClose(CloseEvent<Window> event) {
				

			}*/

			@Override
			public void onWindowClosing(ClosingEvent event) {
				// TODO Auto-generated method stub
				System.out.println("closing");
				DocLogout logout = new DocLogout(self);
				logout.logout();
			}
		});
	}

	/**
	 * Initialize the UI.
	 */
	private void initUI() {
		// the main outer panel - holds everything
		mainOuterPanel = new HorizontalPanel();
		mainOuterPanel.setStyleName("mainOuterPanel");
		mainOuterPanel.setWidth("100%");
		mainOuterPanel.setHeight("100%");

		// console
		statusArea.setStyleName("statusArea");

		// left side - the doc list and the console
		VerticalPanel docsAndConsoleVertPanel = new VerticalPanel();
		docsAndConsoleVertPanel.setStyleName("docsAndConsoleVertPanel");
		docsAndConsoleVertPanel.setSpacing(20);

		// list of docs
		VerticalPanel docListPanel = new VerticalPanel();
		docListPanel.setStyleName("docListPanel");
		docListPanel.setHeight("100%");
		docListPanel.setSpacing(10);

		HTML docListPanelTitle = new HTML("<h2>Available Documents</h2>");
		docListPanel.add(docListPanelTitle);
		documentList.setWidth("100%");
		docListPanel.add(documentList);
		docListPanel.setWidth("350px");

		// button styles
		refreshDoc.setStylePrimaryName("refreshButton");
		lockButtonL.setStylePrimaryName("lockButton");
		saveButtonL.setStylePrimaryName("saveButton");
		lockButtonR.setStylePrimaryName("lockButton");
		saveButtonR.setStylePrimaryName("saveButton");
		showButtonL.setStylePrimaryName("showLButton");
		showButtonR.setStylePrimaryName("showRButton");
		removeTabL.setStylePrimaryName("removeButton");
		removeTabR.setStylePrimaryName("removeButton");
		refreshButtonL.setStylePrimaryName("refreshButton");
		refreshButtonR.setStylePrimaryName("refreshButton");
		createNew.setStylePrimaryName("createNewButton");
		refreshList.setStylePrimaryName("refreshButton");
		simulateButton.setStylePrimaryName("simulateButton");
		stopSimulateButton.setStylePrimaryName("stopSimulateButton");

		// add button tooltips
		refreshDoc.setTitle("Refresh the doc list.");
		lockButtonL.setTitle("Start editing this document.");
		saveButtonL.setTitle("Save the changes to this document.");
		lockButtonR.setTitle("Start editing this document.");
		saveButtonR.setTitle("Save the changes to this document.");
		showButtonL.setTitle("Display on the left tab panel.");
		showButtonR.setTitle("Display on the right tab panel.");
		removeTabL.setTitle("Remove from this panel.");
		removeTabR.setTitle("Remove from this panel.");
		refreshButtonL.setTitle("Refresh this document.");
		refreshButtonR.setTitle("Refresh this document.");
		createNew.setTitle("Create a new document.");
		refreshList.setTitle("Refresh the documents list.");
		simulateButton.setTitle("Start the simulation.");

		refreshDoc.addStyleName("gwt-Button");
		lockButtonL.addStyleName("gwt-Button");
		saveButtonL.addStyleName("gwt-Button");
		lockButtonR.addStyleName("gwt-Button");
		saveButtonR.addStyleName("gwt-Button");
		showButtonL.addStyleName("gwt-Button");
		showButtonR.addStyleName("gwt-Button");
		removeTabL.addStyleName("gwt-Button");
		removeTabR.addStyleName("gwt-Button");
		refreshButtonL.addStyleName("gwt-Button");
		refreshButtonR.addStyleName("gwt-Button");
		createNew.addStyleName("gwt-Button");
		refreshList.addStyleName("gwt-Button");
		simulateButton.addStyleName("gwt-Button");
		stopSimulateButton.addStyleName("gwt-Button");

		// buttons under the doc list
		docListButtonPanel.setSpacing(10);
		docListButtonPanel.add(refreshList);
		docListButtonPanel.add(createNew);
		docListButtonPanel.add(showButtonL);
		docListButtonPanel.add(showButtonR);
		docListButtonPanel.add(simulateButton);
		docListPanel.add(docListButtonPanel);
		DecoratorPanel dp = new DecoratorPanel();
		dp.setWidth("100%");
		dp.add(docListPanel);
		docsAndConsoleVertPanel.add(dp);

		VerticalPanel consoleDP = new VerticalPanel();
		consoleDP.setStyleName("consoleDP");
		consoleDP.setWidth("330px");
		consoleDP.setHeight("250px");
		HTML consoleTitle = new HTML("<h2>Console</h2>");
		statusArea.setSpacing(10);
		statusArea.add(consoleTitle);
		statusArea.setCellWidth(consoleTitle, "100%");
		statusArea.setWidth("100%");
		consoleDP.add(statusArea);

		consoleDP.setCellVerticalAlignment(statusArea,
				HasVerticalAlignment.ALIGN_TOP);

		docsAndConsoleVertPanel.add(consoleDP);
		mainOuterPanel.add(docsAndConsoleVertPanel);

		// right side - open docs
		VerticalPanel openDocsOuterPanel = new VerticalPanel();
		openDocsOuterPanel.setStyleName("openDocsOuterPanel");
		openDocsOuterPanel.setSpacing(20);
		HorizontalPanel openDocsDP = new HorizontalPanel();
		openDocsDP.setStyleName("openDocsDP");
		openDocsDP.setWidth("100%");

		VerticalPanel openDocsInnerPanel = new VerticalPanel();
		openDocsInnerPanel.setStyleName("openDocsInnerPanel");
		HTML openDocumentsText = new HTML("<h2>Open Documents</h2>");
		openDocsInnerPanel.add(openDocumentsText);
		openDocsInnerPanel.setCellHeight(openDocumentsText, "2em");

		// holds the left tab panel
		HorizontalPanel innerHp = new HorizontalPanel();
		innerHp.setSpacing(10);
		leftPanel.add(documentsL);

		// holds the buttons for the left tab panel
		// initially add the save, refresh, and removeTab buttons
		// leftHPanel.add(lockButtonL);
		leftHPanel.add(saveButtonL);
		leftHPanel.add(refreshButtonL);
		leftHPanel.add(removeTabL);

		leftPanel.add(leftHPanel);

		// holds the right tab panel
		rightPanel.add(documentsR);

		// holds the buttons for the right tab panel
		// rightHPanel.add(lockButtonR);
		rightHPanel.add(saveButtonR);
		rightHPanel.add(refreshButtonR);
		rightHPanel.add(removeTabR);

		rightPanel.add(rightHPanel);

		innerHp.add(leftPanel);
		innerHp.add(rightPanel);

		openDocsInnerPanel.add(innerHp);

		openDocsDP.add(openDocsInnerPanel);
		openDocsOuterPanel.add(openDocsDP);
		mainOuterPanel.add(openDocsOuterPanel);

		// Divide up the horizontal space
		mainOuterPanel.setWidth("100%");
		mainOuterPanel.setCellWidth(docsAndConsoleVertPanel, "200px");
		mainOuterPanel.setCellWidth(openDocsOuterPanel, "100%");

		openDocsOuterPanel.setWidth("100%");
		innerHp.setCellWidth(leftPanel, "50%");
		innerHp.setCellWidth(rightPanel, "50%");

		innerHp.setWidth("100%");
		innerHp.setHeight("100%");

		// Fixing the vertical
		mainOuterPanel.setCellHeight(docsAndConsoleVertPanel, "100%");

		innerHp.setCellHeight(leftPanel, "100%");
		innerHp.setCellHeight(rightPanel, "100%");

		// Vertical textboxes
		leftPanel.setCellHeight(documentsL, "100%");
		rightPanel.setCellHeight(documentsR, "100%");

		// Setting up the document sizes
		// the panels

		openDocsDP.setCellVerticalAlignment(openDocsInnerPanel,
				HasAlignment.ALIGN_TOP);
		openDocsDP.setHeight("100%");
		openDocsOuterPanel.setHeight("100%");
		openDocsInnerPanel.setHeight("100%");

		leftPanel.setStyleName("leftPanel");
		rightPanel.setStyleName("rightPanel");
		leftPanel.setWidth("100%");
		leftPanel.setHeight("100%");
		rightPanel.setWidth("100%");
		rightPanel.setHeight("100%");

		// fixing space issues
		leftPanel.setCellHeight(leftHPanel, "30px");
		rightPanel.setCellHeight(rightHPanel, "30px");

		// Setting console/document space
		docsAndConsoleVertPanel.setCellHeight(consoleDP, "200px");

		// Tab bars
		documentsL.setWidth("100%");
		documentsR.setWidth("100%");
	}

	/**
	 * Shorten 'title' so that it is of length 'len' - 3, with ... at the end.
	 * Used for displaying doc titles in the tab panel.
	 * 
	 * @param title
	 * @param len
	 * @return
	 */
	public static String shortenText(String title, int len) {
		if (title.length() > len)
			title = title.substring(0, len - 3) + "...";

		return title;
	}

	/**
	 * Add a selection handler to the tab panel. This allows us to refresh the
	 * doc title in the tab name.
	 * 
	 * @param left
	 */
	private void addSelectionHandlerToTabPanel(boolean left) {
		setGenericObjects(left);

		final TabPanel tabPanelFinal = tabPanel;
		final ArrayList<AbstractDocument> docListFinal = docList;
		final ArrayList<TextArea> contentsListFinal = contentsList;
		final ArrayList<TextBox> titleListFinal = titleList;
		final Button lockButtonFinal = lockButton;
		final Button removeTabButtonFinal = removeTabButton;
		final Button refreshFinal = refresh;
		final HorizontalPanel hPanelFinal = hPanel;
		final Button saveDocButtonFinal = saveDocButton;

		tabPanelFinal.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				int ind = tabPanelFinal.getTabBar().getSelectedTab();

				hPanelFinal.clear();

				if (docListFinal.get(ind) instanceof LockedDocument) {
					// enable and add the save button
					enableButton(saveDocButtonFinal);
					hPanelFinal.add(saveDocButtonFinal);

					// Enable the fields since have the lock
					titleListFinal.get(ind).setEnabled(true);
					contentsListFinal.get(ind).setEnabled(true);

					// disable the refresh button
					disableButton(refreshFinal);
				} else {
					// enable and add the lock button
					enableButton(lockButtonFinal);
					hPanelFinal.add(lockButtonFinal);

					// Disabling the fields since you don't have the lock
					titleListFinal.get(ind).setEnabled(false);
					contentsListFinal.get(ind).setEnabled(false);

					// enable the refresh button
					enableButton(refreshFinal);
				}

				// add removeTab and refresh buttons, enable removeTab
				enableButton(removeTabButtonFinal);
				hPanelFinal.add(refreshFinal);
				hPanelFinal.add(removeTabButtonFinal);
			}
		});
	}

	/**
	 * Add click handlers to our buttons.
	 */
	private void addClickHandlersToButtons() {
		refreshList.addClickHandler(this);
		createNew.addClickHandler(this);
		lockButtonL.addClickHandler(this);
		saveButtonL.addClickHandler(this);
		lockButtonR.addClickHandler(this);
		saveButtonR.addClickHandler(this);
		showButtonL.addClickHandler(this);
		showButtonR.addClickHandler(this);
		removeTabR.addClickHandler(this);
		removeTabL.addClickHandler(this);
		refreshButtonL.addClickHandler(this);
		refreshButtonR.addClickHandler(this);
		simulateButton.addClickHandler(this);
		stopSimulateButton.addClickHandler(this);
	}

	/**
	 * Adds status lines to the console window to enable transparency of the
	 * underlying processes.
	 * 
	 * @param status
	 *            the status to add to the console window
	 */
	protected void statusUpdate(String status) {
		while (statusArea.getWidgetCount() > maxConsoleEnt)
			statusArea.remove(1);

		final HTML statusUpd = new HTML(status);
		statusArea.add(statusUpd);
	}

	/**
	 * Sets the generic private objects to the objects of the correct side. This
	 * simply specifies the objects based on their side (since there are a lot
	 * of things that are the same for the left and the right).
	 * 
	 * @param left
	 */
	protected void setGenericObjects(boolean left) {
		if (left) {
			tabPanel = documentsL;
			docList = documentsLeftList;
			contentsList = contentsL;
			titleList = titleL;
			lockButton = lockButtonL;
			removeTabButton = removeTabL;
			saveDocButton = saveButtonL;
			refresh = refreshButtonL;
			hPanel = leftHPanel;
			side = "left";
		} else {
			tabPanel = documentsR;
			docList = documentsRightList;
			contentsList = contentsR;
			titleList = titleR;
			lockButton = lockButtonR;
			removeTabButton = removeTabR;
			saveDocButton = saveButtonR;
			refresh = refreshButtonR;
			hPanel = rightHPanel;
			side = "right";
		}
	}

	/**
	 * Set the text (title) of the specified tab.
	 * 
	 * @param text
	 * @param ind
	 * @param side
	 */
	private void setTabText(String text, int ind, String side) {
		if (side.equals("left"))
			documentsL.getTabBar().setTabText(ind,
					shortenText(text, maxTabTextLen));
		else if (side.equals("right"))
			documentsR.getTabBar().setTabText(ind,
					shortenText(text, maxTabTextLen));
	}

	/**
	 * Adds a tab to either the left or the right tab panel.
	 * 
	 * @param title
	 * @param content
	 * @param left
	 */
	public void addTab(final String title, String content, boolean left) {
		// are we dealing with a new doc?
		final boolean isNewDoc = title.equals(defaultDocTitle)
				&& content.equals(defaultDocContents);

		// holds the title and the contents
		VerticalPanel vp = new VerticalPanel();
		// vp.setSpacing(5);

		// the document title
		final TextBox titleBox = new TextBox();
		titleBox.setValue(title);
		titleBox.setEnabled(true);
		titleBox.setWidth("100%");
		titleBox.setStyleName("titleBox");

		// prevent spacing issues
		// titleBox.setHeight("1.2em");

		// the document contents
		final TextArea areaBox = new TextArea();
		areaBox.setWidth("99%");
		areaBox.setHeight("100%");
		areaBox.setStyleName("documentTextBox");

		titleBox.setText(title);
		areaBox.setText(content);

		if (simulation) {
			// titleBox.setEnabled(false);
			// areaBox.setEnabled(false);
		} else {
			titleBox.setEnabled(true);
			areaBox.setEnabled(true);
		}

		// if user hits the 'enter' key anywhere in the title box, move cursor
		// to the end of the contents box
		titleBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				// if user hits the 'enter' key
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					areaBox.setCursorPos(areaBox.getText().length());
					areaBox.setFocus(true);

					// if we are dealing with a new doc (or one that has the
					// default title and contents,
					// automatically select all the text in the contents
					if (isNewDoc)
						areaBox.selectAll();
				}
			}
		});

		vp.add(titleBox);
		vp.add(areaBox);
		vp.setCellHeight(titleBox, "20px");

		// Centering the title box
		// vp.setCellHorizontalAlignment(titleBox,
		// HasHorizontalAlignment.ALIGN_CENTER);

		setGenericObjects(left);

		// enable the lock, removeTab, save, and refresh buttons on the
		// correct tab panel
		enableButton(lockButton);
		enableButton(removeTabButton);
		enableButton(saveDocButton);
		enableButton(refresh);

		final int ind = titleList.size();

		// set a value-change handler to the title box (so that it updates even
		// when user pastes stuff to it)
		titleBox.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setTabText(titleL.get(ind).getText(), ind, side);
			}

		});

		// add key handler to the title box - update the tab text
		// as the user is typing the title
		titleBox.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				setTabText(titleList.get(ind).getText(), ind, side);
			}
		});

		// add the title and contents to the lists for bookkeeping
		titleList.add(titleBox);
		contentsList.add(areaBox);

		// add the doc to the correct tab panel
		tabPanel.add(vp, shortenText(title, maxTabTextLen));

		// how many tabs do we have open?
		int numLeftTabs = documentsL.getTabBar().getTabCount();
		int numRightTabs = documentsR.getTabBar().getTabCount();

		// if we have space for new doc, enable the button
		if (numLeftTabs < maxTabsOnOneSide && numRightTabs < maxTabsOnOneSide)
			enableButton(createNew);
		else if (numLeftTabs >= maxTabsOnOneSide
				&& numRightTabs >= maxTabsOnOneSide)
			disableButton(createNew);

		// can we add more tabs on the left?
		if (numLeftTabs < maxTabsOnOneSide)
			enableButton(showButtonL);
		else
			disableButton(showButtonL);

		// can we add more tabs on the right?
		if (numRightTabs < maxTabsOnOneSide)
			enableButton(showButtonR);
		else
			disableButton(showButtonR);
	}

	/**
	 * Receives button events.
	 * 
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick
	 *      (com.google.gwt.event.dom.client.ClickEvent)
	 */
	@Override
	public void onClick(ClickEvent event) {
		// pressed 'refresh document list' button
		if (event.getSource().equals(refreshList))
			lister.getDocumentList();

		// pressed 'new doc' button
		else if (event.getSource().equals(createNew)) {
			if (documentsL.getTabBar().getTabCount() < maxTabsOnOneSide)
				createNewDocumentButtonHandler("left");
			else if (documentsR.getTabBar().getTabCount() < maxTabsOnOneSide)
				createNewDocumentButtonHandler("right");
		}

		// pressed left 'get lock' button
		else if (event.getSource().equals(lockButtonL))
			lockDocumentButtonHandler(true);

		// pressed right 'get lock' button
		else if (event.getSource().equals(lockButtonR))
			lockDocumentButtonHandler(false);

		// pressed left 'save doc' button
		else if (event.getSource().equals(saveButtonL))
			saveDocumentButtonHandler(true);

		// pressed right 'save doc' button
		else if (event.getSource().equals(saveButtonR))
			saveDocumentButtonHandler(false);

		// if show left is pressed, add doc to the left tab panel
		else if (event.getSource().equals(showButtonL))
			showDocumentButtonHandler(true);

		// if show right is pressed, add doc to the right tab panel
		else if (event.getSource().equals(showButtonR))
			showDocumentButtonHandler(false);

		// if user wants to remove current tab on left
		else if (event.getSource().equals(removeTabL))
			removeTabButtonHandler(true);

		// if user wants to remove current tab on right
		else if (event.getSource().equals(removeTabR))
			removeTabButtonHandler(false);

		else if (event.getSource().equals(refreshButtonL))
			refreshButtonHandler(true);

		else if (event.getSource().equals(refreshButtonR))
			refreshButtonHandler(false);

		// if user selects a doc from the doc list
		else if (event.getSource().equals(documentList))
			docListHandler();

		// if user presses the simulate button
		else if (event.getSource().equals(simulateButton))
			simulateButtonHandler();

		// if user presses the stop simulate button
		else if (event.getSource().equals(stopSimulateButton))
			stopSimulateButtonHandler();
	}

	/**
	 * Behaves similarly to locking a document, except without a key/lock obj.
	 */
	private void createNewDocumentButtonHandler(String side) {
		LockedDocument ld = new LockedDocument(null, null, null,
				defaultDocTitle, defaultDocContents);

		// keep track of which side we are on
		boolean left = false;
		if (side.equals("left"))
			left = true;
		else
			left = false;

		setGenericObjects(left);

		docList.add(ld);
		addTab(ld.getTitle(), ld.getContents(), left);
		setTabText(ld.getTitle(), docList.size() - 1, side);
		openLatestTab(side);

		// when a new doc is opened, set the cursor to the title
		TextBox title = titleList.get(titleList.size() - 1);
		title.setCursorPos(title.getText().length());
		title.setFocus(true);
		title.selectAll();

		// add save, refresh, and removeTab buttons
		hPanel.clear();
		hPanel.add(saveDocButton);
		hPanel.add(refresh);
		hPanel.add(removeTabButton);

		// buttons on the correct tab panel
		enableButton(saveDocButton);
		disableButton(refresh);
		enableButton(removeTabButton);

		// buttons under the doc list
		disableButton(showButtonL);
		disableButton(showButtonR);
		disableButton(simulateButton);
	}

	/**
	 * Opens the latest-opened tab on the appropriate tabpanel.
	 * 
	 * @param side
	 */
	public void openLatestTab(String side) {
		int last;
		if (side.equals("left")) {
			last = documentsL.getTabBar().getTabCount() - 1;
			documentsL.getTabBar().selectTab(last);
		} else {
			last = documentsR.getTabBar().getTabCount() - 1;
			documentsR.getTabBar().selectTab(last);
		}

		// Record the tab number if the simulation is running
		if (simulation)
			simulationTab = last;
	}

	/**
	 * Handler for the 'refresh doc' button.
	 * 
	 * @param left
	 */
	private void refreshButtonHandler(boolean left) {
		setGenericObjects(left);

		// get the currently-displayed doc
		int index = tabPanel.getTabBar().getSelectedTab();
		AbstractDocument currDoc = docList.get(index);

		if (currDoc != null && currDoc.getKey() != null)
			DocReader.readDoc(this, currDoc.getKey(), side, docList.size() - 1);
	}

	/**
	 * Remove a tab from the correct side. Called after user presses either
	 * right or left removeTab button. Also remove this client from the server's
	 * wait queue for this document.
	 * 
	 * @param left
	 */
	private void removeTabButtonHandler(boolean left) {
		setGenericObjects(left);

		int ind = tabPanel.getTabBar().getSelectedTab();

		// get the doc that we are removing from the tabpanel
		AbstractDocument currDoc = docList.get(ind);

		// remove this client from the server's waiting queue associated
		// with this doc
		if (currDoc.getKey() != null && currDoc instanceof LockedDocument) {
			closer.removeFromServerQueue(currDoc.getKey());
			releaser.releaseLock((LockedDocument) currDoc);
		}

		// do some cleanup
		tabPanel.remove(ind);
		docList.remove(ind);
		contentsList.remove(ind);
		titleList.remove(ind);

		// if we have another open tab before the deleted one
		if (ind > 0) {
			hPanel.clear();

			// select the previous tab
			tabPanel.selectTab(ind - 1);

			// if the title (and contents) of the prev tab is non-editable,
			// then add 'lock', 'removeTab', and 'refresh' buttons
			if (!titleList.get(ind - 1).isEnabled()) {
				hPanel.add(lockButton);
				enableButton(lockButton);
				enableButton(refresh);
			}
			// title and contents are editable, so add 'save', 'remove', and
			// 'refresh' buttons (refresh must be disabled)
			else {
				hPanel.add(saveDocButton);
				enableButton(saveDocButton);
				disableButton(refresh);
			}

			hPanel.add(refresh);
			hPanel.add(removeTabButton);
			enableButton(removeTabButton);
		}

		// otherwise, if this tab has no tabs to its left
		else {
			int numTabsLeft = tabPanel.getTabBar().getTabCount();

			// if we still have tabs left (on the right)
			if (numTabsLeft > 0) {
				// select the next tab to the right (the new first tab)
				tabPanel.selectTab(0);

				hPanel.clear();

				// if the title (and contents) of the next tab is non-editable,
				// then add 'lock', 'removeTab', and 'refresh' buttons
				if (!titleList.get(0).isEnabled()) {
					hPanel.add(lockButton);
					enableButton(lockButton);
					enableButton(refresh);
				}

				// title and contents are editable, so add 'save', 'remove', and
				// 'refresh' buttons (refresh must be disabled)
				else {
					hPanel.add(saveDocButton);
					enableButton(saveDocButton);
					disableButton(refresh);
				}

				hPanel.add(refresh);
				hPanel.add(removeTabButton);
				enableButton(removeTabButton);
			}

			// if no longer have any tabs on the left, disable all buttons
			else {
				for (Widget w : hPanel)
					disableButton((Button) w);
			}
		}

		// enable 'new doc' button
		enableButton(createNew);

		// user cannot start the simulation at this point
		disableButton(simulateButton);
	}

	/**
	 * Called after user presses either right or left 'save doc' button.
	 * 
	 * @param left
	 */
	private void saveDocumentButtonHandler(boolean left) {
		setGenericObjects(left);

		int ind = tabPanel.getTabBar().getSelectedTab();
		AbstractDocument doc = docList.get(ind);

		// if we can save this document
		if (doc instanceof LockedDocument) {
			// if title and contents have not been changed, no need to save
			if (doc.getTitle().equals(titleList.get(ind).getValue())
					&& doc.getContents()
							.equals(contentsList.get(ind).getText()))
				statusUpdate("No document changes; not saving.");

			// otherwise if stuff was changed, save
			else {
				LockedDocument ld = (LockedDocument) doc;
				String title = titleList.get(ind).getText();
				String contents = contentsList.get(ind).getText();

				// if the title and contents are less than the max length,
				// then save this doc
				if (title.length() < maxTitleLength
						&& contents.length() < maxContentsLength) {
					ld.setTitle(title);
					ld.setContents(contents);
					DocSaver.saveDoc(this, ld, side, ind);
				}

				// otherwise, print error messages to console

				else if (contents.length() >= maxContentsLength)
					statusUpdate("Error: Can't save; contents must be less than "
							+ maxContentsLength + " characters.");

				else if (title.length() >= maxTitleLength)
					statusUpdate("Error: Can't save; title must be less than "
							+ maxTitleLength + " characters.");
			}
		}
	}

	/**
	 * Called after user presses either right of left 'lock doc' button.
	 * 
	 * @param left
	 */
	private void lockDocumentButtonHandler(boolean left) {
		setGenericObjects(left);

		// get the index of the selected tab on the correct tabpanel
		int ind = tabPanel.getTabBar().getSelectedTab();

		// get the selected doc
		AbstractDocument doc = docList.get(ind);

		// Lock only if it can be locked.
		// this call can result in either success or failure, both of
		// which are taken care of in DocLocker
		if (doc instanceof UnlockedDocument)
			DocLocker.lockDoc(this, doc.getKey());

		disableButton(lockButton);

		// if in simulation, disable simulate button and the doc title and
		// contents
		if (simulation) {
			disableButton(simulateButton);

			// user cannot edit the doc while simulation is running
			titleList.get(ind).setEnabled(false);
			contentsList.get(ind).setEnabled(false);
		}
	}

	/**
	 * Called after user presses either right of left 'show' button.
	 * 
	 * @param left
	 */
	private void showDocumentButtonHandler(boolean left) {
		setGenericObjects(left);

		String key = documentList.getValue(documentList.getSelectedIndex());

		// if we arent already showing this doc, add it to the panel
		if (!contained(key, documentsLeftList, documentsRightList))
			openDocument(side);

		// this is already up on the tabpanels, so disable these buttons
		disableButton(showButtonL);
		disableButton(showButtonR);
	}

	/**
	 * Opens a new document on the tab of the given side and puts it in focus.
	 * 
	 * @param String
	 *            side: Side to open new document in, "left" or "right".
	 */
	public void openDocument(String side) {
		int docIndx = documentList.getSelectedIndex();
		String title = documentList.getItemText(docIndx);
		String key = documentList.getValue(docIndx);

		if (side.equals("left")) {
			documentsLeftList.add(null);
			addTab(title, "", true);
			DocReader.readDoc(this, key, "left", documentsLeftList.size() - 1);
		} else {
			documentsRightList.add(null);
			addTab(title, "", false);
			DocReader
					.readDoc(this, key, "right", documentsRightList.size() - 1);
		}

		openLatestTab(side);
	}

	/**
	 * Called when the user selects a doc from the doc list.
	 */
	private void docListHandler() {
		// if we selected something valid in the doc list
		if (documentList.getSelectedIndex() >= 0) {
			String key = documentList.getValue(documentList.getSelectedIndex());

			// if not already showing this doc, disable showLeft + showRight
			if (contained(key, documentsLeftList, documentsRightList)) {
				disableButton(showButtonL);
				disableButton(showButtonR);
			} else {
				enableButton(showButtonL);
				enableButton(showButtonR);
			}

			// disable show left or right based on how many tabs are open
			int numLeftTabs = documentsL.getTabBar().getTabCount();
			int numRightTabs = documentsR.getTabBar().getTabCount();

			// disable show left and show right appropriately
			if (numLeftTabs >= maxTabsOnOneSide)
				disableButton(showButtonL);
			if (numRightTabs >= maxTabsOnOneSide)
				disableButton(showButtonR);

			// disable new doc if no more space anywhere
			if (numLeftTabs >= maxTabsOnOneSide
					&& numRightTabs >= maxTabsOnOneSide) {
				disableButton(createNew);
				statusUpdate("No more space on the tab panels!");
			}

			// if simulation is not running or not stopping
			if (!simulation && !simulationStopping) {
				// Replace stop-simulate button with simulate button
				docListButtonPanel.remove(stopSimulateButton);
				docListButtonPanel.add(simulateButton);

				// enable simulate button
				enableButton(simulateButton);
			}
		}
	}

	// ===================================================================
	// SIMULATION METHODS

	/**
	 * Called when the user presses the stop simulate button.
	 */
	private void stopSimulateButtonHandler() {
		// Terminate the simulation
		simulation = false;
		simulationStopping = true;

		// cannot disable again, and also dont add simulate button yet since
		// stuff can still go on, and when that finishes (for this client only),
		// then add the sim button so this client can enter the simulation again
		disableButton(stopSimulateButton);
		
		statusUpdate("Stopping the simulation...");

		// since simulation is set to false, once the thinking/hungry/eating
		// phases we started finish then we need to enable some buttons
		// (code is in editSimulateDoc())
	}

	/**
	 * Called when the user presses the simulate button.
	 */
	private void simulateButtonHandler() {
		// only allow user to press this button when a doc is selected
		// when button is pressed, open the selected doc

		// Start the simulation
		simulation = true;
		simulationStopping = false;

		// Replace simulate button with stop simulate button
		docListButtonPanel.remove(simulateButton);
		docListButtonPanel.add(stopSimulateButton);

		// user can't do anything in simulation mode
		lockDownUI();

		// start thinking (wait for a random time and then become hungry
		// aka request lock)
		simulateThinking();
	}

	/**
	 * Waits for a random thinking time and then becomes hungry.
	 */
	private void simulateThinking() {

		// Generate an int between and thinkTimeMin up to but not including
		// thinkTimeMax
		int thinkTime = thinkTimeMin
				+ com.google.gwt.user.client.Random.nextInt(thinkTimeMax
						- thinkTimeMin);

		// disable any possibly running timer
		thinkingTimer.cancel();

		// wait for the thinking time and then call simulateHungry()
		thinkingTimer.schedule(thinkTime);

	}

	/**
	 * Called after the client is done thinking.
	 */
	private void simulateHungry() {
		statusUpdate("Hungry");

		// at this point the correct doc is selected by the user since simulate
		// button can only be pressed after user selects a doc from the doc list

		// check if the current simulation doc is already open
		// in a tab, and if so, dont open it again.
		// for this, keep a boolean variable that we set once we open up the sim
		// doc for the first time.

		// if we havent already opened a simulation doc for this client
		if (!isSimDocAlreadyOpen) {
			isSimDocAlreadyOpen = true;

			// figure out which side to open the simulate doc on
			if (documentsL.getTabBar().getTabCount() < maxTabsOnOneSide)
				simulateLeft = true;
			else if (documentsR.getTabBar().getTabCount() < maxTabsOnOneSide)
				simulateLeft = false;
			else {
				statusUpdate("Tabs panels are full...");
				return;
			}

			// act as if the user pressed the show right or left button
			// since we want to display the simulate doc to the user
			showDocumentButtonHandler(simulateLeft);
		}

		// disable the UI since simulation is still going
		lockDownUI();

		// wait for a set time (simulationWaitTimeUntilLockReq) and then
		// call simulateHungryLock() which acts as if the user pressed the
		// correct lock doc button for our simulate doc
		delayProblemTimer.schedule(simulationWaitTimeUntilLockReq);
	}

	/**
	 * Called at the end of the hungry phase - after the client waits for
	 * simulationWwaitTimeUntilLockReq time, request the lock for the simulation
	 * doc. simulateLeft will tell us if the simulate doc is on the left or the
	 * right.
	 */
	private void simulateHungryLock() {
		// request the lock for the simulation doc
		lockDocumentButtonHandler(simulateLeft);

		// disable the UI since simulation is still going
		lockDownUI();

		// at this point this client has requested the lock for the simulate
		// doc.
		// when the lock is acquired the DocLockedReader class calls the
		// simulateEating()
		// method to simulate the eating phase.
	}

	/**
	 * Called by DocLockedReader's onSuccess() method. This code runs after this
	 * client has acquired the lock for the simulate doc.
	 * 
	 * @param doc
	 *            The actual simulate document
	 * @param index
	 *            Where on the tabPanel this doc is
	 * @param side
	 *            Which side is this doc on?
	 */
	public void simulateEating(LockedDocument doc, int index, String side) {
		statusUpdate("Eating");

		simulateDoc = doc;
		simulationTab = index;
		simulationSide = side;

		// append this client ID to the contents of this simulation doc
		simulateDoc.setContents(simulateDoc.getContents() + "\nClient: "
				+ clientID);

		// Wait random time before editing the doc
		int eatTime = eatTimeMin
				+ com.google.gwt.user.client.Random.nextInt(eatTimeMax
						- eatTimeMin);

		// simulates the actual "eating" - wait for a random time and then call
		// editSimulateDoc()
		eatingTimer.schedule(eatTime);
	}

	/**
	 * Called when the eatingTimer goes off - we have waited the appropriate
	 * time and can now edit the simulation doc.
	 */
	protected void editSimulateDoc() {
		// save this simulation doc so that other clients can see its updates
		DocSaver.saveDoc(this, simulateDoc, simulationSide, simulationTab);
		// saveDocumentButtonHandler(simulateLeft);

		// Simulation still running? If so start thinking again. But we cannot
		// do this here since we must make sure the doc is saved before we can
		// continue. Thus, do this in setDoc (called by DocSaver).
	}

	/**
	 * Called by setDoc() after saveDoc - after the eating phase. Either
	 * continues the simulation or
	 * 
	 * @param doc
	 */
	protected void simulationDone() {
		// if we are still in simulation mode, start thinking again
		if (simulation) {
			lockDownUI();

			int thinkTime = thinkTimeMin
					+ com.google.gwt.user.client.Random.nextInt(thinkTimeMax
							- thinkTimeMin);

			// after we wait for thinkTime, simulateHungry() is called, and the
			// entire simulation process repeats
			thinkingTimer.schedule(thinkTime);
		}

		// simulation is done
		else {
			// no longer in stopping mode
			simulationStopping = false;

			statusUpdate("Simulation done.");
			
			// refresh the client page
			Window.Location.reload();
		}
	}

	// END SIMULATION METHODS
	// ===================================================================

	/**
	 * Returns true of key is in either of the lists, false otherwise.
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	private boolean contained(String key, ArrayList<AbstractDocument> list1,
			ArrayList<AbstractDocument> list2) {

		if (list1 == null || list2 == null)
			return false;

		boolean contains = false;

		for (AbstractDocument doc1 : list1) {
			if (doc1 != null && doc1.getKey() != null) {
				if (doc1.getKey().equals(key))
					contains = true;
			}
		}

		for (AbstractDocument doc2 : list2) {
			if (doc2 != null && doc2.getKey() != null) {
				if (doc2.getKey().equals(key))
					contains = true;
			}
		}

		return contains;
	}

	/**
	 * Generalized so that it can be called elsewhere. In particular, after a
	 * document is saved, it calls this function to simulate an initial reading
	 * of a document.
	 * 
	 * Called by docsaver (onFailure and onSuccess) and docreader (onSuccess).
	 * 
	 * @param doc
	 *            the unlocked doc that should be displayed
	 */
	protected void setDoc(AbstractDocument doc, int index, String side) {
		// set the tab text to the doc title
		setTabText(doc.getTitle(), index, side);

		if (side.equals("left"))
			setGenericObjects(true);
		else
			setGenericObjects(false);

		docList.set(index, doc);

		// set the title and contents to be the most updated stuff
		// from the input fields
		titleList.get(index).setValue(doc.getTitle());
		contentsList.get(index).setValue(doc.getContents());

		// add lock, remove tab, and refresh buttons
		hPanel.clear();

		if (doc instanceof UnlockedDocument) {
			hPanel.add(lockButton);

			// if simulation is still running or is finishing up, then disable
			// buttons
			if (simulation || simulationStopping) {
				disableButton(lockButton);
				disableButton(refresh);
			} else {
				enableButton(lockButton);
				enableButton(refresh);
			}

			// title and contents cannot be edited
			titleList.get(index).setEnabled(false);
			contentsList.get(index).setEnabled(false);
			String key = doc.getKey();

			// Cancel and remove the timer if there was one before the save was
			// pressed.
			if (timerMap.containsKey(key)) {
				timerMap.get(key).cancel();
				timerMap.remove(key);
			}

		}
		// we have a locked doc
		else {
			hPanel.add(saveDocButton);

			// if simulation is still running or is finishing up, then disable
			// buttons
			if (simulation || simulationStopping)
				disableButton(saveDocButton);
			else
				enableButton(saveDocButton);

			// title and contents can now be edited
			titleList.get(index).setEnabled(true);
			contentsList.get(index).setEnabled(true);

			disableButton(refresh);
		}

		hPanel.add(refresh);

		// if simulation is still running or is finishing up, then disable
		// buttons
		if (simulation || simulationStopping)
			disableButton(removeTabButton);
		else
			enableButton(removeTabButton);

		hPanel.add(removeTabButton);

	}

	/**
	 * Enables the given button and removes the disabledCSS string part of the
	 * CSS class
	 * 
	 * @param Button
	 *            b the button to be enabled
	 */
	protected void enableButton(Button b) {
		/* Enable button */
		b.setEnabled(true);

		/* Replace CSS image */
		String curClass = b.getStylePrimaryName();
		if (curClass.contains(disabledCSS)) {
			curClass = curClass.replace(disabledCSS, "");
			b.setStylePrimaryName(curClass);
		}
	}

	/**
	 * Disables the given button and adds the disabledCSS string to the current
	 * CSS class
	 * 
	 * @param Button
	 *            b the button to be disabled
	 */
	protected void disableButton(Button b) {
		/* Disable button */
		b.setEnabled(false);
		/* Replace CSS image */
		String curClass = b.getStylePrimaryName();
		if (!curClass.contains(disabledCSS)) {
			curClass += disabledCSS;
			b.setStylePrimaryName(curClass);
		}
	}

	/**
	 * Lock the UI after the user presses the simulate button so that no actions
	 * could be done.
	 */
	protected void lockDownUI() {
		// disable all buttons under the doc list, except the stop simulate
		// button
		for (Widget w : docListButtonPanel) {
			Button button = (Button) w;
			if (button != stopSimulateButton)
				disableButton(button);
		}

		// disable all buttons in rightHPanel and leftHPanel
		for (Widget w : rightHPanel)
			disableButton((Button) w);
		for (Widget w : rightHPanel)
			disableButton((Button) w);
	}

}