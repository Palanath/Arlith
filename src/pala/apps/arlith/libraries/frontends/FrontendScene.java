package pala.apps.arlith.libraries.frontends;

public interface FrontendScene<F extends Frontend> {
	/**
	 * Returns the {@link Frontend} main-class for the Frontend that this scene is a
	 * part of.
	 * 
	 * @return The {@link Frontend} class that this scene belongs to.
	 */
	F getFrontend();

	/**
	 * Causes this scene to be shown to the user.
	 */
	void show() throws SceneShowFailureException;

	/**
	 * Thrown when a {@link FrontendScene} cannot be shown
	 * 
	 * @author Palanath
	 *
	 */
	class SceneShowFailureException extends Exception {

		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;

		private final FrontendScene<?> scene;

		public SceneShowFailureException(FrontendScene<?> scene) {
			this.scene = scene;
		}

		public SceneShowFailureException(String message, FrontendScene<?> scene) {
			super(message);
			this.scene = scene;
		}

		public SceneShowFailureException(Throwable cause, FrontendScene<?> scene) {
			super(cause);
			this.scene = scene;
		}

		public SceneShowFailureException(String message, Throwable cause, FrontendScene<?> scene) {
			super(message, cause);
			this.scene = scene;
		}

		protected SceneShowFailureException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace, FrontendScene<?> scene) {
			super(message, cause, enableSuppression, writableStackTrace);
			this.scene = scene;
		}

		public FrontendScene<?> getScene() {
			return scene;
		}

	}
}
