package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A <i>native structure</i> represents a native structure or union.
 * TODO
 * - public fields
 * - mapping to layout
 * - modify issues, i.e. ignored after allocate => (re)marshals every time
 * @author Sarge
 */
public abstract class NativeStructure {
	/**
	 * Memory layout for a pointer field of a structure.
	 */
    protected static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_BYTE));

    /**
     * Default padding to ensure structure fields are correctly aligned.
     */
    protected static final MemoryLayout PADDING = MemoryLayout.paddingLayout(4);

    private MemorySegment address;

    /**
	 * @return Memory layout of this structure
	 */
	protected abstract StructLayout layout();

	/**
	 * Allocates the off-heap memory for this structure as required.
	 * @param layout		Structure layout
	 * @param allocator		Allocator
	 */
	private void allocate(StructLayout layout, SegmentAllocator allocator) {
		if(address == null) {
			address = allocator.allocate(layout);
		}
	}

	/**
	 * @return Public structure fields
	 */
	private Stream<Field> fields() {
		final Field[] fields = this.getClass().getDeclaredFields();
		return Arrays
				.stream(fields)
				.filter(NativeStructure::isStructureField);
	}

	/**
	 * Helper - Retrieves a structure field value.
	 * @param field Structure field
	 * @return Field value
	 */
	Object get(Field field) {
		try {
			return field.get(this);
		}
		catch(Exception e) {
			throw new RuntimeException("Cannot retrieve structure field: " + field, e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(address);
//		return this
//				.fields()
//				.map(this::get)
//				.mapToInt(Object::hashCode)
//				.sum();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeStructure that) &&
				this.getClass().isAssignableFrom(that.getClass()) &&
				this.fields().allMatch(field -> equals(field, that));
	}

	private boolean equals(Field field, NativeStructure that) {
		return Objects.equals(this.get(field), that.get(field));
	}

	/**
	 * @return Whether the given field is a valid structure member
	 */
	static boolean isStructureField(Field field) {
		final int modifiers = field.getModifiers();
		return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
	}

	/**
	 * The <i>structure native mapper</i> marshals a {@link NativeStructure} to/from its native representation.
	 */
	public static class StructureNativeTransformer extends AbstractNativeTransformer<NativeStructure, MemorySegment> {
		@Override
		public StructureNativeTransformer derive(Class<? extends NativeStructure> target, TransformerRegistry registry) {
			final NativeStructure instance = create(target);
			final StructLayout layout = instance.layout();
			final StructureFieldMapping mappings = StructureFieldMapping.build(layout, 0, target, registry);
			return new Instance(target, layout, mappings);
		}

		@Override
		public Class<NativeStructure> type() {
			return NativeStructure.class;
		}

		@Override
		public MemorySegment transform(NativeStructure instance, SegmentAllocator allocator) {
			throw new RuntimeException();
		}

		/**
		 * Instantiates a new structure instance of the given type.
		 */
		protected static NativeStructure create(Class<? extends NativeStructure> type) {
			try {
				return type.getDeclaredConstructor().newInstance();
			}
			catch(Exception e) {
				throw new RuntimeException("Error creating structure return value: " + type, e);
			}
		}
	}

	/**
	 * Structure subclass mapper.
	 */
	private static class Instance extends StructureNativeTransformer {
		private final Class<? extends NativeStructure> type;
		private final StructLayout layout;
		private final StructureFieldMapping mappings;

		/**
		 * Constructor.
		 * @param type				Target type
		 * @param layout			Structure layout
		 * @param mappings			Field mappings
		 */
		public Instance(Class<? extends NativeStructure> type, StructLayout layout, StructureFieldMapping mappings) {
			this.type = requireNonNull(type);
			this.layout = requireNonNull(layout);
			this.mappings = requireNonNull(mappings);
		}

		@Override
		public MemoryLayout layout() {
			return layout;
		}

		@Override
		public MemorySegment transform(NativeStructure structure, SegmentAllocator allocator) {
			structure.allocate(layout, allocator);
			mappings.marshal(structure, structure.address, allocator);
			return structure.address;
		}

		@Override
		public Function<MemorySegment, NativeStructure> returns() {
			return address -> {
				final NativeStructure structure = create(type);
				unmarshal(address, structure);
				return structure;
			};
		}

		@Override
		public BiConsumer<MemorySegment, NativeStructure> update() {
			return this::unmarshal;
		}

		/**
		 * Unmarshals off-heap memory to the given structure.
		 */
		private void unmarshal(MemorySegment address, NativeStructure structure) {
			// TODO - always needs to be done?
			final MemorySegment segment = address.reinterpret(layout.byteSize());
			mappings.unmarshal(segment, structure);
		}
	}
}
