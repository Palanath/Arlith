package pala.apps.arlith.app.client.api;

import pala.apps.arlith.api.communication.gids.GID;
import pala.apps.arlith.api.communication.gids.Identifiable;
import pala.apps.arlith.app.client.Client;

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
	private final Client client;

	protected SimpleClientObject(GID gid, Client client) {
		if (gid == null || client == null)
			throw null;
		this.gid = gid;
		this.client = client;
	}

	@Override
	public final Client client() {
		return client;
	}

	@Override
	public final GID id() {
		return gid;
	}

}
