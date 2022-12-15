package pala.apps.arlith.api.communication.protocol.types;

import pala.apps.arlith.api.communication.gids.GID;
import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public class GIDValue implements CommunicationProtocolType {

	public GID getGid() {
		return gid;
	}

	private final GID gid;

	public GIDValue(GID gid) {
		this.gid = gid;
	}

	public GIDValue(JSONValue json) {
		try {
			gid = GID.fromString(((JSONString) json).getValue());
		} catch (RuntimeException e) {
			throw new CommunicationProtocolConstructionError(e, json);
		}
	}

	@Override
	public JSONValue json() {
		return new JSONString(gid.toString());
	}

	/**
	 * Returns a {@link GIDValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link GIDValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link GIDValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link GIDValue}, whichever represents the
	 *         provided argument.
	 */
	public static GIDValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new GIDValue(value);
	}

}
