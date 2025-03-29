package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * The <i>structure native transformer</i> marshals a structure to/from off-heap memory.
 * @author Sarge
 */
public class NativeStructureTransformer extends AbstractNativeTransformer<NativeStructure> {
	private static final Logger LOG = Logger.getLogger(NativeStructureTransformer.class.getName());

	/**
	 * Creates a factory for native structure transformers.
	 * @param registry Transformer registry
	 * @return Native structure transformer factory
	 */
	public static Factory<NativeStructure> factory(NativeRegistry registry) {
		return new Factory<>() {
			@Override
			public NativeTransformer<NativeStructure> create(Class<? extends NativeStructure> type) {
				LOG.info("Generating native structure mapping: " + type);
    			final Constructor<? extends NativeStructure> constructor = constructor(type);
    			final NativeStructure structure = NativeStructureTransformer.create(constructor);
    			final StructLayout layout = structure.layout();
    			final List<FieldMapping> mappings = FieldMapping.build(type, layout, registry);
    			return new NativeStructureTransformer(constructor, layout, mappings);
			}

			/**
			 * @return Default constructor for the given structure type
			 */
			private static Constructor<? extends NativeStructure> constructor(Class<? extends NativeStructure> type) {
		    	try {
		    		return type.getDeclaredConstructor();
		    	}
		    	catch(Exception e) {
		    		throw new RuntimeException("Cannot find default constructor for structure: " + type, e);
		    	}
			}
   		};
	}

	/**
	 * Creates a new structure instance.
	 * @param constructor Structure constructor
	 * @return New structure
	 * @throws RuntimeException if the structure cannot be created
	 */
	private static NativeStructure create(Constructor<? extends NativeStructure> constructor) {
		try {
			return constructor.newInstance();
		}
		catch(Exception e) {
			throw new RuntimeException("Error instantiating structure: " + constructor, e);
		}
	}

	private final Constructor<? extends NativeStructure> constructor;
	private final StructLayout layout;
	private final List<FieldMapping> mappings;

	/**
	 * Constructor.
	 * @param constructor		Constructor for new structure instances
	 * @param layout			Memory layout
	 * @param mappings			Field mappings
	 */
	NativeStructureTransformer(Constructor<? extends NativeStructure> constructor, StructLayout layout, List<FieldMapping> mappings) {
		this.constructor = requireNonNull(constructor);
		this.layout = requireNonNull(layout);
		this.mappings = requireNonNull(mappings);
	}

	/**
	 * A <i>field mapping</i> is used to marshal structure fields to/from off-heap memory.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static class FieldMapping {
		private final VarHandle local, foreign;
		private final NativeTransformer transformer;
		private Function unmarshal;

		/**
		 * Constructor.
		 * @param local				Structure field
		 * @param foreign			Off-heap field
		 * @param transformer		Transformer
		 */
		public FieldMapping(VarHandle local, VarHandle foreign, NativeTransformer<?> transformer) {
			this.local = requireNonNull(local);
			this.foreign = requireNonNull(foreign);
			this.transformer = requireNonNull(transformer);
		}

		/**
		 * Marshals this structure field to off-heap memory.
		 * @param structure		Structure instance
		 * @param address		Off-heap memory
		 * @param allocator		Allocator
		 */
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			final Object value = local.get(structure);
			final Object transformed = transformer.marshal(value, allocator);
			foreign.set(address, 0L, transformed);
		}

		/**
		 * Unmarshals this structure field from off-heap memory.
		 * @param address		Off-heap memory
		 * @param structure		Structure instance
		 */
		public void unmarshal(MemorySegment address, NativeStructure structure) {
			init();
			final Object value = foreign.get(address, 0L);
			final Object transformed = unmarshal.apply(value);
			local.set(structure, transformed);
		}

		/**
		 * Initialises the unmarshal function.
		 */
		private void init() {
			if(unmarshal == null) {
				unmarshal = transformer.unmarshal();
			}
		}

		/**
		 * Builds the field mappings for the given structure.
		 * @param type			Structure type
		 * @param layout		Memory layout
		 * @param registry		Transformer registry
		 * @return Structure fields
		 */
		public static List<FieldMapping> build(Class<? extends NativeStructure> type, StructLayout layout, NativeRegistry registry) {
			// Helper
			final var builder = new Object() {
				private final Lookup lookup = MethodHandles.lookup();

				/**
				 * Builds a field descriptor for the given member layout.
				 * @param layout Field layout
				 * @return Field descriptor
				 */
				public FieldMapping build(ValueLayout member) {
					// Lookup local structure field
					final String name = member.name().orElseThrow(() -> new IllegalArgumentException("Expected named structure field: " + member));
					final VarHandle local = localVarHandle(name);

					// Lookup off-heap structure field
					final PathElement path = PathElement.groupElement(name);
					final VarHandle foreign = layout.varHandle(path);

					// Lookup field transformer
					final NativeTransformer<?> transformer = registry.transformer(local.varType());

					// Create structure field
					return new FieldMapping(local, foreign, transformer);
				}

				/**
				 * Builds a handle to a local structure field with the given name.
				 * @param Field name
				 * @return Local field handle
				 * @throws RuntimeException if the field cannot be accessed
				 */
				private VarHandle localVarHandle(String name) {
					try {
						final Field field = type.getDeclaredField(name);
						return lookup.unreflectVarHandle(field);
					}
					catch(Exception e) {
						throw new RuntimeException("Cannot access structure field: " + name, e);
					}
				}
			};

			// Build a field descriptor for each member declared in the layout
			return layout
					.memberLayouts()
					.stream()
					.filter(e -> e instanceof ValueLayout)
					.map(ValueLayout.class::cast)
					.map(builder::build)
					.toList();
		}
	}

	@Override
	public MemorySegment marshal(NativeStructure structure, SegmentAllocator allocator) {
		final MemorySegment address = allocator.allocate(layout);
		for(FieldMapping field : mappings) {
			field.marshal(structure, address, allocator);
		}
		return address;
	}

	@Override
	public Function<MemorySegment, NativeStructure> unmarshal() {
		return address -> {
			final NativeStructure structure = create(constructor);
			final MemorySegment segment = address.reinterpret(layout.byteSize());
			for(FieldMapping field : mappings) {
				field.unmarshal(segment, structure);
			}
			return structure;
		};
	}
}
