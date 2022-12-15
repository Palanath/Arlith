package pala.apps.arlith.api.communication.protocol.types;

abstract class BoundedTypeValue {
	private final Integer min, max;

	public BoundedTypeValue(Integer min, Integer max) {
		this.min = min;
		this.max = max;
	}

	public BoundedTypeValue(Integer bound, boolean lower) {
		this(lower ? bound : null, lower ? null : bound);
	}

	public BoundedTypeValue(int minimum) {
		this(minimum, null);
	}

	public BoundedTypeValue() {
		this(null, null);
	}

	public boolean hasLowerBound() {
		return min != null;
	}

	public boolean hasUpperBound() {
		return max != null;
	}

	public int getLowerBound() {
		return min;
	}

	public int getUpperBound() {
		return max;
	}

	/**
	 * @return The size of this bounded type's value, or <code>null</code> if this
	 *         object currently does not store a value.
	 * @author Palanath
	 */
	public abstract Integer size();

	protected final void checkSize(int size) {
		if (max != null && size > max)
			throw new IllegalArgumentException("Value above size maximum.");
		else if (min != null && size < min)
			throw new IllegalArgumentException("Value below size minimum.");
	}

	protected void checkSize() {
		checkSize(size());
	}
}
