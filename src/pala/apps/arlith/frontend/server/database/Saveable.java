package pala.apps.arlith.frontend.server.database;

import java.io.OutputStream;

public interface Saveable {
	long save(OutputStream file) throws ObjectSaveFailureException;
}
