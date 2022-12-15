package pala.apps.arlith.app.server.world;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import pala.apps.arlith.app.server.contracts.world.ServerCommunityThread;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.libs.generic.json.JSONObject;

class ServerCommunityThreadImpl extends ServerThreadImpl implements ServerCommunityThread {

	private static final String NAME_KEY = "name", COMMUNITY_KEY = "community";
	/**
	 *
	 */
	private final ServerCommunityImpl community;
	private String name;

	public ServerCommunityThreadImpl(final ServerCommunityImpl community, final String name) {
		super(community.getWorld());
		this.community = community;
		this.name = name;
	}

	public ServerCommunityThreadImpl(final ServerWorldImpl world, final JSONObject snap) {
		super(world, snap);
		community = world.getRegistry().getCommunity(getGID(snap, COMMUNITY_KEY));
		name = getString(snap, NAME_KEY);
	}

	@Override
	public void delete() {
		deleteAsChild();
		community.save();// Also save.
	}

	/**
	 * Deletes this community thread <i>as a child</i> of its parent community. For
	 * more details on what deletion as a child means, see
	 * {@link ServerMessageImpl#deleteAsChild()}.
	 */
	void deleteAsChild() {
		// Perform the deletion code that is specific to this thread.
		while (!messages.isEmpty())
			messages.get(0).deleteAsChild();// Delete each child message WITHOUT calling ServerCommunityThread#save() on
											// this object every time a child is deleted.
		community.threads.remove(this);
		deleteFile();
	}

	@Override
	public ServerCommunityImpl getCommunity() {
		return community;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<? extends ServerUser> getParticipants() {
		return Collections.unmodifiableSet(community.participants);
	}

	@Override
	public File getStorageFile() {
		return new File(getWorld().getCommunityThreadPath(), getGID().getHex() + ".aso");
	}

	@Override
	public void restore(final JSONObject snap) throws IllegalArgumentException {
		// IMPLEMENT Auto-generated method stub
		super.restore(snap);
	}

	@Override
	public void setName(final String newName) {
		name = newName;
		save();
	}

	@Override
	public JSONObject snapshot() {
		return super.snapshot().put(NAME_KEY, name).put(COMMUNITY_KEY, community.getGID().getHex());
	}

	@Override
	public String toString() {
		return getName() + "[position=" + getCommunity().indexOf(this) + ", members=" + getParticipants()
				+ ", community=" + getCommunity() + ']';
	}

}