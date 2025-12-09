package org.sarge.jove.platform.vulkan.generator;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.lang.foreign.*;

/**
 * A <i>native type</i> specifies a domain type mapping for a structure field.
 * @author Sarge
 */
public record NativeType(String name, MemoryLayout layout) {
	/**
	 * Constructor.
	 * @param name			Type name
	 * @param layout		Memory layout
	 */
	public NativeType {
		requireNotEmpty(name);
		requireNonNull(layout);
	}

	/**
	 * Appends a Java array declaration to the name of the type.
	 * Note that that actual memory layout is unchanged.
	 * @return This type as an array
	 */
	public NativeType array() {
		return new NativeType(name + "[]", layout);
	}

	/**
	 * Creates a primitive native type.
	 * @param layout Memory layout
	 * @return Primitive type
	 */
	public static NativeType of(ValueLayout layout) {
		final String name = layout.carrier().getSimpleName();
		return new NativeType(name, layout);
	}
}
