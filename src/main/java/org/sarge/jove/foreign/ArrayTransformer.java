package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

/**
 * An <i>array transformer</i> marshals an array to/from off-heap memory.
 * @author Sarge
 */
public class ArrayTransformer extends DefaultTransformer<Object[]> {
	private final Transformer transformer;
	private final VarHandle handle;

	/**
	 * Constructor.
	 * @param transformer Component transformer
	 */
	public ArrayTransformer(Transformer transformer) {
		this.transformer = requireNonNull(transformer);
		this.handle = Transformer.removeOffset(handle(transformer.layout()));
	}

	private static VarHandle handle(MemoryLayout layout) {
		if(layout instanceof StructLayout) {
			return ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, layout)).arrayElementVarHandle();
		}
		else {
			return layout.arrayElementVarHandle();
		}
	}

	@Override
	public MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	@Override
	public MemorySegment marshal(Object[] array, SegmentAllocator allocator) {
		// Allocate off-heap array
		final MemoryLayout layout = transformer.layout();
		final MemorySegment address = allocator.allocate(layout, array.length);

		// Transform elements and populate off-heap memory
    	for(int n = 0; n < array.length; ++n) {
    		final Object element = TransformerHelper.marshal(array[n], transformer, allocator);
    		handle.set(address, (long) n, element);
    	}
//		if(array instanceof NativeStructure[]) {
//			marshalStructureArray(array, address, allocator);
//		}
//		else {
//			marshalReferenceArray(array, address, allocator);
//		}

		return address;
	}

//	// TODO - should array element transformation be a function of the Transformer?
//	// i.e. returns a int consumer function that marshals each element?
//
//	private void marshalStructureArray(T[] array, MemorySegment address, SegmentAllocator allocator) {
//
//		final AddressLayout structureArray = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(array.length, transformer.layout()));
//		System.out.println("layout="+structureArray+" size="+structureArray.byteSize()+" length="+array.length);
//
//		final MemoryLayout layout = transformer.layout();
//    	final var handle = Transformer.removeOffset(structureArray.arrayElementVarHandle());
//    	for(int n = 0; n < array.length; ++n) {
//    		final Object element = Transformer.marshal(array[n], transformer, allocator);
//    		handle.set(address, (long) n, element);
//    	}
//
//
////		final MemoryLayout layout = transformer.layout();
////		final long size = layout.byteSize();
////		for(int n = 0; n < array.length; ++n) {
////			final Object element = Transformer.marshal(array[n], transformer, allocator);
////    		final MemorySegment slice = address.asSlice(n * size, size);
////    		slice.copyFrom((MemorySegment) element);
////		}
//	}
//
//	private void marshalReferenceArray(T[] array, MemorySegment address, SegmentAllocator allocator) {
//		final MemoryLayout layout = transformer.layout();
//    	final var handle = Transformer.removeOffset(layout.arrayElementVarHandle());
//    	for(int n = 0; n < array.length; ++n) {
//    		final Object element = Transformer.marshal(array[n], transformer, allocator);
//    		handle.set(address, (long) n, element);
//    	}
//	}

	@Override
	public Function<MemorySegment, Object[]> unmarshal() {
		throw new UnsupportedOperationException();
	}

//	@Override
//	public BiConsumer<MemorySegment, T[]> update() {
//		return (address, array) -> {
//System.out.println("array="+array.getClass());
//			final Function unmarshal = transformer.unmarshal();
//			for(int n = 0; n < array.length; ++n) {
//System.out.println("n="+n);
//	    		final Object element = handle.get(address, (long) n);
//	    		array[n] = (T) unmarshal.apply(element);
//			}
//		};
//	}
}

//
//	private void updateReferenceArray(MemorySegment address, Object[] array) {
//		final MemoryLayout layout = transformer.layout();
//    	final var handle = Transformer.removeOffset(layout.arrayElementVarHandle());
//		final Function unmarshal = transformer.unmarshal();
//
//		for(int n = 0; n < array.length; ++n) {
//    		final MemorySegment element = (MemorySegment) handle.get(address, (long) n);
//    		array[n] = unmarshal.apply(element);
//
//    		//address.getAtIndex(AddressLayout.ADDRESS, 0);
//
//    	}
//	}
//
//	private void updateStructureArray(MemorySegment address, Object[] array) {
//		final MemoryLayout layout = transformer.layout();
//		final long size = layout.byteSize();
//		final Function unmarshal = transformer.unmarshal();
//
//		for(int n = 0; n < array.length; ++n) {
//    		final MemorySegment element = address.asSlice(n * size, size);
//    		array[n] = unmarshal.apply(element);
//		}
//	}
//}
