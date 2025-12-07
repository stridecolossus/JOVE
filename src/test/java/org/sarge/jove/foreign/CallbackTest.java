package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.Callback.CallbackTransformerFactory;

class CallbackTest {
	private CallbackTransformerFactory factory;
	private Transformer<Callback, MemorySegment> transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		final var registry = new Registry();
		registry.add(int.class, new PrimitiveTransformer<>(ValueLayout.JAVA_INT));
		allocator = Arena.ofAuto();
		factory = new CallbackTransformerFactory(registry);
		transformer = factory.transformer(MockCallback.class);
	}

	@Test
	void layout() {
		assertEquals(AddressLayout.ADDRESS, transformer.layout());
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@Test
	void marshal() {
		final var instance = new MockCallback() {
			@Override
			public int callback(int parameter) {
				return 0;
			}
		};
		final MemorySegment address = transformer.marshal(instance, allocator);
		assertNotNull(address);
	}

	// TODO - can we even test the stub? white-box test?
	// TODO - test unsupported return type/parameters? (not needed if reuses framework)

	@Test
	void unsupported() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}

	@Test
	void functional() {
		interface InvalidCallback extends Callback {
			void one();
			void two();
		}
		assertThrows(IllegalArgumentException.class, () -> factory.transformer(InvalidCallback.class));
	}
}
