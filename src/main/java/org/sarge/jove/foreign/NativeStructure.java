package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.*;
import java.util.function.*;

import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;

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
	 * Instantiates a new structure instance of the given type.
	 */
	static NativeStructure create(Class<? extends NativeStructure> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		}
		catch(Exception e) {
			throw new RuntimeException("Error creating structure return value: " + type, e);
		}
	}

	@Override
	public int hashCode() {
		// TODO - fields by reflection?
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO - compare fields by reflection?
		return super.equals(obj);
	}

	/**
	 * The <i>structure native mapper</i> marshals a {@link NativeStructure} to/from its native representation.
	 */
	public static class StructureNativeMapper extends AbstractNativeMapper<NativeStructure, MemorySegment> {
		/**
		 * Structure metadata.
		 */
		private record Entry(StructLayout layout, List<FieldMapping> mappings) {
		}

		private final Map<Class<? extends NativeStructure>, Entry> entries = new HashMap<>();
		private final NativeMapperRegistry registry;

	    /**
		 * Constructor.
		 * @param registry Native mappers
		 */
		public StructureNativeMapper(NativeMapperRegistry registry) {
			this.registry = requireNonNull(registry);
		}

		@Override
		public Class<NativeStructure> type() {
			return NativeStructure.class;
		}

		/**
		 * Retrieves the metadata for the given structure.
		 */
		private Entry entry(Class<? extends NativeStructure> type) {
			return entries.computeIfAbsent(type, this::register);
		}

		private Entry register(Class<? extends NativeStructure> type) {
			final NativeStructure instance = create(type);
			final StructLayout layout = instance.layout();
			final List<FieldMapping> mappings = FieldMapping.build(layout, type, registry);
			return new Entry(layout, mappings);
		}

		@Override
		public MemoryLayout layout(Class<? extends NativeStructure> type) {
			final Entry entry = entry(type);
			return entry.layout;
		}

		@Override
		public MemorySegment marshal(NativeStructure structure, NativeContext context) {
			// Allocate off-heap memory as required
			final Entry entry = entry(structure.getClass());
			if(structure.address == null) {
				structure.address = context.allocator().allocate(entry.layout);
			}

			if(structure instanceof VkPhysicalDeviceMemoryProperties) {
				System.out.println("FIDDLED");
				return structure.address;
			}

			// Marshal structure fields
			try {
    			for(FieldMapping m : entry.mappings) {
    				m.marshal(structure, structure.address, context);
    			}
			}
			catch(Exception e) {
				throw new RuntimeException("Error marshalling structure: " + structure, e);
			}

			return structure.address;
		}

		@Override
		public Function<MemorySegment, NativeStructure> returns(Class<? extends NativeStructure> target) {
			return address -> {
				final NativeStructure structure = create(target);
				unmarshal(address, structure);
				return structure;
			};
		}

		@Override
		public BiConsumer<MemorySegment, NativeStructure> unmarshal(Class<? extends NativeStructure> target) {
			return this::unmarshal;
		}

		private void unmarshal(MemorySegment address, NativeStructure structure) {
			// Resize address
			// TODO - always needs to be done?
			final Entry entry = entry(structure.getClass());
			final MemorySegment pointer = address.reinterpret(entry.layout.byteSize());

			// Unmarshal structure fields
			try {
    			for(FieldMapping m : entry.mappings) {
    				m.unmarshal(pointer, structure);
    			}
			}
			catch(Exception e) {
				throw new RuntimeException("Error unmarshalling structure: " + structure, e);
			}
		}
	}
}
