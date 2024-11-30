package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.Collection;

import org.junit.jupiter.api.*;

class TransformerRegistryTest {
	private TransformerRegistry registry;

	@BeforeEach
	void before() {
		registry = new TransformerRegistry();
	}

	@DisplayName("A transformer can be registered")
	@Test
	void add() {
		final var mapper = new PrimitiveNativeTransformer<>(int.class);
		registry.add(mapper);
		assertEquals(mapper, registry.get(int.class));
	}

	@DisplayName("The transformer for an unsupported type cannot be retrieved from the registry")
	@Test
	void unsupported() {
		assertThrows(IllegalArgumentException.class, () -> registry.get(int.class));
		assertThrows(IllegalArgumentException.class, () -> registry.get(int[].class));
	}

	@DisplayName("A transformer can be specialised to a given subclass")
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

	@DisplayName("An array can be transformed if its component type is supported")
	@Test
	void arrays() {
		final var mapper = new PrimitiveNativeTransformer<>(int.class);
		registry.add(mapper);
		registry.get(int[].class);
	}

	@DisplayName("A collection cannot be transformed")
	@Test
	void collections() {
		@SuppressWarnings("rawtypes")
		final var transformer = new AbstractNativeTransformer<Collection, Object>() {
			@Override
			public Class<? extends Collection> type() {
				return Collection.class;
			}

			@Override
			public Object transform(Collection collection, SegmentAllocator allocator) {
				return null;
			}
		};
		assertThrows(IllegalArgumentException.class, () -> registry.add(transformer));
		assertThrows(IllegalArgumentException.class, () -> registry.get(Collection.class));
	}
}
