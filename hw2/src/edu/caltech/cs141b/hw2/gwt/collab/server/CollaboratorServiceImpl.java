package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw2.gwt.collab.shared.Document;
import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * The server side implementation of the RPC service.
 */

@SuppressWarnings("serial")
public class CollaboratorServiceImpl extends RemoteServiceServlet implements
		CollaboratorService {

	@SuppressWarnings("unchecked")
	@Override
	public List<DocumentMetadata> getDocumentList() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Document.class);
		ArrayList<DocumentMetadata> docList = new ArrayList<DocumentMetadata>();
		Transaction t = pm.currentTransaction();

		try {
			t.begin();

			for (Document doc : (List<Document>) query.execute()) {
				DocumentMetadata metaDoc = new DocumentMetadata(doc.getKey(),
						doc.getTitle());
				docList.add(metaDoc);
			}

			t.commit();
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
			query.closeAll();
			pm.close();
		}

		return docList;
	}

	@Override
	public UnlockedDocument getDocument(String documentKey) {
		UnlockedDocument unlockedDoc;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction t = pm.currentTransaction();

		try {
			t.begin();

			Key key = KeyFactory.stringToKey(documentKey);
			Document doc = pm.getObjectById(Document.class, key);
			unlockedDoc = doc.getUnlockedDoc();

			t.commit();
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
			pm.close();
		}

		return unlockedDoc;

	}

	@Override
	public UnlockedDocument saveDocument(LockedDocument doc) throws LockExpired {
		String stringKey = doc.getKey();
		Document toSave;
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Transaction t = pm.currentTransaction();
		try {
			t.begin();
			if (stringKey == null) {
				toSave = new Document(doc);
				pm.makePersistent(toSave);
			} else {
				Key key = KeyFactory.stringToKey(stringKey);
				toSave = pm.getObjectById(Document.class, key);
				String lockedBy = toSave.getLockedBy();
				Date lockedUntil = toSave.getLockedUntil();

				String identity = getThreadLocalRequest().getRemoteAddr();
				if (lockedBy.equals(identity)
						&& lockedUntil.after(new Date(System
								.currentTimeMillis()))) {
					toSave.update(doc);
					toSave.unlock();
					pm.makePersistent(toSave);
				} else {
					throw new LockExpired();
				}
			}
			t.commit();
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
			pm.close();
		}

		return toSave.getUnlockedDoc();

	}

	@Override
	public void releaseLock(LockedDocument doc) throws LockExpired {
		String stringKey = doc.getKey();
		Document toSave;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction t = pm.currentTransaction();
		try {
			t.begin();
			Key key = KeyFactory.stringToKey(stringKey);
			toSave = pm.getObjectById(Document.class, key);
			toSave.unlock();
			pm.makePersistent(toSave);
			t.commit();
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
			pm.close();
		}
	}

	@Override
	public LockedDocument lockDocument(String documentKey)
			throws LockUnavailable {

		String identity = getThreadLocalRequest().getRemoteAddr();
		Document toSave;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction t = pm.currentTransaction();
		try {
			t.begin();

			Key key = KeyFactory.stringToKey(documentKey);
			toSave = pm.getObjectById(Document.class, key);

			if (toSave.isLocked()
					&& toSave.getLockedUntil().before(
							new Date(System.currentTimeMillis()))) {
				toSave.unlock();
			}

			if (toSave.isLocked()) {
				throw new LockUnavailable(key + " is locked");
			} else {
				toSave.lock(new Date(System.currentTimeMillis() + 20000L),
						identity);
				pm.makePersistent(toSave);
			}

			t.commit();
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
			pm.close();
		}
		return toSave.getLockedDoc();
	}

}
