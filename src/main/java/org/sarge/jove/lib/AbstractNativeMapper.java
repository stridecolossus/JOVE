package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.MemoryLayout;
import java.util.Objects;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractNativeMapper<T> implements NativeMapper<T> {
	private final Class<T> type;
	private final MemoryLayout layout;

	/**
	 * Constructor.
	 * @param type			Java type
	 * @param layout		Native layout
	 */
	protected AbstractNativeMapper(Class<T> type, MemoryLayout layout) {
		this.type = requireNonNull(type);
		this.layout = requireNonNull(layout);
	}

	@Override
	public final Class<T> type() {
		return type;
	}

	@Override
	public final MemoryLayout layout() {
		return layout;
	}


	@Override
	public Object toNativeNull(Class<?> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, layout);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeMapper that) &&
				this.type.equals(that.type()) &&
				this.layout.equals(that.layout());
	}

	@Override
	public String toString() {
		return String.format("NativeMapper[%s -> %s]", type, layout);
	}
}
