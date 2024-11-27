package org.sarge.jove.foreign;

import static org.sarge.lib.Validation.requireZeroOrMore;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.reflect.Field;
import java.util.List;

/**
 * A <i>structure field mapping</i> composes the enumerated structure fields to be marshalled to/from off-heap memory.
 * @author Sarge
 */
record StructureFieldMapping(long offset, long size, List<StructureField> fields) implements FieldMapping<NativeStructure> {
	/**
	 * A <i>structure field</i> marshals a structure field to/from off-heap memory.
	 */
	@SuppressWarnings("rawtypes")
	private record StructureField(Field field, NativeMapper mapper, FieldMapping mapping) {
		/**
		 * Marshals a structure field.
		 * @param structure		Structure
		 * @param address		Memory address
		 * @param allocator		Allocator
		 */
		@SuppressWarnings("unchecked")
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			final Object value = structure.get(field);
			final Object foreign = NativeMapper.marshal(value, mapper, allocator);
			mapping.marshal(foreign, address, allocator);
		}

		/**
		 * Unmarshals a structure field.
		 * @param address		Memory address
		 * @param structure		Structure
		 */
		@SuppressWarnings("unchecked")
		public void unmarshal(MemorySegment address, NativeStructure structure) {
			// TODO - doc
			if(mapping instanceof StructureFieldMapping) {
				mapping.unmarshal(address, (NativeStructure) structure.get(field));
				return;
			}

			// Retrieve the native field from the off-heap memory
			final Object foreign = mapping.unmarshal(address, structure);
			final Object value = mapper.returns().apply(foreign);
			set(value, structure);
		}

		private void set(Object value, NativeStructure structure) {
			try {
				field.set(structure, value);
			}
			catch(Exception e) {
				throw new RuntimeException(String.format("Cannot set structure field [%s.%s]", structure.getClass().getSimpleName(), field.getName()), e);
			}
		}
	}

	/**
	 * Constructor.
	 * @param offset		Memory offset
	 * @param fields		Structure fields
	 */
	public StructureFieldMapping {
		requireZeroOrMore(offset);
		fields = List.copyOf(fields);
	}

	@Override
	public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
		// Slice structure memory
		// TODO - ignore if zero offset?
		final MemorySegment segment = address.asSlice(offset, size);

		// Marshal structure fields
		for(StructureField m : fields) {
			m.marshal(structure, segment, allocator);
		}
	}

	@Override
	public Object unmarshal(MemorySegment address, NativeStructure structure) {
		// Slice structure memory
		// TODO - ignore if zero offset?
		final MemorySegment segment = address.asSlice(offset, size);

		// Unmarshal structure fields
		for(StructureField m : fields) {
			m.unmarshal(segment, structure);
		}

		return address;
	}

	/**
	 * Builds the field mappings for the given structure.
	 * @param layout		Structure layout
	 * @param offset		Memory offset
	 * @param type			Structure type
	 * @param registry		Native mappers
	 * @return Field mappings
	 * @throws IllegalArgumentException for an unknown or unsupported field
	 */
	static StructureFieldMapping build(StructLayout layout, long offset, Class<? extends NativeStructure> type, NativeMapperRegistry registry) {
		// Init builder for structure fields
		final var builder = new Object() {
			/**
			 * Builds the structure field descriptor for the given member.
			 * @param member Field member layout
			 * @return Structure field descriptor
			 */
			public StructureField build(MemoryLayout member) {
				final String name = member.name().get();
				final Field field = field(name);
				final FieldMapping<?> marshaller = marshaller(field, member);
				final NativeMapper<?, ?> mapper = registry.mapper(field.getType());
				return new StructureField(field, mapper, marshaller);
			}

			/**
			 * Reflects a structure field.
			 */
			private Field field(String name) {
				try {
					return type.getDeclaredField(name);
				}
				catch(NoSuchFieldException e) {
					throw new IllegalArgumentException(String.format("Error reflecting structure field [%s.%s]", type.getName(), name), e);
				}
				catch(Exception e) {
					throw new RuntimeException(e);
				}
			}

			/**
			 * Builds the field marshaller for the given structure field.
			 * @param field			Structure field
			 * @param member		Field layout
			 * @return Marshaller
			 */
			private FieldMapping<?> marshaller(Field field, MemoryLayout member) {
				// Lookup structure field from layout
				final PathElement path = PathElement.groupElement(field.getName());
                final long offset = layout.byteOffset(path);

				// Build marshaller for structure field
				return switch(member) {
					case ValueLayout __ -> new AtomicFieldMapping(layout.varHandle(path));

					case StructLayout struct -> {
						// Recurse for embedded structures
						// TODO - can we not just 'add' these rather than wrapping into another layer? i.e. embedded structure only needs the 'root' structure?
						@SuppressWarnings("unchecked") final var type = (Class<? extends NativeStructure>) field.getType();
						yield StructureFieldMapping.build(struct, offset, type, registry);
					}

					// TODO
					case SequenceLayout sequence -> new ArrayFieldMapping();

    				default -> throw new RuntimeException(String.format("Unexpected memory layout %s for field [%s.%s]", member, type.getName(), field.getName()));
				};
			}
		};

		// Enumerate structure fields specified by the memory layout
		final List<StructureField> mappings = layout
				.memberLayouts()
				.stream()
				.filter(e -> e.name().isPresent())
				.map(builder::build)
				.toList();

		// Create structure mapping
		final long size = layout.byteSize();
		return new StructureFieldMapping(offset, size, mappings);
	}
}
