package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class TransformerRegistryTest {
	private TransformerRegistry registry;

	@BeforeEach
	void before() {
		registry = new TransformerRegistry();
	}

	@Test
	void add() {
		final var mapper = new PrimitiveNativeTransformer<>(int.class);
		registry.add(mapper);
		assertEquals(mapper, registry.get(int.class));
	}

	@Test
	void unsupported() {
		assertThrows(IllegalArgumentException.class, () -> registry.get(int.class));
	}

	@Test
	void derived() {
		final var transformer = new AbstractNativeTransformer<Number, MemorySegment>() {
			@Override
			public Class<Number> type() {
				return Number.class;
			}

			@Override
			public Object transform(Number instance, SegmentAllocator allocator) {
				return instance;
			}
		};
		registry.add(transformer);

		final var derived = registry.get(Integer.class);
		assertEquals(Number.class, derived.type());
	}
}
