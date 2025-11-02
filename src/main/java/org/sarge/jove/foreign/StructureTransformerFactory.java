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
 * The <i>structure transformer factory</i> generates the transformer for a native structure.
 * @author Sarge
 */
public class StructureTransformerFactory implements Registry.Factory<NativeStructure> {
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
	public StructureTransformer transformer(Class<? extends NativeStructure> type) {
		// Create structure factory
		final Supplier<NativeStructure> factory = () -> instance(type);

		// Create a transient instance to retrieve the structure layout
		final NativeStructure instance = factory.get();
		final GroupLayout layout = instance.layout();

		// Build field mappings for this structure
		final var builder = new FieldMappingBuilder(type, layout);
		final List<FieldMapping> mappings = builder.build(layout);

		// Create transformer
		return new StructureTransformer(factory, layout, mappings);
	}

	/**
	 * Creates a factory for new structure instances.
	 */
	private static <T> T instance(Class<? extends T> type) {
		try {
			return type.getConstructor().newInstance();
		}
		catch(NoSuchMethodException e) {
			throw new IllegalArgumentException("Cannot find default constructor: " + type, e);
		}
		catch(Exception e) {
			throw new IllegalArgumentException("Cannot instantiate structure: " + type, e);
		}
	}

	/**
	 * Constructs the field mappings for a given structure.
	 */
	private class FieldMappingBuilder {
		private final Class<? extends NativeStructure> type;
		private final GroupLayout layout;

		/**
		 * Constructor.
		 * @param type			Structure type
		 * @param layout		Memory layout
		 */
		public FieldMappingBuilder(Class<? extends NativeStructure> type, GroupLayout layout) {
			this.type = type;
			this.layout = layout;
		}

		/**
		 * @return Field mappings for this structure
		 */
		private List<FieldMapping> build(GroupLayout group) {
			return group
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
					.orElseThrow(() -> new IllegalArgumentException("Anonymous structure member %s::%s".formatted(type, member)));

			// Get handle to structure field
			final VarHandle local = local(name);

			// Create field mapping depending on layout
			return switch(member) {
				case ValueLayout _			-> simple(name, local);
				case SequenceLayout seq		-> array(name, local, seq);
				case GroupLayout nested		-> nested(name, local, nested);
				default -> throw new IllegalArgumentException("Unsupported field %s::%s".formatted(type, member));
			};
		}

		/**
		 * Looks up a structure field via reflection.
		 * @param name Field name
		 * @return Structure field
		 */
		private VarHandle local(String name) {
			try {
				final Field field = type.getField(name);
				return lookup.unreflectVarHandle(field);
			}
			catch(NoSuchFieldException e) {
				throw new IllegalArgumentException("Unknown structure field %s::%s".formatted(type, name), e);
			}
			catch(Exception e) {
				throw new IllegalArgumentException("Cannot access structure field %s::%s".formatted(type, name), e);
			}
		}

		/**
		 * Creates a simple field mapping.
		 * @param name
		 * @param local
		 * @param parent
		 * @return
		 */
		private FieldMapping simple(String name, VarHandle local) {
			// Lookup field transformer
			final var transformer = registry
					.transformer(local.varType())
					.orElseThrow(() -> new IllegalArgumentException("Unsupported field %s::%s".formatted(type, name)));

			// Get handle to off-heap field
			final PathElement path = PathElement.groupElement(name);
			final VarHandle foreign = Transformer.removeOffset(layout.varHandle(path));

			// Create field mapping
			return new SimpleFieldMapping(local, foreign, transformer);
		}

		/**
		 *
		 * @param name
		 * @param local
		 * @param sequence
		 * @return
		 */
		private FieldMapping array(String name, VarHandle local, SequenceLayout sequence) {

			final long count = sequence.elementCount();

			// Lookup field transformer
			final var transformer = registry
					.transformer(local.varType())
					.orElseThrow(() -> new IllegalArgumentException("Unsupported sequence type %s::%s".formatted(type, name)));

			return new FieldMapping() {
				@Override
				public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {

					final Object value = local.get(structure);

					if(value == null) {
						return;
					}

					System.err.println("MARSHAL SEQUENCE ELEMENT " + sequence);

				}

				@Override
				public void unmarshal(MemorySegment address, NativeStructure structure) {
System.out.println("unmarshal local="+local+" transformer="+transformer);

					final MemorySegment slice = address.asSlice(0L, count * sequence.elementLayout().byteSize());
					// TODO - is this ALWAYS sequence.byteSize()?

					if(local.varType().isArray()) {
						final Object[] array = (Object[]) Array.newInstance(local.varType().getComponentType(), (int) sequence.elementCount());
						final ArrayTransformer t = (ArrayTransformer) transformer;
						t.update().accept(slice, array);		// TODO - promote update() to Transformer? and/or make it used for arrays in general?
						local.set(structure, array);
					}
					else {
						final Object result = transformer.unmarshal().apply(slice);
						local.set(structure, result);
					}
					// TODO - primitive arrays that are not mapped to String
				}
			};
		}

		/**
		 * Creates a nested structure field mapping.
		 * @param local
		 * @param nested
		 * @return Nested field mapping
		 */
		private FieldMapping nested(String name, VarHandle local, GroupLayout nested) {

			//final List<FieldMapping> mappings = build(nested);

			/**
			 *
			 * - local = child structure field
			 *
			 * marshal
			 * - skip if null (?)
			 * - create field mapping for each field in child:
			 * - local = path element to child field
			 *
			 * unmarshal
			 * - if null, create from factory
			 * - for each mapping:
			 * - write transformer result to child instance
			 *
			 */

			return new FieldMapping() {
				@Override
				public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
				}

				@Override
				public void unmarshal(MemorySegment address, NativeStructure structure) {
				}
			};
		}
	}
}
