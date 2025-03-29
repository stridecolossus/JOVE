package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NativeRegistryTest {
	private NativeRegistry registry;

	@BeforeEach
	void before() {
		registry = NativeRegistry.create();
	}

	@Test
	void add() {
		final var transformer = new IdentityNativeTransformer<Integer>(ValueLayout.JAVA_INT);
		registry.add(int.class, transformer);
		assertEquals(transformer, registry.transformer(int.class));
	}

	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> new NativeRegistry().transformer(int.class));
	}

	@Test
	void derived() {
		final var transformer = new IdentityNativeTransformer<Number>(ValueLayout.JAVA_INT);
		registry.add(Number.class, transformer);
		assertEquals(transformer, registry.transformer(Integer.class));
	}

	private static List<Class<?>> primitives() {
		return List.of(
				boolean.class,
				byte.class,
				char.class,
				short.class,
				int.class,
				long.class,
				float.class,
				double.class
		);
	}

	@ParameterizedTest
	@MethodSource
	void primitives(Class<?> type) {
		assertNotNull(registry.transformer(type));
	}
}
