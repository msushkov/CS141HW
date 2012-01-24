package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.List;
import java.util.logging.Logger;

import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.Document;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;

import javax.jdo.Query;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;



import java.util.Date;
import java.util.ArrayList;
import java.util.Set;

import javax.jdo.PersistenceManager;
/**
 * The server side implementation of the RPC service.
 */




@SuppressWarnings("serial")
public class CollaboratorServiceImpl extends RemoteServiceServlet implements
    CollaboratorService {
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private PersistenceManager pm = PMF.get().getPersistenceManager();


  private static final Logger log = Logger.getLogger(CollaboratorServiceImpl.class.toString());
  
  // Datastore static strings
  private static final String dsDoc = "Document";
  private static final String dsTitle = "title";
  private static final String dsContent = "content";
  private static final String dsLocked = "locked";
  private static final String dsLockedTil = "lockedTil";

  
  
  @Override
  public List<DocumentMetadata> getDocumentList() {
	  Query query = pm.newQuery(Document.class);
	  List<Document> documentList;
	  ArrayList <DocumentMetadata> docList = new ArrayList<DocumentMetadata>();
	  try {
		  documentList = (List<Document >) query.execute();
		  System.out.println("Document list = " + documentList);
		  for (Document doc:documentList) {
			  DocumentMetadata metaDoc = new DocumentMetadata(doc.GetKey(),doc.GetTitle());
			  docList.add(metaDoc);
		  }
	  } finally {
	        query.closeAll();
	  }

    return docList;
  }

  @Override
  public LockedDocument lockDocument(String documentKey)
      throws LockUnavailable
 {
    return null;
  }

  @Override
  public UnlockedDocument getDocument(String documentKey) {
	Key key = KeyFactory.stringToKey(documentKey);
	UnlockedDocument unlockedDoc;
	try {
		Document doc = pm.getObjectById(Document.class, key);
		unlockedDoc = doc.GetUnlocked();
	} finally {
		pm.close();
	}
    return unlockedDoc;
    
  }

  @Override
  public UnlockedDocument saveDocument(LockedDocument doc)
    throws LockExpired {
	String stringKey = doc.getKey();
	Document toSave;
	if (stringKey == null) {
		toSave = new Document(doc);
	} else {
		Key key = KeyFactory.stringToKey(stringKey);
		toSave = pm.getObjectById(Document.class, key);
		toSave.Update(doc);
	}
	try {
		pm.makePersistent(toSave);
	} finally {
		pm.close();
		
	}
    
    return toSave.GetUnlocked();
  }
  
  @Override
  public void releaseLock(LockedDocument doc) throws LockExpired {
  }

}
