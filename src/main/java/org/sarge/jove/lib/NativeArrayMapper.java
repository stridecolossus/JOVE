package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.*;

import org.sarge.jove.lib.NativeMapper.*;

public class NativeArrayMapper extends AbstractNativeMapper<Object[]> implements ReturnMapper<Object[], MemorySegment>, ReturnedParameterMapper<Object[]> {

	protected NativeArrayMapper() {
		super(Object[].class);
	}

	@Override
	public Object marshal(Object[] array, NativeContext context) {
		final Class component = array.getClass().getComponentType();
		final NativeMapper mapper = context.registry().mapper(component).orElseThrow();

		final MemoryLayout layout = mapper.layout(component);

//		System.out.println("type="+component);
//		switch(layout) {
//			case AddressLayout addr -> System.out.println("address="+addr);
//			case ValueLayout value -> System.out.println("value="+value);
//			case StructLayout struct -> System.out.println("struct="+struct);
//			default -> System.out.println("other="+layout);
//		}

		final MemorySegment address = context.allocator().allocate(layout, array.length);

		// TODO
		// - problem is that structure mapper will ALLOCATE off-heap memory for each structure!!!
		// - init the address of each structure after allocate above?
		// - or separate allocation from population altogether?
		// - i.e. THIS mapper should be responsible for allocation of CONTIGUOUS memory blocks for arrays (ditto for collections)

		for(int n = 0; n < array.length; ++n) {
			if(array[n] == null) {
				continue;
			}
			final MemorySegment element = (MemorySegment) context.marshal(mapper, array[n], component); // mapper.marshal(array[n], context);
			address.setAtIndex(ADDRESS, n, element);
		}

		return address;
	}

	@Override
	public Object[] unmarshal(MemorySegment value, Class<? extends Object[]> type) {
		throw new UnsupportedOperationException("TODO"); // TODO
	}

	@Override
	public void unmarshal(MemorySegment address, Object[] array) {
		final Class component = array.getClass().getComponentType();
		final var mapper = NativeMapperRegistry.create().mapper(component).orElseThrow();
		final var layout = mapper.layout(component);
		final var returns = (ReturnMapper) mapper;

		for(int n = 0; n < array.length; ++n) {
			final MemorySegment element = address.asSlice(n * layout.byteSize(), layout.byteSize());
					//address.getAtIndex(ADDRESS, n);
//			mapper.unmarshal(element, array[n]);
			array[n] = returns.unmarshal(element, component);
		}
	}
}
