package pala.apps.arlith.api.communication.gids;

public interface Identifiable extends Comparable<Identifiable> {
	GID id();

	default byte[] idBytes() {
		return id().getBytes();
	}

	default String idHex() {
		return id().getHex();
	}

	default String idNumb() {
		return id().getNumber();
	}

	@Override
	default int compareTo(Identifiable o) {
		return id().compareTo(o.id());
	}
}
