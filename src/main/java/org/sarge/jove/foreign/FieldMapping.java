package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

/**
 * A <i>field mapping</i> marshals structure fields to/from off-heap memory.
 * @author Sarge
 */
class FieldMapping {
	private final VarHandle local, foreign;
	private final Transformer transformer;
	private Function<Object, Object> unmarshal;

	/**
	 * Constructor.
	 * @param local				Structure field
	 * @param foreign			Off-heap field
	 * @param transformer		Transformer
	 */
	private FieldMapping(VarHandle local, VarHandle foreign, Transformer transformer) {
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
		final Object transformed = TransformerHelper.marshal(value, transformer, allocator);
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
			unmarshal = TransformerHelper.unmarshal(transformer);
		}
	}

	/**
	 * Builds the field mappings for the given structure.
	 * @param type			Structure type
	 * @param layout		Memory layout
	 * @param registry		Transformer registry
	 * @return Structure fields
	 */
	public static List<FieldMapping> build(Class<? extends NativeStructure> type, StructLayout layout, Registry registry) {
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
				final Transformer transformer = registry.get(local.varType());

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
					final Field field = type.getField(name);
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
				.filter(e -> e instanceof ValueLayout)		// TODO - other layouts: sequence -> array, struct -> embedded
				.map(ValueLayout.class::cast)
				.map(builder::build)
				.toList();
	}
}
