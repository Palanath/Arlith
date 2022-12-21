package pala.apps.arlith.backend.server.database;

import java.io.IOException;

public class ObjectSaveFailureException extends IOException {
	private final long bytesWritten;

	public ObjectSaveFailureException(long bytesWritten) {
		this.bytesWritten = bytesWritten;
	}

	public long getBytesWritten() {
		return bytesWritten;
	}

	public ObjectSaveFailureException(Throwable cause, long bytesWritten) {
		super(cause);
		this.bytesWritten = bytesWritten;
	}

}
