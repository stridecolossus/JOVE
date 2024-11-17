package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.*;

/**
 * A <i>field mapping</i> is used to marshal a structure field to/from its native representation.
 * @author Sarge
 */
class FieldMapping {
	private final Field field;
	private final VarHandle handle;
	private final NativeMapper<?> mapper;

	/**
	 * Constructor.
	 * @param field		Field
	 * @param handle	Native field handle
	 * @param mapper	Native mapper for this field
	 */
	FieldMapping(Field field, VarHandle handle, NativeMapper<?> mapper) {
		this.field = requireNonNull(field);
		this.handle = requireNonNull(handle);
		this.mapper = requireNonNull(mapper);
	}

	/**
	 * Marshals the fields of the given structure.
	 * @param structure		Structure
	 * @param address		Off-heap instance
	 * @param context		Context
	 */
	void toNative(NativeStructure structure, MemorySegment address, NativeContext context) {
		final Object value = get(structure);
		final Object actual = context.toNative(mapper, value, field.getType());
//System.out.println("field " + field + " [" + value + "] -> [" + actual + "]");
		handle.set(address, 0L, actual);
	}

	void fromNative(MemorySegment address, NativeStructure structure) {
		// TODO
	}

	/**
	 * Retrieves the field value for this mapping.
	 * @param structure Structure instance
	 * @return Field value
	 */
	private Object get(NativeStructure structure) {
		try {
			return field.get(structure);
		}
		catch(Exception e) {
			throw new RuntimeException(String.format("Cannot retrieve structure field %s.%s", structure, field), e);
		}
	}

	private void set(NativeStructure structure, Object value) {
		// TODO
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof FieldMapping that) &&
				this.field.equals(that.field);
	}

	@Override
	public String toString() {
		final String type = field.getDeclaringClass().getSimpleName();
		return String.format("FieldMapping[%s.%s]", type, field.getName());
	}

	/**
	 * Builds the field mappings for the given structure.
	 * @param layout		Structure layout
	 * @param type			Structure type
	 * @param registry		Native mappers
	 * @return Field mappings
	 * @throws IllegalArgumentException for an unknown or unsupported field
	 */
	protected static List<FieldMapping> build(StructLayout layout, Class<? extends NativeStructure> type, NativeMapperRegistry registry) {
		// Init field mapping builder
		final var builder = new Object() {
			/**
			 * Builds the field mapping for the given structure field.
			 * @param field Structure field
			 * @return Field mapping
			 */
			FieldMapping build(Field field) {
				final VarHandle handle = handle(field.getName());
				final NativeMapper<?> mapper = mapper(field);
				return new FieldMapping(field, handle, mapper);
			}

			private NativeMapper<?> mapper(Field field) {
				return registry
						.mapper(field.getType())
						.orElseThrow(() -> new IllegalArgumentException("Unsupported structure field: " + field));
			}

			/**
			 * Creates a handle to an off-heap structure field.
			 * @param name Field name
			 * @return Field handle
			 */
			private VarHandle handle(String name) {
				try {
					return layout.varHandle(PathElement.groupElement(name));
				}
				catch(IllegalArgumentException e) {
					throw new IllegalArgumentException(String.format("%s for structure field %s.%s", e, name, type));
				}
			}
		};

		// Build mapping for each top-level public field in the structure
		return Arrays
				.stream(type.getDeclaredFields())
				.filter(FieldMapping::isStructureField)
				.map(builder::build)
				.toList();
	}

	private static boolean isStructureField(Field field) {
		final int modifiers = field.getModifiers();
		return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
	}
}
