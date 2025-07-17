package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.Registry.Factory;

class RegistryTest {
	private Registry registry;
	private Transformer transformer;

	@BeforeEach
	void before() {
		registry = new Registry();
		transformer = new IdentityTransformer(ValueLayout.JAVA_INT);
	}

	@DisplayName("A transformer can be explicitly registered for a domain type")
	@Test
	void add() {
		registry.add(Integer.class, transformer);
		assertEquals(Optional.of(transformer), registry.transformer(Integer.class));
	}

	@DisplayName("A transformer can be generated on demand via a registered factory")
	@Test
	void factory() {
		final Factory<Integer> factory = _ -> transformer;
		registry.add(Integer.class, factory);
		assertEquals(Optional.of(transformer), registry.transformer(Integer.class));
	}

	@DisplayName("A transformer can be derived from a registered supertype")
	@Test
	void derived() {
		registry.add(Number.class, transformer);
		assertEquals(Optional.of(transformer), registry.transformer(Integer.class));
	}

	@DisplayName("A supported type also automatically generates a transformer for an array of that type")
	@Test
	void array() {
		registry.add(Integer.class, transformer);
		registry.transformer(Integer[].class).orElseThrow();
	}

	@DisplayName("An unsupported type cannot be transformed")
	@Test
	void unsupported() {
		assertEquals(Optional.empty(), registry.transformer(String.class));
	}
}
