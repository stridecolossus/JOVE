package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class NativeMapperRegistryTest {
	private NativeMapperRegistry registry;

	@BeforeEach
	void before() {
		registry = new NativeMapperRegistry();
	}

	@Test
	void add() {
		final var mapper = new PrimitiveNativeMapper<>(int.class);
		registry.add(mapper);
		assertEquals(mapper, registry.mapper(int.class));
	}

	@Test
	void derived() {
		final var mapper = new AbstractNativeMapper<Number, MemorySegment>() {
			@Override
			public Class<Number> type() {
				return Number.class;
			}

			@Override
			public Object marshal(Number instance, SegmentAllocator allocator) {
				return instance;
			}
		};
		registry.add(mapper);

		final var derived = registry.mapper(Integer.class);
		assertEquals(Number.class, derived.type());
	}

	@Test
	void unsupported() {
		assertThrows(IllegalArgumentException.class, () -> registry.mapper(int.class));
	}
}
