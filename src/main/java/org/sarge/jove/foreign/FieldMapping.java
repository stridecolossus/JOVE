package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.List;
import java.util.function.*;

/**
 * A <i>field mapping</i> marshals a structure field to/from native memory.
 * @author Sarge
 */
interface FieldMapping {
	/**
	 * Marshals this field to off-heap memory.
	 * @param structure		Structure
	 * @param address		Off-heap address
	 * @param allocator		Allocator
	 */
	void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator);

	/**
	 * Unmarshals this field from off-heap memory.
	 * @param address		Off-heap memory
	 * @param structure		Structure
	 */
	void unmarshal(MemorySegment address, NativeStructure structure);

	/**
	 * A <i>default field mapping</i> marshals an atomic structure field.
	 */
	record DefaultFieldMapping(VarHandle local, VarHandle foreign, Transformer<?> transformer) implements FieldMapping {
		/**
		 * Constructor.
		 * @param local				Structure field
		 * @param foreign			Off-heap field
		 * @param transformer		Transformer
		 */
		public DefaultFieldMapping {
			requireNonNull(local);
			requireNonNull(foreign);
			requireNonNull(transformer);
		}

		@Override
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			final Object value = local.get(structure);
			final Object arg = Transformer.marshal(value, transformer, allocator);
			foreign.set(address, arg);
		}

		@Override
		@SuppressWarnings({"rawtypes", "unchecked"})
		public void unmarshal(MemorySegment address, NativeStructure structure) {
			final Object arg = foreign.get(address);
			final Function unmarshal = transformer.unmarshal();			// TODO - cache
			final Object value = unmarshal.apply(arg);
			local.set(structure, value);
		}
	}

	/**
	 * A <i>compound field mapping</i> marshals a top-level or nested structure.
	 */
	record CompoundFieldMapping(StructLayout layout, Supplier<NativeStructure> factory, List<FieldMapping> fields) implements FieldMapping {
		/**
		 * Constructor.
		 * @param layout		Structure layout
		 * @param factory		Factory for new structure instances
		 * @param fields		Field mappings
		 */
		public CompoundFieldMapping {
			requireNonNull(layout);
			requireNonNull(factory);
			fields = List.copyOf(fields);
		}

		@Override
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			for(var f : fields) {
				f.marshal(structure, address, allocator);
			}
		}

		@Override
		public void unmarshal(MemorySegment address, NativeStructure structure) {
			for(var f : fields) {
				f.unmarshal(address, structure);
			}
		}

		/**
		 * Unmarshals a new structure instance from the given off-heap memory.
		 * @param address Off-heap address
		 * @return New structure
		 * @see #unmarshal(MemorySegment, NativeStructure)
		 */
		public NativeStructure unmarshal(MemorySegment address) {
			final var structure = factory.get();
			for(var f : fields) {
				f.unmarshal(address, structure);
			}
			return structure;
		}
	}

	/**
	 * The <i>field mapping builder</i> generates the field mappings for a structure.
	 */
	class Builder {
		private final Lookup lookup = MethodHandles.lookup();
		private final Registry registry;

		/**
		 * Constructor.
		 * @param registry Transformer registry
		 */
		public Builder(Registry registry) {
			this.registry = requireNonNull(registry);
		}

		/**
		 * Builds the field mappings for a structure of the given type.
		 * @param type Structure type
		 * @return Field mappings
		 */
		public CompoundFieldMapping build(Class<? extends NativeStructure> type) {
			// Lookup the default constructor for this structure
			//final Constructor<? extends NativeStructure> constructor = constructor(type);
			final Supplier<NativeStructure> factory = factory(type);

			// Extract the structure layout from a temporary instance
			final NativeStructure instance = factory.get();
			final StructLayout layout = instance.layout();

			// Build the field mappings for the structure
			final var helper = new Helper(type, layout);
			final List<FieldMapping> mappings = helper.build();

			// Create compound wrapper
			return new CompoundFieldMapping(layout, factory, mappings);
		}

		/**
		 * Helper for recursively building field mappings.
		 */
		private class Helper {
			private final Class<? extends NativeStructure> type;
			private final StructLayout layout;

			public Helper(Class<? extends NativeStructure> type, StructLayout layout) {
				this.type = type;
				this.layout = layout;
			}

			/**
			 * @return Field mappings
			 */
			public List<FieldMapping> build() {
    			return layout
    					.memberLayouts()
    					.stream()
    					.filter(Predicate.not(e -> e instanceof PaddingLayout))
    					.map(this::mapping)
    					.toList();
			}

			/**
			 * Builds a field mapping for a given structure member.
			 */
			private FieldMapping mapping(MemoryLayout member) {
				// Lookup structure field
				final String name = member.name().orElseThrow(() -> new IllegalArgumentException("Anonymous structure member %s in %s".formatted(member, type)));
				final Field field = field(name);

				// Create mapping
				return switch(member) {
					case ValueLayout _			-> atomic(field);
					case StructLayout nested	-> nested(field, nested);
					default -> throw new IllegalArgumentException("Unsupported member layout %s in field %s::%s".formatted(member, type, name));
				};
			}

			/**
			 * Looks up a structure field via reflection.
			 */
			private Field field(String name) {
				try {
					return type.getField(name);
				}
				catch(NoSuchFieldException e) {
					throw new IllegalArgumentException("Unknown structure field %s::%s".formatted(type, name), e);
				}
				catch(Exception e) {
					throw new RuntimeException("Error looking up structure field %s::%s ".formatted(type, name), e);
				}
			}

			/**
			 * Builds an atomic field mapping.
			 */
			private DefaultFieldMapping atomic(Field field) {
				final PathElement path = PathElement.groupElement(field.getName());
				final VarHandle local = handle(field);
				final VarHandle foreign = Transformer.removeOffset(layout.varHandle(path));
				final Transformer<?> transformer = registry.transformer(field.getType());
				return new DefaultFieldMapping(local, foreign, transformer);
			}

			/**
			 * @return Foreign handle for the given field
			 */
			private VarHandle handle(Field field) {
				try {
					return lookup.unreflectVarHandle(field);
				}
				catch(IllegalAccessException e) {
					throw new RuntimeException("Error accessing field: %s::%s".formatted(type, field.getName()), e);
				}
			}

			/**
			 * Recursively builds the field mappings for a nested structure.
			 */
			private CompoundFieldMapping nested(Field field, StructLayout nested) {
				// Lookup the default constructor for the nested structure
				final var child = (Class<? extends NativeStructure>) field.getType(); // TODO - check
				final Supplier<NativeStructure> factory = factory(child);

				// Recursively build field mappings
				final Helper recurse = new Helper(child, nested);
				final List<FieldMapping> mappings = recurse.build();

				// Create wrapper
				return new CompoundFieldMapping(nested, factory, mappings);
			}
		}

		/**
		 * @return Factory for the given structure type
		 */
		private static Supplier<NativeStructure> factory(Class<? extends NativeStructure> type) {
			try {
				final var constructor = type.getConstructor();
				return () -> instance(constructor);
			}
			catch(Exception e) {
				throw new RuntimeException("Cannot find default constructor for structure: " + type, e);
			}
		}

		private static NativeStructure instance(Constructor<? extends NativeStructure> constructor) {
			try {
				return constructor.newInstance();
			}
			catch(Exception e) {
				throw new RuntimeException("Cannot instantiate structure: " + constructor, e);
			}
		}
	}
}
