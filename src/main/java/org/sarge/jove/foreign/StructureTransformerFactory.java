package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.*;

/**
 * The <i>structure transformer factory</i> generates the transformer for a native structure.
 * @author Sarge
 */
public class StructureTransformerFactory implements Registry.Factory<NativeStructure> {
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
	 * @throws RuntimeException if a structure cannot be instantiated or a field cannot be accessed
	 */
	@Override
	public StructureTransformer transformer(Class<? extends NativeStructure> type) {
		// Create structure factory
		final Supplier<NativeStructure> factory = () -> instance(type);

		// Create a transient instance to retrieve the structure layout
		final NativeStructure instance = factory.get();
		final GroupLayout layout = instance.layout();

		// TODO...
		if(layout == null) {
			throw new RuntimeException("TODO old code generated structure: " + type);
		}
		// ...TODO

		// Build field mappings for this structure
		final var builder = new FieldMappingBuilder(type, layout);
		final List<FieldMapping> mappings = builder.build();

		// Create transformer
		return new StructureTransformer(factory, layout, mappings);
	}

	/**
	 * Creates a factory for new structure instances.
	 * @throws IllegalArgumentException if the structure does not declare a default constructor
	 * @throws RuntimeException if the structure cannot be instantiated
	 */
	private static <T> T instance(Class<? extends T> type) {
		try {
			return type.getConstructor().newInstance();
		}
		catch(NoSuchMethodException e) {
			throw new IllegalArgumentException("Cannot find default constructor: " + type, e);
		}
		catch(Exception e) {
			throw new RuntimeException("Cannot instantiate structure: " + type, e);
		}
	}

	/**
	 * Constructs the field mappings for a given structure.
	 */
	private class FieldMappingBuilder {
		private final Lookup lookup = MethodHandles.lookup();
		private final Class<? extends NativeStructure> structure;
		private final GroupLayout layout;

		/**
		 * Constructor.
		 * @param structure		Structure type
		 * @param layout		Memory layout
		 */
		public FieldMappingBuilder(Class<? extends NativeStructure> structure, GroupLayout layout) {
			this.structure = structure;
			this.layout = layout;
		}

		/**
		 * @return Field mappings for this structure
		 */
		private List<FieldMapping> build() {
			try {
				return buildLocal();
			}
			catch(IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("%s in %s", e.getMessage(), structure), e);
			}
		}

		private List<FieldMapping> buildLocal() {
			return layout
					.memberLayouts()
					.stream()
					.filter(Predicate.not(member -> member instanceof PaddingLayout))
					.map(this::mapping)
					.toList();
		}

		/**
		 * Builds the mapping for the given structure member.
		 * @param member Structure member
		 * @return Field mapping
		 */
		private FieldMapping mapping(MemoryLayout member) {
			// Lookup member name
			final String name = member
					.name()
					.orElseThrow(() -> new IllegalArgumentException("Anonymous structure member: " + member));

			// Get handle to structure field
			final VarHandle local = local(name);
			final Class<?> type = local.varType();

			// Lookup field transformer
			final var transformer = registry
					.transformer(type)
					.orElseThrow(() -> new IllegalArgumentException("Unsupported field type %s in %s".formatted(type, member)));

			// Init path to this member
			final PathElement path = PathElement.groupElement(name);

			// Build field marshalling adapter depending on layout
			final FieldMarshal marshal = switch(member) {
				case ValueLayout _			-> value(path);
				case SequenceLayout seq		-> sequence(path, type, seq);
				case GroupLayout nested		-> nested(path, nested);
				default -> throw new IllegalArgumentException("Unsupported structure field layout: " + member);
			};

			// Create mapping
			return new FieldMapping(local, transformer, marshal);
		}

		/**
		 * Retrieves a handle for a structure field.
		 * @param name Field name
		 * @return Field handle
		 * @throws IllegalArgumentException if the field is not declared in the structure
		 * @throws RuntimeException if the field cannot be accessed
		 */
		private VarHandle local(String name) {
			try {
				final Field field = structure.getField(name);
				return lookup.unreflectVarHandle(field);
			}
			catch(NoSuchFieldException e) {
				throw new IllegalArgumentException("Unknown structure field: " + name);
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Creates an atomic field marshaller for primitives and simple reference types.
		 * @param path Field path
		 * @return Atomic marshaller
		 */
		private FieldMarshal value(PathElement path) {
			final VarHandle foreign = layout.varHandle(path);
			return new AtomicFieldMarshal(foreign);
		}

		/**
		 * Creates a field marshaller for an array.
		 * <p>
		 * Note that if the structure field is <b>not</b> an array type this implementation delegates to a simple {@link SliceFieldMarshal}.
		 * This allows compound types such as {@link String} to be transparently marshalled to/from an off-heap array.
		 * <p>
		 * @param path			Field path
		 * @param type			Field type
		 * @param sequence		Sequence layout
		 * @return Array field marshaller
		 */
		private FieldMarshal sequence(PathElement path, Class<?> type, SequenceLayout sequence) {
			final long offset = layout.byteOffset(path);
			final long size = sequence.byteSize();

			if(type.isArray()) {
				// Create array field marshaller
				final Class<?> component = type.componentType();
				final int count = (int) sequence.elementCount();
				return new ArrayFieldMarshal(offset, size, component, count);
			}
			else {
				// Otherwise transparently marshal to a compound type, i.e. string
				return new SliceFieldMarshal(offset, size);
			}
		}

		/**
		 * Creates a nested structure field marshaller.
		 * @param path			Field path
		 * @param nested		Nested structure layout
		 * @return Nested field marshaller
		 */
		private FieldMarshal nested(PathElement path, GroupLayout nested) {
			final long offset = layout.byteOffset(path);
			final long size = nested.byteSize();
			return new SliceFieldMarshal(offset, size) {
				@SuppressWarnings("rawtypes")
				@Override
				public void marshal(Object value, Transformer transformer, MemorySegment address, SegmentAllocator allocator) {
					if(value == null) {
						return;
					}

					// TODO...
					final var structure = (StructureTransformer) transformer;
					structure.marshal((NativeStructure) value, slice(address), allocator);
					// ...TODO
				}
			};
		}
	}
}
