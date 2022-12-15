package pala.apps.arlith.api;

public class UserReference {
	private final String username, disc;

	public UserReference(String username, String disc) {
		this.username = username;
		this.disc = disc;
	}

	public String getUsername() {
		return username;
	}

	public String getDisc() {
		return disc;
	}

	@Override
	public String toString() {
		return getUsername() + '#' + getDisc();
	}

}
