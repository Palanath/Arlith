package pala.apps.arlith.app.guis;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import pala.apps.arlith.api.graphics.windows.Page;
import pala.apps.arlith.api.watchables.BindableImpl;
import pala.apps.arlith.graphics.windows.ArlithWindow;

public abstract class BindHandlerPage implements Page {

	private final Set<PageBindable<?>> bindables = new HashSet<>();

	public class PageBindable<S> extends BindableImpl<S> {

		private final Consumer<? super S> handler;

		public PageBindable(Consumer<? super S> handler) {
			this.handler = handler;
		}

		@Override
		public void set(S value) {
			handler.accept(value);
		}

		@Override
		public void unbind() {
			super.unbind();
			bindables.remove(this);
		}

		private void clean() {
			super.unbind();
		}

	}

	@Override
	public void cleanup(ArlithWindow window) {
		bindables.forEach(PageBindable::clean);
		bindables.clear();
	}

	/**
	 * Creates a {@link PageBindable} that will automatically be cleaned after this
	 * 
	 * @param <S>     The type of the {@link PageBindable}.
	 * @param handler The handler that will receive the changes.
	 * @return The new {@link PageBindable}.
	 */
	public final <S> PageBindable<S> bindable(Consumer<? super S> handler) {
		PageBindable<S> bindable = new PageBindable<>(handler);
		bindables.add(bindable);
		return bindable;
	}
}
