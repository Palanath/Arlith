package pala.apps.arlith.app.client.api;

import pala.apps.arlith.app.client.ArlithClient;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.communication.gids.Identifiable;

public class SimpleClientObject implements ClientObject, Identifiable {

	@Override
	public final int hashCode() {
		return gid.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		return obj instanceof SimpleClientObject && gid.equals(((SimpleClientObject) obj).gid);
	}

	private final GID gid;
	private final ArlithClient client;

	protected SimpleClientObject(GID gid, ArlithClient client) {
		if (gid == null || client == null)
			throw null;
		this.gid = gid;
		this.client = client;
	}

	@Override
	public final ArlithClient client() {
		return client;
	}

	@Override
	public final GID id() {
		return gid;
	}

}
