package pala.apps.arlith.app.server.database;

import java.io.OutputStream;

public interface Saveable {
	long save(OutputStream file) throws ObjectSaveFailureException;
}
