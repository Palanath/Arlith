package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.CommunicationProtocolType;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

/**
 * <p>
 * This class represents a {@link CommunicationProtocolRequest} that does not send nor receive any
 * auxiliary data when it's made out to the server. {@link SimpleCommunicationProtocolRequest}s
 * are entirely defined by their {@link #json() JSON package}.
 * </p>
 * 
 * @author Palanath
 *
 */
public abstract class SimpleCommunicationProtocolRequest<R extends CommunicationProtocolType> extends CommunicationProtocolRequest<R> {

	protected SimpleCommunicationProtocolRequest(String requiredName, JSONObject properties) throws CommunicationProtocolConstructionError {
		super(requiredName, properties);
	}

	public SimpleCommunicationProtocolRequest(String requestName) {
		super(requestName);
	}

	public SimpleCommunicationProtocolRequest(TextValue requestName) {
		super(requestName);
	}

	/**
	 * Parses the return value for the {@link CommunicationProtocolRequest} from the provided
	 * {@link JSONValue}. {@link SimpleCommunicationProtocolRequest}s have no auxiliary data, so all
	 * of the request's response should be recoverable from the JSON response
	 * package (provided to this method).
	 * 
	 * @param json The {@link JSONValue} to parse.
	 * @return The return value from the server.
	 */
	protected abstract R parseReturnValue(JSONValue json);

	@Override
	protected final R parseReturnValue(JSONValue json, CommunicationConnection connection) {
		return parseReturnValue(json);
	}

	@Override
	protected final void sendAuxiliaryData(CommunicationConnection connection) {
	}

}
