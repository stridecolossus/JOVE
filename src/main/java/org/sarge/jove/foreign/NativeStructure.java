package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.List;
import java.util.function.*;

import org.sarge.jove.foreign.NativeStructure.StructureTransformer.FieldMapping;

/**
 * A <i>native structure</i> is the base type for all JOVE structures.
 * @author Sarge
 */
public interface NativeStructure {
	/**
	 * Memory layout for a pointer field of a structure.
	 */
    AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_BYTE));

    /**
     * Default padding to ensure structure fields are correctly aligned.
     */
    MemoryLayout PADDING = MemoryLayout.paddingLayout(4);

    /**
	 * @return Memory layout of this structure
	 */
    GroupLayout layout();

    /**
     *
     */
    class StructureTransformer extends DefaultTransformer<NativeStructure> {
		/**
		 * A <i>field mapping</i> associates a structure field and the corresponding off-heap memory.
		 */
		record FieldMapping(VarHandle local, VarHandle foreign, Transformer transformer) {
			/**
			 * Marshals this structure field.
			 * @param structure		Structure
			 * @param address		Off-heap memory
			 * @param allocator		Allocator
			 */
			public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
				final Object value = local.get(structure);
				final Object result = TransformerHelper.marshal(value, transformer, allocator);
				foreign.set(address, result);
			}

			/**
			 * Unmarshals this structure field to the given instance.
			 * @param address		Off-heap memory
			 * @param structure		Structure
			 */
			public void unmarshal(MemorySegment address, NativeStructure structure) {
				final Object value = foreign.get(address);
				final Object result = TransformerHelper.unmarshal(value, transformer);
				local.set(structure, result);
			}
		}

		private final Supplier<NativeStructure> factory;
		private final GroupLayout layout;
		private final List<FieldMapping> fields;

		/**
		 * Constructor.
		 * @param factory
		 * @param layout
		 * @param fields
		 */
		StructureTransformer(Supplier<NativeStructure> factory, GroupLayout layout, List<FieldMapping> fields) {
			this.factory = requireNonNull(factory);
			this.layout = requireNonNull(layout);
			this.fields = List.copyOf(fields);
		}

		@Override
		public MemoryLayout layout() {
			return layout;
		}

		@Override
		public MemorySegment marshal(NativeStructure structure, SegmentAllocator allocator) {
			final MemorySegment address = allocator.allocate(layout);
			for(FieldMapping f : fields) {
				f.marshal(structure, address, allocator);
			}
			return address;
		}

		@Override
		public Function<MemorySegment, NativeStructure> unmarshal() {
			return address -> {
				final NativeStructure structure = factory.get();
				unmarshal(address, structure);
				return structure;
			};
		}

		private void unmarshal(MemorySegment address, NativeStructure structure) {
			for(FieldMapping f : fields) {
				f.unmarshal(address, structure);
			}
		}
    }

	/**
	 * The <i>structure transformer factory</i> generates a transformer for a given native structure.
	 */
	class StructureTransformerFactory implements Registry.Factory<NativeStructure> {
		private final Lookup lookup = MethodHandles.lookup();
		private final Registry registry;

		/**
		 * Constructor.
		 * @param registry Transformer registry
		 */
		public StructureTransformerFactory(Registry registry) {
			this.registry = requireNonNull(registry);
		}

		/**
		 * Creates a transformer for the given type of structure.
		 * @param type Structure type
		 * @return Transformer
		 * @throws IllegalArgumentException if any of the structure members are anonymous or not present in the structure
		 * @throws IllegalArgumentException if the type of any field is unsupported
		 * @throws IllegalArgumentException if the structure does not provide a default constructor
		 * @throws RuntimeException if a structure field cannot be accessed
		 */
		@Override
		public StructureTransformer create(Class<? extends NativeStructure> type) {
			// Lookup structure constructor
			final Constructor<? extends NativeStructure> constructor = constructor(type);
			final Supplier<NativeStructure> factory = () -> instance(constructor);
			final NativeStructure instance = factory.get();

			// Build field mappings for this structure
			final GroupLayout layout = instance.layout();
			final Helper helper = new Helper(type, layout);
			final List<FieldMapping> fields = helper.build();

			// Create transformer
			return new StructureTransformer(factory, layout, fields);
		}

		/**
		 * Constructs the field mappings for a given structure.
		 */
		private class Helper {
			private final Class<? extends NativeStructure> type;
			private final GroupLayout layout;

			/**
			 * Constructor.
			 * @param type		Structure type
			 * @param layout	Memory layout
			 */
			public Helper(Class<? extends NativeStructure> type, GroupLayout layout) {
				this.type = type;
				this.layout = layout;
			}

			/**
			 * @return Field mappings for this structure
			 */
			private List<FieldMapping> build() {
				return layout
						.memberLayouts()
						.stream()
						.filter(e -> e instanceof ValueLayout)
						.map(this::mapping)
						.toList();
			}

			/**
			 * Builds the mapping for the given structure member.
			 * @param member Structure member
			 * @return Field mapping
			 */
			private FieldMapping mapping(MemoryLayout member) {
				// Lookup structure field
				final String name = member.name().orElseThrow(() -> new IllegalArgumentException("Anonymous structure member %s::%s".formatted(type, member)));
				final Field field = field(name);

				// Lookup handle to the structure field
				final VarHandle local = handle(field);

				// Lookup handle to the off-heap field
				final PathElement path = PathElement.groupElement(name);
				final VarHandle foreign = Transformer.removeOffset(layout.varHandle(path));

				// Lookup transformer for this field
				final Transformer transformer = registry
						.transformer(field.getType())
						.orElseThrow(() -> new IllegalArgumentException("Unsupported field type: " + field));

				// Create field mapping
				return new FieldMapping(local, foreign, transformer);
			}

			/**
			 * Looks up a structure field via reflection.
			 * @param name Field name
			 * @return Structure field
			 */
			private Field field(String name) {
				try {
					return type.getField(name);
				}
				catch(NoSuchFieldException e) {
					throw new IllegalArgumentException("Unknown structure field %s::%s".formatted(type, name), e);
				}
				catch(Exception e) {
					throw new IllegalArgumentException("Cannot access structure field %s::%s".formatted(type, name), e);
				}
			}

			/**
			 * Creates up a handle to the off-heap structure field
			 * @param field Structure field
			 * @return Foreign handle for the given field
			 */
			private VarHandle handle(Field field) {
				try {
					return lookup.unreflectVarHandle(field);
				}
				catch(IllegalAccessException e) {
					throw new RuntimeException("Cannot access field %s::%s".formatted(type, field), e);
				}
			}
		}

		/**
		 * Looks up the default constructor of the given type.
		 * @param type Type
		 * @return Constructor for the given type
		 */
		private static <T> Constructor<T> constructor(Class<T> type) {
			try {
				return type.getConstructor();
			}
			catch(Exception e) {
				throw new IllegalArgumentException("Cannot find default constructor: " + type, e);
			}
		}

		/**
		 * Creates a new structure instance.
		 * @param constructor Default constructor for the structure
		 * @return New instance
		 */
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
