package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.Registry.Factory;

class RegistryTest {
	private Registry registry;
	private PrimitiveTransformer<Number> transformer;

	@BeforeEach
	void before() {
		transformer = new PrimitiveTransformer<>(ValueLayout.JAVA_INT);
		registry = new Registry();
		registry.register(Number.class, transformer);
	}

	@Test
	void transformer() {
		assertEquals(Optional.of(transformer), registry.transformer(Number.class));
	}

	@Test
	void subclass() {
		assertEquals(Optional.of(transformer), registry.transformer(Integer.class));
	}

	@Test
	void replace() {
		final var other = new PrimitiveTransformer<Number>(ValueLayout.JAVA_INT);
		registry.register(Number.class, other);
		assertEquals(Optional.of(other), registry.transformer(Number.class));
	}

	@Test
	void array() {
		registry.register(String.class, new StringTransformer());
		final var array = registry.transformer(String[].class).get();
		assertEquals(ValueLayout.ADDRESS, array.layout());
	}

	@Test
	void factory() {
		final Factory<Number> factory = _ -> transformer;
		registry.register(Number.class, factory);
		assertEquals(Optional.of(transformer), registry.transformer(Number.class));
		assertEquals(Optional.of(transformer), registry.transformer(Integer.class));
	}

	@Test
	void unknown() {
		assertEquals(Optional.empty(), registry.transformer(String.class));
	}
}
