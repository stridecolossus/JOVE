package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.util.function.Function;

import org.junit.jupiter.api.*;

class NativeTransformerAdapterTest {
	private NativeTransformerAdapter<String> adapter;
	private String arg;

	@BeforeEach
	void before() {
		final var delegate = new NativeTransformer<String>() {
			@Override
			public MemoryLayout layout() {
				return ValueLayout.ADDRESS;
			}

			@Override
			public Object marshal(String arg, SegmentAllocator allocator) {
				return arg;
			}

			@Override
			public Function<? extends Object, String> unmarshal() {
				return Function.identity();
			}
		};

		arg = "whatever";
		adapter = new NativeTransformerAdapter<>(delegate);
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, adapter.layout());
	}

	@Test
	void marshal() {
		assertEquals(arg, adapter.marshal(arg, null));
	}

	@Test
	void marshalNull() {
		assertEquals(MemorySegment.NULL, adapter.marshal(null, null));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	void unmarshal() {
		final Function unmarshal = adapter.unmarshal();
		assertEquals(arg, unmarshal.apply(arg));
		assertEquals(null, unmarshal.apply(MemorySegment.NULL));
	}
}
