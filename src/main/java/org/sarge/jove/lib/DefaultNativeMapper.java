package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.ValueLayout;
import java.util.Objects;

/**
 * The <i>default native mapper</i> maps built-in Java types and primitives to/from the equivalent native representation.
 * @author Sarge
 */
public class DefaultNativeMapper implements NativeMapper {
	private final Class<?> type;
	private final ValueLayout layout;

	/**
	 * Constructor.
	 * @param type			Java type
	 * @param layout		Native layout
	 */
	public DefaultNativeMapper(Class<?> type, ValueLayout layout) {
		this.type = requireNonNull(type);
		this.layout = requireNonNull(layout);
	}

	@Override
	public Class<?> type() {
		return type;
	}

	@Override
	public ValueLayout layout() {
		return layout;
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
