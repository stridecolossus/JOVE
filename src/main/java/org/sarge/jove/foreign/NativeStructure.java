package org.sarge.jove.foreign;

import java.lang.foreign.*;

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
}

//
//	/**
//	 * Transformer for a native structure.
//	 * <p>
//	 * Implementation considerations for the {@link #layout(Class)} of a native structure:
//	 * <ul>
//	 * <li>The convenience {@link NativeStructure#POINTER} can be used for pointer fields such as strings, structures, etc</li>
//	 * <li>The layout must adhere to the byte alignment requirements of an FFM structure (8 bytes by default), see {@link NativeStructure#PADDING}</li>
//	 * <li>A <i>nested</i> structure is declared using an <i>embedded</i> structure layout</li>
//	 * </ul>
//	 * <p>
//	 * Example:
//	 * <pre>
//	 * TODO
//	 * </pre>
//	 * <p>
//	 * Note that structures (and other data transfer objects) used in a native method are assumed to be transient, carrier types with a scope that is relevant only during method invocation.
//	 * Multiple calls to {@link #transform(NativeStructure, ParameterMode, SegmentAllocator)} or other marshalling methods with the same instance will repeat the entire process.
//	 * i.e. A native structure has no <i>state</i> or off-heap memory.
//	 * <p>
//	 */
//	public static class StructureNativeTransformer implements UpdateTransformer<NativeStructure> {
//		private static final Logger LOG = Logger.getLogger(StructureNativeTransformer.class.getName());
//
//		/**
//		 *
//		 */
//		private record Entry(StructLayout layout, StructureFieldMapping mapping) {
//		}
//
//		private final Map<Class<? extends NativeStructure>, Entry> entries = new HashMap<>();
//		private final TransformerRegistry registry;
//
//		/**
//		 * Constructor.
//		 * @param registry Native transformers
//		 */
//		public StructureNativeTransformer(TransformerRegistry registry) {
//			this.registry = requireNonNull(registry);
//		}
//
//		/**
//		 *
//		 * @param type
//		 * @return
//		 */
//		private Entry entry(Class<? extends NativeStructure> type) {
//			return entries.computeIfAbsent(type, this::register);
//		}
//
//		/**
//		 *
//		 * @param type
//		 * @return
//		 */
//		private Entry register(Class<? extends NativeStructure> type) {
//			LOG.info("Registering " + type.getName());
//			final NativeStructure structure = create(type);
//			final StructLayout layout = structure.layout();
//			final var mapping = StructureFieldMapping.build(structure.getClass(), layout, registry);
//			return new Entry(layout, mapping);
//		}
//
//		/**
//		 * Instantiates a new structure instance of the given type.
//		 */
//		private static NativeStructure create(Class<? extends NativeStructure> type) {
//			try {
//				return type.getDeclaredConstructor().newInstance();
//			}
//			catch(Exception e) {
//				throw new RuntimeException("Error instantiating structure: " + type, e);
//			}
//		}
//
//		@Override
//		public MemorySegment transform(NativeStructure structure, SegmentAllocator allocator) {
//			if(structure == null) {
//				return MemorySegment.NULL;
//			}
//			else {
//				final Entry entry = entry(structure.getClass());
//				final MemorySegment address = allocator.allocate(entry.layout);
//   				entry.mapping.transform(structure, address, allocator);
//    			return address;
//			}
//		}
//
//		@Override
//		public Function<MemorySegment, NativeStructure> returns(Class<? extends NativeStructure> type) {
//			return address -> {
//				final Entry entry = entry(type);
//				final NativeStructure structure = create(type);
//				entry.mapping.populate(address, structure);
//				return structure;
//			};
//		}
//
//		@Override
//		public void update(MemorySegment address, NativeStructure structure) {
//			final Entry entry = entry(structure.getClass());
//			final var segment = address.reinterpret(entry.layout.byteSize());
//			entry.mapping.populate(segment, structure);
//		}
//	}
//}
