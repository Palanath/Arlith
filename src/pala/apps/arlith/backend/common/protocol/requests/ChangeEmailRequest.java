package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.CreateAccountProblemValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

/**
 * <h1>Change Email Request</h1>
 * <p>
 * Represents a request to change the user's email on Arlith. After a successful
 * invocation of this request, the email address associated with the user
 * invoking the request will be the specified {@link #getEmail()}.
 * </p>
 * <p>
 * This request requires the requesting connection to be <i>authorized</i>.
 * </p>
 * <h2>Request Structure</h2>
 * <p>
 * This request has the following parameters:
 * </p>
 * <table class="parameters">
 * <tr>
 * <th>Parameter Name</th>
 * <th>Type</th>
 * <th>Description</th>
 * <th>Optional?</th>
 * </tr>
 * <tr>
 * <td>Email</td>
 * <td>{@link TextValue}</td>
 * <td>The new email to associate with this account.</td>
 * <td>No</td>
 * </tr>
 * </table>
 * <p>
 * This request can result in the following errors:
 * </p>
 * <table class="errors">
 * <tr>
 * <th>Error Name</th>
 * <th>Formal Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Syntax</td>
 * <td>{@link SyntaxError}</td>
 * <td>If the request is syntactically invalid.</td>
 * </tr>
 * <tr>
 * <td>Rate Limit</td>
 * <td>{@link RateLimitError}</td>
 * <td>If the server attempts to rate limit the connection.</td>
 * </tr>
 * <tr>
 * <td>Server</td>
 * <td>{@link ServerError}</td>
 * <td>If the server encounters an unknown, unexpected error.</td>
 * </tr>
 * <tr>
 * <td>Restricted</td>
 * <td>{@link RestrictedError}</td>
 * <td>If the current connection does not have permission to invoke this
 * request.</td>
 * </tr>
 * <tr>
 * <td>Create Account</td>
 * <td>{@link CreateAccountError}</td>
 * <td>If there is a syntactic or other error with the username. See
 * below<sup>[1]</sup> for details.</td>
 * </tr>
 * <tr>
 * <td>Illegal Protocol Err</td>
 * <td>{@link IllegalCommunicationProtocolException}</td>
 * <td>Denotes that the server responded with an error that should not have been
 * sent. This is usually indicative that the client and server are running
 * different versions (and that the specification for this request was updated
 * between the versions). If this is thrown, it will wrap the unexpected
 * {@link CommunicationProtocolError}.</td>
 * </table>
 * <p>
 * <sup>[1]</sup>This request uses the {@link CreateAccountError} if the user
 * attempts to change their email address to a new email that one could not
 * create an account with. Specifically,
 * </p>
 * <ul>
 * <li>If the provided email address is not syntactically valid as per <a href=
 * "https://arlith.net/user-accounts/">https://arlith.net/user-accounts/</a>,
 * this request will result in a {@link CreateAccountError} with
 * {@link CreateAccountProblemValue#ILLEGAL_EM}.</li>
 * <li>If the provided email address is already in use by another account, this
 * request will result in a {@link CreateAccountError} with
 * {@link CreateAccountProblemValue#TAKEN_EM}.</li>
 * </ul>
 * <p>
 * The result type of this request is a {@link CompletionValue}, which indicates
 * that the request has completed and that the account's email address is what
 * was requested.
 * </p>
 * 
 * @author Palanath
 *
 */
public class ChangeEmailRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {

	public static final String REQUEST_NAME = "change-email";
	private static final String EMAIL_KEY = "email";

	private TextValue email;

	@Override
	protected CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	public ChangeEmailRequest(String email) {
		super(REQUEST_NAME);
		this.email = new TextValue(email);
	}

	public ChangeEmailRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		email = new TextValue(properties.get(EMAIL_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(EMAIL_KEY, email.json());
	}

	public TextValue getEmail() {
		return email;
	}

	public String email() {
		return email.getValue();
	}

	public void setEmail(TextValue email) {
		this.email = email;
	}

	@Override
	public CompletionValue receiveResponse(CommunicationConnection client)
			throws RateLimitError, SyntaxError, ServerError, CreateAccountError, IllegalCommunicationProtocolException {
		try {
			return super.receiveResponse(client);
		} catch (RateLimitError | SyntaxError | ServerError | CreateAccountError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
