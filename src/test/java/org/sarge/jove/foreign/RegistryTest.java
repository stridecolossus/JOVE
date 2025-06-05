package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.Registry.Factory;

class RegistryTest {
	private Registry registry;
	private Transformer<Number> transformer;

	@BeforeEach
	void before() {
		registry = new Registry();
		transformer = new IdentityTransformer<>(ValueLayout.JAVA_INT);
	}

	@DisplayName("A transformer can be explicitly registered for a domain type")
	@Test
	void add() {
		registry.add(Number.class, transformer);
		assertEquals(transformer, registry.transformer(Number.class));
	}

	@DisplayName("A transformer can be generated on demand via a registered factory")
	@Test
	void factory() {
		final Factory<Number> factory = _ -> transformer;
		registry.add(Number.class, factory);
		assertEquals(transformer, registry.transformer(Number.class));
	}

	@DisplayName("A transformer can be derived from a registered supertype")
	@Test
	void derived() {
		registry.add(Number.class, transformer);
		assertEquals(transformer, registry.transformer(Integer.class));
	}

	@DisplayName("A supported type also automatically generates a transformer for an array of that type")
	@Test
	void array() {
		registry.add(Number.class, transformer);
		assertTrue(registry.transformer(Number[].class) instanceof ArrayTransformer);
	}

	@DisplayName("An unsupported type cannot be transformed")
	@Test
	void unsupported() {
		assertThrows(IllegalArgumentException.class, () -> registry.transformer(Number.class));
	}
}
