package pala.apps.arlith.backend.client.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.ClientCache;
import pala.apps.arlith.backend.client.api.caching.v2.NewCache;
import pala.apps.arlith.backend.client.requests.v2.ActionInterface;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.requests.ChangeEmailRequest;
import pala.apps.arlith.backend.common.protocol.requests.ChangePhoneNumberRequest;
import pala.apps.arlith.backend.common.protocol.requests.ChangeUsernameRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetEmailRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetPhoneNumberRequest;
import pala.apps.arlith.backend.common.protocol.requests.SetProfileIconRequest;
import pala.apps.arlith.backend.common.protocol.requests.SetStatusRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

import static pala.apps.arlith.libraries.CompletableFutureUtils.*;

public class ClientOwnUser extends ClientUser {

	private final NewCache<String> email = new NewCache<>(new GetEmailRequest(), TextValue::getValue,
			client().getRequestQueue()),
			phoneNumber = new NewCache<>(new GetPhoneNumberRequest(), TextValue::getValue, client().getRequestQueue());

	public ClientOwnUser(GID gid, ArlithClient client, String username, String status, long messageCount,
			String discriminant) {
		super(gid, client, username, status, messageCount, discriminant);
	}

	public CompletableFuture<Void> setStatusRequest(String status) {
		return client().getRequestQueue().queueFuture(new SetStatusRequest(new TextValue(status)))
				.thenAccept(a -> this.status.updateItem(status));
	}

	public void setStatus(String status) throws ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		getValueWithDefaultExceptions(setStatusRequest(status));
	}

	// TODO Fix.
	public ActionInterface<Void> setProfileIconRequest(byte[] data) {
		return client().getRequestSubsystem()
				.action(new SetProfileIconRequest(data == null ? null : new PieceOMediaValue(data)))
				.then((Consumer<CompletionValue>) a -> refreshProfileIcon());
	}

	public void setProfileIcon(byte[] data) throws CommunicationProtocolError, RuntimeException {
		setProfileIconRequest(data).get();
	}

	public ActionInterface<String> getEmailRequest() {
		return email.get();
	}

	public ActionInterface<String> getPhoneNumberRequest() {
		return phoneNumber.get();
	}

	public String getEmail() throws CommunicationProtocolError, RuntimeException {
		return getEmailRequest().get();
	}

	public String getPhoneNumber() throws CommunicationProtocolError, RuntimeException {
		return getPhoneNumberRequest().get();
	}

	public ActionInterface<Void> setEmailRequest(String email) {
		return client().getRequestSubsystem().action(new ChangeEmailRequest(email))
				.process(a -> this.email.update(email)).ditchResult();
	}

	public ActionInterface<Void> setPhoneNumberRequest(String phoneNumber) {
		return client().getRequestSubsystem().action(new ChangePhoneNumberRequest(phoneNumber))
				.process(a -> this.phoneNumber.update(phoneNumber)).ditchResult();
	}

	public void setEmail(String email) throws CommunicationProtocolError, RuntimeException {
		setEmailRequest(email).get();
	}

	public void setPhoneNumber(String phoneNumber) throws CommunicationProtocolError, RuntimeException {
		setPhoneNumberRequest(phoneNumber).get();
	}

	public ActionInterface<Void> setUsernameRequest(String username) {
		return client().getRequestSubsystem().action(new ChangeUsernameRequest(new TextValue(username)))
				.process(a -> this.username.update(username)).ditchResult();
	}

	public void setUsername(String username) throws CommunicationProtocolError, RuntimeException {
		setUsernameRequest(username).get();
	}

}
