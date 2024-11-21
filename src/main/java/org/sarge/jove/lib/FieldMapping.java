package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.List;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

/**
 * A <i>field mapping</i> marshals a structure field to/from its native representation.
 * @author Sarge
 */
class FieldMapping {
	/**
	 * FFM marshalling adapter for off-heap structure fields.
	 */
	private interface FieldAdapter {
		void marshal(Object actual, MemorySegment address);
		Object unmarshal(MemorySegment address);
	}

	private final Field field;
	private final NativeMapper<?> mapper;
	private final FieldAdapter handle;

	/**
	 * Constructor.
	 * @param field		Field
	 * @param mapper	Native mapper for this structure field
	 * @param handle	Marshalling adapter for the off-heap field
	 * @throws IllegalArgumentException if {@link #field} is not a valid structure field
	 */
	FieldMapping(Field field, NativeMapper<?> mapper, FieldAdapter handle) {
		if(!isStructureField(field)) throw new IllegalArgumentException("Invalid structure field: " + field);
		this.field = requireNonNull(field);
		this.mapper = requireNonNull(mapper);
		this.handle = requireNonNull(handle);
	}

	/**
	 * @return Whether the given field is a valid structure member
	 */
	private static boolean isStructureField(Field field) {
		final int modifiers = field.getModifiers();
		return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
	}

	/**
	 * Marshals the fields of the given structure to off-heap memory.
	 * @param structure		Structure
	 * @param address		Off-heap memory
	 * @param context		Context
	 * @throws Exception if the field cannot be marshalled
	 */
	public void marshal(NativeStructure structure, MemorySegment address, NativeContext context) throws Exception {
		final Object value = field.get(structure);
		final Object actual = context.marshal(mapper, value, field.getType());
		handle.marshal(actual, address);
	}

	/**
	 * Marshals off-heap memory to the given structure.
	 * @param address		Off-heap memory
	 * @param structure		Structure
	 * @throws Exception if the field cannot be unmarshalled
	 */
	public void unmarshal(MemorySegment address, NativeStructure structure) throws Exception {
		final Object value = handle.unmarshal(address);
		if(!(mapper instanceof ReturnMapper m)) throw new IllegalArgumentException();
		@SuppressWarnings("unchecked")
		final Object actual = m.unmarshal(value, field.getType());
		field.set(structure, actual);
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
	 * @param structure		Structure layout
	 * @param type			Structure type
	 * @param registry		Native mappers
	 * @return Field mappings
	 * @throws IllegalArgumentException for an unknown or unsupported field
	 */
	protected static List<FieldMapping> build(StructLayout structure, Class<? extends NativeStructure> type, NativeMapperRegistry registry) {
		/**
		 * Helper for building the field mappings.
		 */
		final var builder = new Object() {
			/**
			 * Builds the field mapping for the given memory layout.
			 */
			FieldMapping build(MemoryLayout layout) {
				final String name = layout.name().get();
				try {
					return build(name, layout);
				}
				catch(Exception e) {
					throw new IllegalArgumentException(String.format("Cannot build mapping for structure field [%s.%s]", type.getName(), name), e);
				}
			}

			/**
			 * Builds the field mapping.
			 */
			private FieldMapping build(String name, MemoryLayout layout) throws Exception {
				final var path = PathElement.groupElement(name);
				final FieldAdapter adapter = adapter(path, layout);
				final Field field = type.getField(name);
				final NativeMapper<?> mapper = mapper(field);
				return new FieldMapping(field, mapper, adapter);
			}

			/**
			 * Creates the adapter for the off-heap field.
			 */
			private FieldAdapter adapter(PathElement path, MemoryLayout layout) {
				if(layout instanceof SequenceLayout sequence) {
					return array(path, sequence);
				}
				else {
					return varHandle(path);
				}
			}

			/**
			 * Creates a field adapter for an address or primitive layout implemented using a {@link VarHandle}.
			 */
			private FieldAdapter varHandle(PathElement path) {
				// TODO - insertCoordinates
				return new FieldAdapter() {
					private final VarHandle handle = structure.varHandle(path);

					@Override
					public void marshal(Object arg, MemorySegment address) {
						handle.set(address, 0L, arg);
					}

					@Override
					public Object unmarshal(MemorySegment address) {
						return handle.get(address, 0L);
					}
				};
			}

			/**
			 * Creates a field adapter for an array implemented using {@link MemorySegment#asSlice(long)}.
			 */
			private FieldAdapter array(PathElement path, SequenceLayout sequence) {
				final long size = sequence.byteSize();
				if(size == 0) {
					// https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/lang/foreign/MemoryLayout.html#layout-align
					throw new UnsupportedOperationException(); // TODO
				}

				return new FieldAdapter() {
					private final long offset = structure.byteOffset(path);

					@Override
					public void marshal(Object actual, MemorySegment address) {
						// TODO
						System.err.println("marshal array="+path);
					}

					@Override
					public Object unmarshal(MemorySegment address) {
						return address.asSlice(offset, size);
					}
				};
			}

			/**
			 * @return Native mapper for the given structure field
			 */
			private NativeMapper<?> mapper(Field field) {
				return registry
						.mapper(field.getType())
						.orElseThrow(() -> new IllegalArgumentException("Unsupported structure field type: " + field.getType()));
			}
		};

		// Build mapping for each declared structure field
		return structure
				.memberLayouts()
				.stream()
				.filter(e -> e.name().isPresent())
				.map(builder::build)
				.toList();
	}
}
