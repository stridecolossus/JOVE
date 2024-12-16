package org.sarge.jove.foreign;

import static java.util.stream.Collectors.*;
import static org.sarge.jove.foreign.FieldMapping.*;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

/**
 * A <i>field mapping</i> marshals structure fields to/from off-heap memory.
 * @author Sarge
 */
@SuppressWarnings("unused")
interface FieldMapping {
	/**
	 * Transforms a structure field to off-heap memory.
	 * @param structure		Structure
	 * @param address		Off-heap memory
	 * @param allocator		Allocator
	 */
	void transform(NativeStructure structure, MemorySegment address, SegmentAllocator allocator);

	/**
	 * Populates a structure field from off-heap memory.
	 * @param address		Off-heap memory
	 * @param structure		Structure
	 */
	void populate(MemorySegment address, NativeStructure structure);

	/**
	 * A <i>structure field mapping</i> marshals the fields of a structure to/from off-heap memory.
	 */
	record StructureFieldMapping(List<FieldMapping> mappings) implements FieldMapping {
		@Override
		public void transform(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			for(FieldMapping m : mappings) {
				m.transform(structure, address, allocator);
			}
		}

		@Override
		public void populate(MemorySegment address, NativeStructure structure) {
			for(FieldMapping m : mappings) {
				m.populate(address, structure);
			}
		}

		/**
		 * Builds the field mapping for a native structure.
		 * @param type			Structure type
		 * @param layout		Structure layout
		 * @param registry		Native transformers
		 * @return Field mappings
		 */
		public static StructureFieldMapping build(Class<?> type, StructLayout layout, TransformerRegistry registry) {
			/**
			 * Builder for the field mapping of a structure member.
			 */
			final var builder = new Object() {
//				private final Logger log = Logger.getLogger(StructureFieldMapping.class.getName());

				/**
				 * Builds the field mapping for the given member.
				 */
				public FieldMapping mapping(MemoryLayout member) {
					final String name = member.name().get();
					final Field field = field(name);

//					if(member instanceof AddressLayout address) {
//						if(address.targetLayout().isPresent()) {
//							System.out.println("member="+field.getName()+" target="+address.targetLayout());
//						}
//					}

					return switch(member) {
						case ValueLayout __ -> atomic(field); // TODO - we never check that transformer.layout() == member layout, should we? use instead of deriving from transformer???
						case StructLayout struct -> nested(field, struct);
						case SequenceLayout seq -> array(field, seq);
						default -> throw new IllegalArgumentException(format("Invalid member layout", type, name));
					};
				}

				/**
				 * Reflects a structure field.
				 */
				private Field field(String name) {
					try {
						return type.getDeclaredField(name);
					}
					catch(NoSuchFieldException e) {
						throw new IllegalArgumentException(format("Unknown structure field", type, name), e);
					}
					catch(Exception e) {
						throw new RuntimeException(format("Error reflecting structure field", type, name), e);
					}
				}

				private AtomicFieldMapping atomic(Field field) {
					final var path = PathElement.groupElement(field.getName());
					final VarHandle handle = layout.varHandle(path);
					final var transformer = registry.get(field.getType());
					return new AtomicFieldMapping(field, transformer, handle);
				}

				private NestedStructureFieldMapping nested(Field field, StructLayout nested) {
					final var mapping = StructureFieldMapping.build(field.getType(), nested, registry);
					return new NestedStructureFieldMapping(field, mapping);
				}

				private FieldMapping array(Field field, SequenceLayout sequence) {
					return switch(sequence.elementLayout()) {
    					case ValueLayout __ -> throw new UnsupportedOperationException(); // TODO - primitive arrays?
    					case StructLayout struct -> {
    						final Class<?> component = field.getType().getComponentType();
							final var transformer = registry.get(component);
    						final var mapping = StructureFieldMapping.build(component, struct, registry);
    						yield new ArrayFieldMapping(field, sequence, transformer, mapping);
    					}
    					default -> throw new UnsupportedOperationException();
					};

//					final var parent = PathElement.groupElement(layout.name().get());
//					final var path = PathElement.groupElement(field.getName());
//					final VarHandle handle = layout.arrayElementVarHandle(parent, PathElement.sequenceElement(), path);
//					return new ArrayFieldMapping(field, length, transformer, handle);
				}
			};

			// Enumerate field mappings for the given structure
			return layout
        			.memberLayouts()
        			.stream()
        			.filter(e -> e.name().isPresent())
        			.map(builder::mapping)
//        			.peek(m -> builder.log.info(m.toString()))
        			.collect(collectingAndThen(toList(), StructureFieldMapping::new));
		}
	}

	// TODO - factor out...

	/**
	 * An <i>atomic field mapping</i> marshals a primitive or simple reference structure field.
	 */
	@SuppressWarnings("rawtypes")
	record AtomicFieldMapping(Field field, NativeTransformer transformer, VarHandle handle) implements FieldMapping {
		@Override
		public void transform(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {

//			if(field.getType().isArray()) {
//				System.out.println("field array " + field);
//				System.out.println();
//			}

			final Object value = get(field, structure);
			final Object foreign = transformer.transform(value, ParameterMode.VALUE, allocator);
			handle.set(address, 0L, foreign);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void populate(MemorySegment address, NativeStructure structure) {
			final Object foreign = handle.get(address, 0L);
			final Object value = transformer.returns(/*field.getType()*/).apply(foreign);
			set(structure, value);
		}

		private void set(NativeStructure structure, Object value) {
	    	try {
	    		field.set(structure, value);
	    	}
	    	catch(Exception e) {
	    		throw new RuntimeException(format("Error populating structure field", structure.getClass(), field.getName()), e);
	    	}
		}
	}

	/**
	 * An <i>embedded structure field mapping</i> marshals an embedded structure.
	 */
	record NestedStructureFieldMapping(Field field, FieldMapping delegate) implements FieldMapping {
		@Override
		public void transform(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			final var embedded = (NativeStructure) get(field, structure);
			delegate.transform(embedded, address, allocator);
		}

		@Override
		public void populate(MemorySegment address, NativeStructure structure) {
			final var embedded = (NativeStructure) get(field, structure);
			delegate.populate(address, embedded);
		}
	}

	/**
	 * An <i>array</i> field mapping marshals an array structure field.
	 */
	record ArrayFieldMapping(Field field, SequenceLayout sequence, NativeTransformer transformer, StructureFieldMapping mapping) implements FieldMapping {
		@Override
		public void transform(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			// TODO
			//System.out.println("transform array " + field);
		}

		@Override
		public void populate(MemorySegment address, NativeStructure structure) {
			final Class<?> component = field.getType().getComponentType();
			final Function mapper = transformer.returns(); // component);

			final long length = sequence.elementCount();
			final long size = sequence.elementLayout().byteSize();

			final Object[] array = (Object[]) get(field, structure);
			for(int n = 0; n < length; ++n) {
				final MemorySegment element = address.asSlice(n * size, size);
				array[n] = mapper.apply(element);
			}
		}
	}

	/**
	 * Helper - Retrieves the given field value from a structure instance.
	 */
	private static Object get(Field field, NativeStructure structure) {
		try {
			return field.get(structure);
		}
		catch(Exception e) {
			throw new RuntimeException(format("Error reflecting structure field", structure.getClass(), field.getName()), e);
		}
	}

	/**
	 * Helper - Formats a field mapping exception.
	 * @param message		Message
	 * @param type			Field type
	 * @param field			Field name
	 * @return Exception message
	 */
	private static String format(String message, Class<?> type, String field) {
    	return String.format("%s in [%s.%s]", message, type.getSimpleName(), field);
    }
}

//					case SequenceLayout sequence -> {
//						final var path = PathElement.groupElement(name);
//						yield new FieldMapping(field) {
//							private final Class<?> type = field.getType().getComponentType();
//							private final NativeTransformer transformer = registry.get(type);
//
//							@Override
//							protected void transform(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
//								final Object[] array = (Object[]) get(structure);
//
//								final long offset = layout.byteOffset(path);
//								final long size = transformer.layout(type).byteSize();
//
//								final MemorySegment segment = address.asSlice(offset);
//
//								for(int n = 0; n < array.length; ++n) {
//									final var foreign = (MemorySegment) NativeTransformer.transform(array[n], type, transformer, allocator);
//									// TODO - check null
//									final MemorySegment element = segment.asSlice(n * size, size);
//									element.copyFrom(foreign);
//								}
//							}
//
//							@Override
//							protected void transform(MemorySegment address, NativeStructure structure) {
//								final Object[] array = (Object[]) get(structure);
//
//								final long offset = layout.byteOffset(path);
//								final long size = transformer.layout(type).byteSize();
//
//								final MemorySegment segment = address.asSlice(offset);
//
//								for(int n = 0; n < array.length; ++n) {
//									final MemorySegment element = segment.asSlice(n * size, size);
//									array[n] = transformer.returns(type).apply(element);
//								}
//							}
//						};
