package org.sarge.jove.platform.vulkan.generator;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

/**
 * A <i>structure field</i> composes a field name and type information.
 * @author Sarge
 */
record StructureField<T>(String name, T type, int length) {
	/**
	 * Constructor.
	 * @param name		Field name
	 * @param type		Type information
	 * @param length	Array length
	 */
	public StructureField {
		requireNotEmpty(name);
		requireNonNull(type);
		requireZeroOrMore(length);
	}

	public StructureField(String name, T type) {
		this(name, type, 0);
	}

	/**
	 * Replaces the type of this field.
	 * @param <R> Structure field type
	 * @param type New field type
	 * @return Modified structure field
	 */
	public <R> StructureField<R> with(R type) {
		return new StructureField<>(name, type, length);
	}
}
