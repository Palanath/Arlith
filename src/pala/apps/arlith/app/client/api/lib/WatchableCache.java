package pala.apps.arlith.app.client.api.lib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.apps.arlith.api.watchables.View;
import pala.apps.arlith.api.watchables.Watchable;
import pala.apps.arlith.api.watchables.Watcher;
import pala.apps.arlith.app.client.api.lib.ClientCache.Querier;
import pala.apps.arlith.app.client.requests.v2.RequestSubsystemInterface;

public final class WatchableCache<O> extends Cache<O> implements Watchable<O> {

	private final List<Watcher<? super O>> watchers = new ArrayList<>(5);
	private final Populator populator;

	private WeakReference<View<O>> view;

	public View<O> getView() {
		if (view != null) {
			View<O> v = view.get();
			if (v != null)
				return v;
		}
		View<O> v = View.view(this);
		view = new WeakReference<>(v);
		return v;
	}

	public WatchableCache(Populator populator) {
		this.populator = populator;
	}

	public WatchableCache(Supplier<? extends RequestSubsystemInterface> reqsys, Populator populator) {
		super(reqsys);
		this.populator = populator;
	}

	public WatchableCache(Querier<? extends O> populator) {
		this.populator = connection -> populate(populator.query(connection));
	}

	public WatchableCache(Supplier<? extends RequestSubsystemInterface> reqsys, Querier<? extends O> populator) {
		super(reqsys);
		this.populator = connection -> populate(populator.query(connection));
	}

	public @FunctionalInterface interface Populator {
		void populate(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException;
	}

	@Override
	public O getValue() {
		return value;
	}

	@Override
	public void register(Watcher<? super O> watcher) {
		watchers.add(watcher);
	}

	@Override
	public void unregister(Watcher<? super O> watcher) {
		watchers.remove(watcher);
	}

	@Override
	public synchronized void update(O newValue) {
		O old = value;
		value = newValue;
		populated = true;
		for (Watcher<? super O> w : watchers)
			w.watch(old, value);
	}

	@Override
	public synchronized void populate(O value) {
		super.populate(value);
		for (Watcher<? super O> w : watchers)
			w.watch(null, value);

	}

	@Override
	protected synchronized void populate(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException {
		populator.populate(connection);
	}

}
