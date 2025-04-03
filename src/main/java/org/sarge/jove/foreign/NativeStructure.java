package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.List;
import java.util.function.*;
import java.util.logging.Logger;

import org.sarge.jove.foreign.NativeStructure.StructureTransformer.Builder.StructureFieldMapping;

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
	StructLayout layout();

	/**
	 * A <i>structure transformer</i> marshals a structure to/from off-heap memory.
	 */
	final class StructureTransformer implements ReferenceTransformer<NativeStructure, MemorySegment> {
		private final StructLayout layout;
		private final StructureFieldMapping mappings;

		/**
		 * Constructor.
		 * @param layout			Memory layout
		 * @param mappings			Field mappings
		 */
		private StructureTransformer(StructLayout layout, StructureFieldMapping mappings) {
			this.layout = requireNonNull(layout);
			this.mappings = requireNonNull(mappings);
		}

		@Override
		public MemoryLayout layout() {
			return layout;
		}

		@Override
		public MemorySegment marshal(NativeStructure structure, SegmentAllocator allocator) {
			final MemorySegment address = allocator.allocate(layout);
			marshal(structure, address, allocator);
			return address;
		}

		/**
		 * Marshals a structure to the given off-heap memory.
		 * @param structure		Structure
		 * @param address		Off-heap memory
		 * @param allocator		Off-heap allocator
		 */
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			mappings.marshal(structure, address, allocator);
		}

		@Override
		public NativeStructure unmarshal(MemorySegment address) {
			return (NativeStructure) mappings.unmarshal(address);
		}

		/**
		 * Unmarshals a structure from the given off-heap memory.
		 * @param address		Off-heap memory
		 * @param structure		Structure
		 */
		public void unmarshal(MemorySegment address, NativeStructure structure) {
			// TODO - where is this used? is it needed?
		}

    	/**
    	 * Builder for a structure transformer.
    	 */
    	public static class Builder {
    		private static final Logger LOG = Logger.getLogger(StructureTransformer.class.getName());

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
    		 * Builds a structure transformer for the given type.
    		 * @param type Structure type
    		 * @return Transformer
    		 * @throws IllegalArgumentException if the structure does not provide a default constructor
    		 * @throws IllegalArgumentException if a member of the structure layout is not named
    		 * @throws IllegalArgumentException for an unsupported structure field type
    		 * @throws RuntimeException if a structure field cannot be accessed
    		 */
    		public StructureTransformer build(Class<? extends NativeStructure> type) {
    			LOG.info("Generating native structure mapping: " + type);

    			// Retrieve the layout of this structure
    			final Supplier<NativeStructure> factory = factory(type);
    			final NativeStructure structure = factory.get();
    			final StructLayout layout = structure.layout();

    			// Builds field mappings and wrap as a top-level structure
    			final List<StructureField> fields = build(type, layout);
   				final var mappings = new StructureFieldMapping(factory, fields);

   				// Create transformer
   				return new StructureTransformer(layout, mappings);
    		}

    		/**
    		 * Builds the the fields mappings for the given structure.
    		 * @param type		Structure type
    		 * @param layout	Memory layout
    		 */
    		private List<StructureField> build(Class<?> type, StructLayout layout) {
    			// Helper
    			final var instance = new Object() {
    				/**
    				 * Creates a field mapping for the given member of this structure layout.
    				 * @param member Layout member
    				 * @return Field mapping
    				 */
        			private StructureField mapping(MemoryLayout member) {
    					// Lookup local structure field
    					final String name = member.name().orElseThrow(() -> new IllegalArgumentException("Expected named structure field: " + member));
    					final Field field = field(type, name);

    					// Create field mapping
    					final FieldMapping mapping = switch(member) {
    						case ValueLayout value -> atomic(field);
    						case StructLayout embedded -> embedded(field, embedded);
    						default -> throw new IllegalArgumentException("Unsupported structure member %s in field %s::%s".formatted(member, type, name));
    					};

    					// Create structure field wrapper
    					return new StructureField(handle(field), mapping);
            		}

            		/**
    				 * Creates an atomic field mapping.
    				 */
    				private FieldMapping atomic(Field field) {
        				final PathElement path = PathElement.groupElement(field.getName());
        				final VarHandle foreign = layout.varHandle(path);
        				final Transformer transformer = registry.get(field.getType());
    					return new AtomicFieldMapping(foreign, transformer);
    				}

    				/**
    				 * Recursively creates field mappings for an embedded structure.
    				 * @param field			Structure field
    				 * @param embedded		Embedded structure layout
    				 * @return Embedded structure field mappings
    				 */
    				@SuppressWarnings("unchecked")
					private FieldMapping embedded(Field field, StructLayout embedded) {
						final Class<?> child = field.getType();
						final List<StructureField> fields = build(child, embedded);
		    			final Supplier<?> factory = factory((Class<? extends NativeStructure>) child);
						return new StructureFieldMapping(factory, fields);
    				}

    	    		/**
    	    		 * Reflects a structure field.
    	    		 */
    	    		private static Field field(Class<?> type, String name) {
    	    			try {
    	    				return type.getField(name);
    	    			}
    	    			catch(Exception e) {
    	    				throw new RuntimeException("Cannot access structure field %s::%s".formatted(type, name), e);
    	    			}
    	    		}

    				/**
    				 * Create a handle to the given structure field.
    				 */
    				private VarHandle handle(Field field) {
    					try {
    						return lookup.unreflectVarHandle(field);
    					}
    					catch(Exception e) {
    						throw new RuntimeException("Cannot access structure field: " + field, e);
    					}
    				}
        		};

        		// Create field mappings for each member of the structure layout
   				return layout
   						.memberLayouts()
   						.stream()
   						.filter(Predicate.not(e -> e instanceof PaddingLayout))
   						.map(instance::mapping)
   						.toList();
    		}

    		/**
    		 * A <i>structure field</i> composes a structure field and the corresponding off-heap mapper.
    		 */
    		private record StructureField(VarHandle field, FieldMapping mapping) {
    			/**
    			 * Marshals a structure field.
    			 * @param structure		Structure instance
    			 * @param address		Off-heap memory
    			 * @param allocator		Allocator
    			 */
    			public void marshal(Object structure, MemorySegment address, SegmentAllocator allocator) {
    				final Object value = field.get(structure);
    				mapping.marshal(value, address, allocator);
    			}

    			/**
    			 * Unmarshals a structure field.
    			 * @param address		Off-heap memory
    			 * @param parent		Parent structure instance
    			 */
    			public void unmarshal(MemorySegment address, Object parent) {
    				final Object child = mapping.unmarshal(address);
    				field.set(parent, child);
    			}
    		}

    		/**
    		 * A <i>field mapping</i> marshals a structure field.
    		 */
    		private interface FieldMapping {
    			/**
    			 * Marshals a structure field.
    			 * @param value			Field value
    			 * @param address		Off-heap memory
    			 * @param allocator		Allocator
    			 */
    			void marshal(Object value, MemorySegment address, SegmentAllocator allocator);

    			/**
    			 * Unmarshals a structure field.
    			 * @param address Off-heap memory
    			 * @return Unmarshalled value
    			 */
    			Object unmarshal(MemorySegment address);
    		}

    		/**
    		 * An <i>atomic field mapping</i> marshals a primitive or reference structure field.
    		 */
    		private record AtomicFieldMapping(VarHandle foreign, Transformer transformer) implements FieldMapping {
    			@Override
    			public void marshal(Object value, MemorySegment address, SegmentAllocator allocator) {
    				final Object transformed = TransformerHelper.marshal(value, transformer, allocator);
    				foreign.set(address, 0L, transformed);
    			}

    			@Override
    			public Object unmarshal(MemorySegment address) {
    				final Object value = foreign.get(address, 0L);
    				return TransformerHelper.unmarshal(value, transformer);
    			}
    		}

    		/**
    		 * A <i>structure field mapping</i> marshals a top-level structure or an embedded structure field.
    		 */
    		protected record StructureFieldMapping(Supplier<?> constructor, List<StructureField> fields) implements FieldMapping {
    			@Override
    			public void marshal(Object structure, MemorySegment address, SegmentAllocator allocator) {
    				for(var field : fields) {
    	    			field.marshal(structure, address, allocator);
    	    		}
    			}

    			@Override
    			public Object unmarshal(MemorySegment address) {
    				// TODO - could only create if NULL or overwrite?
    				final var structure = constructor.get();
    				for(var field : fields) {
    	    			field.unmarshal(address, structure);
    	    		}
    				return structure;
    			}
    		}

    		/**
    		 * Creates a factory for new structure instances.
    		 * @param constructor Structure constructor
    		 * @return Structure factory
    		 */
    		private static Supplier<NativeStructure> factory(Class<? extends NativeStructure> type) {
	    		// Lookup default constructor
    			final Constructor<? extends NativeStructure> constructor;
    	    	try {
    	    		constructor = type.getConstructor();
    	    	}
    	    	catch(Exception e) {
    	    		throw new RuntimeException("Cannot find default constructor for structure: " + type, e);
    	    	}

    	    	// Create factory
    			return () -> {
        			try {
        				return constructor.newInstance();
        			}
        			catch(Exception e) {
        				throw new RuntimeException("Error instantiating structure: " + constructor, e);
        			}
        		};
    		}
        }
    }
}
