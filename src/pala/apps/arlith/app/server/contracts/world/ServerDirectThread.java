package pala.apps.arlith.app.server.contracts.world;

import java.util.Arrays;
import java.util.Collection;

public interface ServerDirectThread extends ServerThread {
	@Override
	default Collection<ServerUser> getParticipants() {
		return Arrays.asList(getStarter(), getReceiver());
	}

	/**
	 * Returns the "other" {@link ServerUser} in this {@link ServerDirectThread}. This is
	 * not the user that initiated the creation of the thread. This user is a
	 * participant in the thread though. The initiating user is the
	 * {@link #getStarter() starter}.
	 *
	 * @return The {@link ServerUser} that is a participant in this thread but is not
	 *         {@link #getStarter()}.
	 */
	ServerUser getReceiver();

	/**
	 * Returns the {@link ServerUser} that "opened" (initiated the creation of) this
	 * {@link ServerDirectThread}. This is always one of the two members/participants of
	 * a direct thread. The other participant is the {@link #getReceiver()
	 * receiver}.
	 *
	 * @return The {@link ServerUser} that started this direct message thread.
	 */
	ServerUser getStarter();
}
