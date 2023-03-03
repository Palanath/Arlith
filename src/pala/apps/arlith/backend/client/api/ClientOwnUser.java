package pala.apps.arlith.backend.client.api;

import static pala.apps.arlith.libraries.CompletableFutureUtils.getValueWithDefaultExceptions;

import java.util.concurrent.CompletableFuture;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.v2.NewCache;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.ChangeEmailError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
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
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;

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

	public CompletableFuture<Void> setProfileIconRequest(byte[] data) {
		return client().getRequestQueue()
				.queueFuture(new SetProfileIconRequest(data == null ? null : new PieceOMediaValue(data)))
				.thenAccept(a -> refreshProfileIcon());
	}

	public void setProfileIcon(byte[] data) throws ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		getValueWithDefaultExceptions(setProfileIconRequest(data));
	}

	public CompletableFuture<String> getEmailRequest() {
		return email.future();
	}

	public CompletableFuture<String> getPhoneNumberRequest() {
		return phoneNumber.future();
	}

	public String getEmail() throws ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getEmailRequest());
	}

	public String getPhoneNumber() throws ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getPhoneNumberRequest());
	}

	public CompletableFuture<Void> setEmailRequest(String email) {
		return client().getRequestQueue().queueFuture(new ChangeEmailRequest(email))
				.thenAccept(a -> this.email.updateItem(email));
	}

	public CompletableFuture<Void> setPhoneNumberRequest(String phoneNumber) {
		return client().getRequestQueue().queueFuture(new ChangePhoneNumberRequest(phoneNumber))
				.thenAccept(a -> this.phoneNumber.updateItem(phoneNumber));
	}

	public void setEmail(String email)
			throws ChangeEmailError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		getValueWithDefaultExceptions(setEmailRequest(email), ChangeEmailError.class);
	}

	public void setPhoneNumber(String phoneNumber) throws CommunicationProtocolError, RuntimeException {
		getValueWithDefaultExceptions(setPhoneNumberRequest(phoneNumber), CreateAccountError.class);
	}

	public CompletableFuture<Void> setUsernameRequest(String username) {
		return client().getRequestQueue().queueFuture(new ChangeUsernameRequest(new TextValue(username)))
				.thenAccept(a -> this.username.updateItem(username));
	}

	public void setUsername(String username)
			throws CreateAccountError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		getValueWithDefaultExceptions(setUsernameRequest(username), CreateAccountError.class);
	}

}
