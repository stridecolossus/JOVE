package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.Collection;
import java.util.function.Function;

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
		final var mapper = new StringNativeTransformer();
		registry.add(mapper);
		assertEquals(mapper, registry.get(String.class));
	}

	@DisplayName("An array can be transformed if its component type is supported")
	@Test
	void array() {
		final var transformer = new StringNativeTransformer();
		registry.add(transformer);
		registry.get(String[].class);
	}

	@DisplayName("The transformer for an unsupported type cannot be retrieved from the registry")
	@Test
	void unsupported() {
		assertThrows(IllegalArgumentException.class, () -> registry.get(String.class));
		assertThrows(IllegalArgumentException.class, () -> registry.get(String[].class));
	}

	@DisplayName("A transformer can be specialised to a given subclass")
	@Test
	void derived() {
		final var transformer = new IdentityNativeTransformer<>(Number.class, ValueLayout.JAVA_INT);
		registry.add(transformer);
		assertEquals(transformer, registry.get(Long.class));
	}

	@DisplayName("Collections cannot be transformed")
	@Test
	void collection() {
		final var transformer = new NativeTransformer<Collection<Object>, MemorySegment>() {
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public Class type() {
				return Collection.class;
			}

			@Override
			public MemorySegment transform(Collection<Object> value, ParameterMode mode, SegmentAllocator allocator) {
				return null;
			}

			@Override
			public Function<MemorySegment, Collection<Object>> returns(Class<? extends Collection<Object>> type) {
				return null;
			}
		};
		assertThrows(IllegalArgumentException.class, () -> registry.add(transformer));
		assertThrows(IllegalArgumentException.class, () -> registry.get(Collection.class));
	}
}
