package pala.apps.arlith.backend.server.contracts.world;

import java.util.Collection;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.types.HexHashValue;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.libraries.Utilities;

public interface ServerWorld {

	/**
	 * Checks if a user with the specified email already exists (in which case the
	 * email is "taken").
	 *
	 * @param email The email address to check.
	 * @return <code>true</code> if the email address is already in use.
	 *         <code>false</code> otherwise.
	 */
	default boolean checkIfEmailTaken(final String email) {
		return getUserByEmail(email) != null;
	}

	/**
	 * Checks if a user with the specified phone number already exists (in which
	 * case the phone number is "taken").
	 *
	 * @param phoneNumber The phone number to check the unavailability of.
	 * @return <code>true</code> if the phone number is already in use.
	 *         <code>false</code> otherwise.
	 */
	default boolean checkIfPhoneTaken(final String phoneNumber) {
		return getUserByPhone(phoneNumber) != null;
	}

	/**
	 * Checks if a user with the specified username and discriminator exists.
	 *
	 * @param username The username of the user.
	 * @param disc     The discriminator of the user.
	 * @return <code>true</code> if a user with the specified username and
	 *         discriminator exists. <code>false</code> otherwise.
	 */
	default boolean checkIfUserExists(final String username, final String disc) {
		return getUserByUsername(username, disc) != null;
	}

	/**
	 * Creates a new Application user given the provided username and password. The
	 * account will have no registered phone number or email address. A
	 * discriminator will be allocated for the user automatically.
	 *
	 * @param username The username for the account.
	 * @param password The hash of the password.
	 * @return A new {@link ServerUser} object representing the new user.
	 */
	default ServerUser createUser(final String username, final HexHashValue password) {
		return createUserWithEmailAndPhone(username, password, null, null);
	}

	/**
	 * <p>
	 * Creates a new Application user with the provided username, password, and
	 * email, if the email is not already taken. The account will have no registered
	 * phone number. A discriminator will be allocated for the user automatically.
	 * </p>
	 * <p>
	 * If the provided email is already in use, this method returns
	 * <code>null</code>. If the provided email itself is <code>null</code>, the
	 * account is created without an email.
	 * </p>
	 *
	 * @param username The username of the account.
	 * @param password The hashed password.
	 * @param email    The email of the account. Can be <code>null</code>, in which
	 *                 case the {@link ServerUser} is made without an email.
	 * @return A new {@link ServerUser}.
	 */
	default ServerUser createUserWithEmail(final String username, final HexHashValue password, final String email) {
		return createUserWithEmailAndPhone(username, password, email, null);
	}

	/**
	 * <p>
	 * Creates a new {@link ServerUser} with the provided username, password hash,
	 * email, and phone number. If either of the email or phone is already in use,
	 * the user is not created and this method returns <code>null</code>. If the
	 * provided phone number is <code>null</code>, creation will succeed, but the
	 * created user account will not have an associated phone number. If the
	 * username, email, or phone number are syntactically incorrect, this method
	 * will fail and return <code>null</code>.
	 * </p>
	 * <p>
	 * This method will succeed even if the provided username is already in use, but
	 * this method will not return a {@link ServerUser} with a non-unique <b>tag</b>
	 * (username and discriminator pair). The tag of the created user will be
	 * obtainable via {@link ServerUser#getTag()} after the object is returned.
	 * </p>
	 * <h2>Argument Syntax</h2>
	 * <p>
	 * Each non-<code>null</code> argument must follow syntactical restrictions as
	 * specified <a href="https://arlith.net/user-accounts">here</a>.
	 * </p>
	 * <h2>Implementation</h2>
	 * <p>
	 * This method relies on the {@link Utilities} class to verify the validity of
	 * arguments. In particular:
	 * </p>
	 * <ul>
	 * <li>The <code>username</code> is verified via
	 * {@link Utilities#checkUsernameValidity(String)},</li>
	 * <li>the <code>email</code> is verified via
	 * {@link Utilities#checkEmailValidity(String)}, and</li>
	 * <li>the <code>phone number</code> is verified via
	 * {@link Utilities#checkPhoneNumberValidity(String)}.</li>
	 * </ul>
	 * <p>
	 * If any of the checks fails, this method returns <code>null</code> and the
	 * user is not created. If the verifications succeed, this method returns the
	 * result of calling
	 * {@link #createUserWithEmailAndPhoneUnchecked(String, HexHashValue, String, String)},
	 * with the same arguments.
	 * </p>
	 *
	 * @param username    The user's username.
	 * @param password    The user's hashed password.
	 * @param email       The user's email.
	 * @param phoneNumber The user's phone number. Can be <code>null</code>, in
	 *                    which case the {@link ServerUser} is created without a
	 *                    phone number.
	 * @return A new {@link ServerUser}, or <code>null</code> if either the provided
	 *         username or email are taken.
	 */
	default ServerUser createUserWithEmailAndPhone(String username, HexHashValue password, String email,
			String phoneNumber) {
		return Utilities.checkUsernameValidity(username) == null && Utilities.checkEmailValidity(email) == null
				&& Utilities.checkPhoneNumberValidity(phoneNumber) == null
						? createUserWithEmailAndPhoneUnchecked(username, password, email, phoneNumber)
						: null;
	}

	/**
	 * <p>
	 * This method creates a user account with the specified username, password,
	 * email address, and phone number. This method does not make any checks to
	 * verify the <i>syntacitcal validity</i> of arguments (see <a href=
	 * "https://arlith.net/user-accounts/">https://arlith.net/user-accounts/</a> for
	 * information on syntax). This method is designed to be invoked by
	 * {@link #createUserWithEmailAndPhone(String, HexHashValue, String, String)}
	 * (and indirectly by {@link #createUser(String, HexHashValue)},
	 * {@link #createUserWithEmail(String, HexHashValue, String)}, and
	 * {@link #createUserWithPhone(String, HexHashValue, String)}) after arguments
	 * have been checked for syntactic validity. It can, however, be safely invoked
	 * by external code which has verified the syntactic validity of arguments in
	 * accordance with the restrictions set forth by <a href=
	 * "https://arlith.net/user-accounts/">https://arlith.net/user-accounts/</a>.
	 * </p>
	 * <p>
	 * Like its checked-variant methods, if either the email address or the phone
	 * number is already in use, the user account is not created and this method
	 * returns <code>null</code>.
	 * </p>
	 * 
	 * 
	 * @param username    The user's username.
	 * @param password    The user's password hash.
	 * @param email       The user's email address.
	 * @param phoneNumber The user's phone number. This can optionally be
	 *                    <code>null</code>, in which case the method will create
	 *                    the user account without an associated phone number.
	 * @return The newly created {@link ServerUser} object representing the new user
	 *         account, or <code>null</code> if either the email address or phone
	 *         number is already in use.
	 */
	ServerUser createUserWithEmailAndPhoneUnchecked(String username, HexHashValue password, String email,
			String phoneNumber);

	/**
	 * <p>
	 * Creates a new {@link ServerUser} with the provided username, password hash,
	 * and phone number. If the phone number is <code>null</code>, the
	 * {@link ServerUser} is created without a phone number. If the provided phone
	 * number is not <code>null</code> and is taken, the user is not created and
	 * this method returns <code>null</code>.
	 * </p>
	 *
	 * @param username    The user's username.
	 * @param password    The user's hashed password.
	 * @param phoneNumber The user's phone number. Can be <code>null</code>, in
	 *                    which case the user will not have a registered phone
	 *                    number.
	 * @return The {@link ServerUser}, or <code>null</code> if the provided phone
	 *         number is taken.
	 */
	default ServerUser createUserWithPhone(final String username, final HexHashValue password,
			final String phoneNumber) {
		return createUserWithEmailAndPhone(username, password, null, phoneNumber);
	}

	/**
	 * <p>
	 * Returns a discriminator to be given to the next user with the specified
	 * username. This method does not actually perform any discriminator allocation;
	 * it simply returns a viable discriminator for the next user with the specified
	 * username. It is up to the server to determine what discriminator the next
	 * user may have. The implementor is free to return the same free discriminator
	 * upon repeated calls to this method until that discriminator is no longer
	 * free, or to implement this method to randomly return a free discriminator.
	 * </p>
	 * <p>
	 * This method is used to return discriminators for use when creating new users,
	 * changing usernames, and any other user-username discrimination processes.
	 * </p>
	 *
	 * @param username The username of the user.
	 * @return A, possibly new, viable (not already in use) discriminator for the
	 *         specified username.
	 */
	String getNextDiscriminator(String username);

	ArlithServer getServer();

	/**
	 * Obtains a user by email, if one with the specified email exists. Otherwise
	 * returns <code>null</code>.
	 *
	 * @param email The email address of the user to get.
	 * @return The {@link ServerUser} with the specified email address, or
	 *         <code>null</code> if none exists.
	 */
	ServerUser getUserByEmail(String email);

	/**
	 * Gets a user with the specified phone number, if one eixsts. Otherwise,
	 * returns <code>null</code>.
	 *
	 * @param phone The phone number of the user.
	 * @return The {@link ServerUser} with the specified phone number, or
	 *         <code>null</code> if none exists.
	 */
	ServerUser getUserByPhone(String phone);

	/**
	 * Gets a user by username and discriminator. This method is case-sensitive. If
	 * a user with the specified username and discriminator are not found, this
	 * method returns <code>null</code>.
	 *
	 * @param username The username of the user to get.
	 * @param disc     The discriminator of the user to get.
	 * @return The obtained {@link ServerUser} object, or <code>null</code> if none
	 *         applicable.
	 */
	ServerUser getUserByUsername(String username, String disc);

	/**
	 * Gets a user by its {@link GID}.
	 * 
	 * @param id The {@link GID} of the user to get.
	 * @return The {@link ServerUser} or <code>null</code> if none with the
	 *         specified {@link GID} was found.
	 */
	ServerUser getUserByID(GID id);

	/**
	 * Lists all public communities on the platform. This will need to be changed
	 * later, most likely. Public communities are listed publicly in the
	 * application.
	 *
	 * @return An unmodifiable {@link Collection} of all the public communities on
	 *         this platform.
	 */
	Collection<? extends ServerCommunity> listPublicCommunities();

}
