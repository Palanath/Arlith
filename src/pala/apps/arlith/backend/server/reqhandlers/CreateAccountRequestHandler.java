package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError.CreateAccountProblemValue;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.CreateAccountRequest;
import pala.apps.arlith.backend.common.protocol.types.AuthTokenValue;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.server.systems.EventConnectionImpl;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

public final class CreateAccountRequestHandler extends SimpleRequestHandler<CreateAccountRequest> {

	@Override
	protected void handle(final CreateAccountRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {

		if (client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			AuthToken token;
			ServerUser user;
			// Check to see if email or phone are taken.
			if (client.getWorld().checkIfEmailTaken(r.emailAddress())) {
				client.sendError(new CreateAccountError(CreateAccountProblemValue.TAKEN_EM));
				return;
			}
			if (r.hasPhoneNumber() && client.getWorld().checkIfPhoneTaken(r.phoneNumber())) {
				client.sendError(new CreateAccountError(CreateAccountProblemValue.TAKEN_PH));
				return;
			}
			// Check to make sure arguments are valid.
			if (Utilities.checkUsernameValidity(r.username()) != null) {
				client.sendError(new CreateAccountError(CreateAccountProblemValue.ILLEGAL_UN));
				return;
			}
			if (Utilities.checkEmailValidity(r.emailAddress()) != null) {
				client.sendError(new CreateAccountError(CreateAccountProblemValue.ILLEGAL_EM));
				return;
			}
			if (r.phoneNumber() != null && Utilities.checkPhoneNumberValidity(r.phoneNumber()) != null) {
				client.sendError(new CreateAccountError(CreateAccountProblemValue.ILLEGAL_PH));
				return;
			}
			// "Unchecked" creation; we've already verified the validity of arguments.
			user = client.getWorld().createUserWithEmailAndPhoneUnchecked(r.username(), r.getPassword(),
					r.emailAddress(), r.phoneNumber());
			// user should not be null at this point (unless there is e.g. a synchronization
			// issue. TODO Account for this later.)
			ArlithServer.getThreadLogger().std("The user " + user.getTag() + " just created their account.");
			ArlithServer.changeThreadLoggerPurpose(user.getTag());
			token = client.getServer().getAuthSystem().login(user);
			// Register an Event Client wrapping this same connection to the event handler.
			client.getServer().getEventSystem()
					.registerClient(new EventConnectionImpl(client.getConnection(), user.getGID()));
			client.sendResult(new AuthTokenValue(token));
			client.stopListening();
			ArlithServer.getThreadLogger().std("The user " + user.getTag() + " just created their account.");
		}
	}

	public CreateAccountRequestHandler() {
		super(CreateAccountRequest::new);
	}
}
