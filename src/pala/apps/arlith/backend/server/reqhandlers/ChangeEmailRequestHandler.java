package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.ChangeEmailError;
import pala.apps.arlith.backend.common.protocol.errors.ChangeEmailError.ErrorType;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.ChangeEmailRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

/**
 * <p>
 * Handles {@link ChangeEmailRequest}s by changing the email address associated
 * with the logged in user.
 * </p>
 * <p>
 * This handler requires connections to be <i>authorized</i> with a user
 * account.
 * </p>
 * 
 * @author Palanath
 *
 */
public final class ChangeEmailRequestHandler extends SimpleRequestHandler<ChangeEmailRequest> {

	@Override
	protected void handle(ChangeEmailRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else if (client.getWorld().checkIfEmailTaken(r.email()))
			client.sendError(new ChangeEmailError(ErrorType.EMAIL_ALREADY_IN_USE));
		else if (Utilities.checkEmailValidity(r.email()) != null) {
			client.sendError(new ChangeEmailError(ErrorType.EMAIL_SYNTACTICALLY_INVALID));
		} else {
			client.getUser().changeEmail(r.email());
			client.sendResult(new CompletionValue());
		}
	}

	public ChangeEmailRequestHandler() {
		super(ChangeEmailRequest::new);
	}
}
