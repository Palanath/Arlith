package pala.apps.arlith.backend.server.systems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.server.database.Writing;

/**
 * This class manages authentication of the server. It stores auth tokens and
 * hashed, salted passwords and maps user GIDs to them. This class exposes the
 * ability to obtain a user's hashed password or auth token, given its ID.
 * 
 * @author Palanath
 *
 */
public class AuthenticationSystem {
	private final Map<AuthToken, GID> usersByToken = new HashMap<>();
	private final Map<GID, AuthToken> authTokens = new HashMap<>();

	/**
	 * <p>
	 * Used to generate an {@link AuthToken} for a user once they've authenticated
	 * themselves with a password (e.g. when logging in or when creating an
	 * account).
	 * </p>
	 * <p>
	 * This method generates an {@link AuthToken} for the specified user and stores
	 * it in this {@link AuthenticationSystem} so that subsequent calls to
	 * {@link #matchToken(ServerUser, AuthToken)} will return appropriately.
	 * </p>
	 * <p>
	 * An {@link AuthToken} can be cleared using {@link #logout(ServerUser)}.
	 * </p>
	 * <p>
	 * <b>Any previous {@link AuthToken} for the specified {@link ServerUser} will
	 * be cleared by a call to this method (and will be replaced with the new,
	 * returned {@link AuthToken}).</b>
	 * </p>
	 * 
	 * @param account The user to generate and store an {@link AuthToken} for.
	 * @return The newly generated {@link AuthToken} for this user. Previous
	 *         {@link AuthToken}s for this user are invalidated.
	 */
	public @Writing AuthToken login(ServerUser account) {
//		HexHashValue pwhash = hyperHash(unsaltedPassword, account.getGID().timestamp());

//		if (!account.getPassword().equals(unsaltedPassword))
//			throw new LoginError(LoginProblemValue.INVALID_PW);

		AuthToken newToken = new AuthToken();
		usersByToken.put(newToken, account.getGID());
		authTokens.put(account.getGID(), newToken);
		return newToken;
	}

	/**
	 * Returns whether the provided {@link AuthToken} is valid for the provided
	 * {@link ServerUser}.
	 * 
	 * @param account The user to check the {@link AuthToken} for.
	 * @param token   The {@link AuthToken}.
	 * @return <code>true</code> if the provided {@link AuthToken} is equal to the
	 *         stored {@link ServerUser}'s {@link AuthToken}, <code>false</code>
	 *         otherwise.
	 */
	public boolean matchToken(ServerUser account, AuthToken token) {
		return token.equals(authTokens.get(account.getGID()));
	}

	/**
	 * Gets the user associated with the specified {@link AuthToken}.
	 * 
	 * @param token The {@link AuthToken} to get the user owning.
	 * @return The associated {@link ServerUser}, or <code>null</code>, if none.
	 */
	public GID getUser(AuthToken token) {
		return usersByToken.get(token);
	}

	public @Writing void logout(ServerUser account) throws IOException {
		usersByToken.remove(authTokens.remove(account.getGID()));
	}

}
