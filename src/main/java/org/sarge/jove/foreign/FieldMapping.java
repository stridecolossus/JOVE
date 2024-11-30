package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.List;

/**
 * A <i>field mapping</i> marshals structure fields to/from off-heap memory.
 * @author Sarge
 */
interface FieldMapping {
	/**
	 * Marshals this field from the given structure to off-heap memory.
	 * @param structure		Structure instance
	 * @param address		Memory address
	 * @param allocator		Allocator
	 */
	void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator);

	/**
	 * Unmarshals this field from off-heap memory to the given structure.
	 * @param address		Memory address
	 * @param structure		Structure
	 */
	void unmarshal(MemorySegment address, NativeStructure structure);

	static String format(String message, Class<?> type, String field) {
		return String.format("%s [%s.%s]", message, type.getSimpleName(), field);
	}

	/**
	 * Helper - Retrieves a structure field value.
	 * @param structure		Structure
	 * @param field 		Field
	 * @return Field value
	 */
	private static Object get(NativeStructure structure, Field field) {
		try {
			return field.get(structure);
		}
		catch(Exception e) {
			throw new RuntimeException(format("Cannot retrieve structure field", structure.getClass(), field.getName()));
		}
	}

	// TODO
	FieldMapping MOCK = new FieldMapping() {
		@Override
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
		}

		@Override
		public void unmarshal(MemorySegment address, NativeStructure structure) {
		}
	};

	/**
	 * An <i>atomic field mapping</i> marshals a structure field using a {@link VarHandle}.
	 */
	@SuppressWarnings("rawtypes")
	record AtomicFieldMapping(Field field, NativeTransformer transformer, VarHandle handle) implements FieldMapping {
		@Override
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			final Object value = FieldMapping.get(structure, field);
			final Object foreign = NativeTransformer.transform(value, transformer, allocator);
			handle.set(address, 0L, foreign);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void unmarshal(MemorySegment address, NativeStructure structure) {
			final Object foreign = handle.get(address, 0L);
			final Object value = transformer.returns().apply(foreign);
			set(value, structure);
		}

		private void set(Object value, NativeStructure structure) {
			try {
				field.set(structure, value);
			}
			catch(Exception e) {
				throw new RuntimeException(FieldMapping.format("Cannot set structure field", structure.getClass(), field.getName()), e);
			}
		}
	}

	/**
	 * A <i>structure field mapping</i> recurses to an embedded structure.
	 */
	record StructureFieldMapping(Field field, List<FieldMapping> mappings) implements FieldMapping {
		private NativeStructure child(NativeStructure parent) {
			return (NativeStructure) FieldMapping.get(parent, field);
		}

		@Override
		public void marshal(NativeStructure parent, MemorySegment address, SegmentAllocator allocator) {
			final NativeStructure child = child(parent);
			for(var m : mappings) {
				m.marshal(child, address, allocator);
			}
		}

		@Override
		public void unmarshal(MemorySegment address, NativeStructure parent) {
			final NativeStructure child = child(parent);
			for(var m : mappings) {
				m.unmarshal(address, child);
			}
		}
	}

	/**
	 * Builds the field mappings for a native structure.
	 * @param layout		Structure layout
	 * @param type			Type
	 * @param registry		Native transformers
	 * @return Field mappings
	 */
	static List<FieldMapping> build(StructLayout layout, Class<? extends NativeStructure> type, TransformerRegistry registry) {
		// Init builder instance
		final var builder = new Object() {
			/**
			 * Builds the field mapping for the given member of the structure.
			 */
			@SuppressWarnings({"rawtypes", "unchecked"})
			public FieldMapping mapping(MemoryLayout member) {
				// Lookup structure field corresponding to this member
				final String name = member.name().get();
				final Field field = field(name);

				// Find native transformer for this mapping
				final NativeTransformer transformer = registry.get(field.getType());

				// Create mapping
				return switch(member) {
					case ValueLayout __ -> {
						final var path = PathElement.groupElement(name);
						yield new AtomicFieldMapping(field, transformer, layout.varHandle(path));
					}

					case StructLayout child -> {
						final var clazz = (Class<? extends NativeStructure>) field.getType();
						final List<FieldMapping> children = build(child, clazz, registry);
						yield new StructureFieldMapping(field, children);
					}

					case SequenceLayout __ -> MOCK; // TODO

					default -> throw new RuntimeException(format("Unexpected memory layout %s for field".formatted(member), type, field.getName()));
				};
			}

			/**
			 * Reflects a structure field.
			 */
			private Field field(String name) {
				try {
					return type.getDeclaredField(name);
				}
				catch(NoSuchFieldException e) {
					throw new IllegalArgumentException(format("Unknown structure field", type, name), e);
				}
				catch(Exception e) {
					throw new RuntimeException(format("Error reflecting structure field", type, name), e);
				}
			}
		};

		// Build field mappings specified by the memory layout
		return layout
				.memberLayouts()
				.stream()
				.filter(e -> e.name().isPresent())
				.map(builder::mapping)
				.toList();
	}
}
