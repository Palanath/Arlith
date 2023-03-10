package pala.apps.arlith.backend.client.api;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.gids.Identifiable;

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
