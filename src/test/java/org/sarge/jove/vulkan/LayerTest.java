package org.sarge.jove.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class LayerTest {
	private Layer layer;

	@BeforeEach
	void before() {
		layer = new Layer("layer", 42);
	}

	@Test
	void constructor() {
		assertEquals("layer", layer.name());
		assertEquals(42, layer.version());
	}

	@Test
	void invalidVersionNumber() {
		assertThrows(IllegalArgumentException.class, () -> new Layer("layer", 0));
	}

	@Test
	void compare() {
		assertEquals(0, layer.compareTo(layer));
		assertEquals(-1, layer.compareTo(new Layer("layer", 1)));
		assertEquals(+1, layer.compareTo(new Layer("layer", 999)));
	}

	@Nested
	class ContainsTests {
		@Test
		void self() {
			assertEquals(true, Layer.contains(Set.of(layer), layer));
		}

		@Test
		void other() {
			assertEquals(false, Layer.contains(Set.of(layer), new Layer("other", 42)));
		}

		@Test
		void lower() {
			assertEquals(false, Layer.contains(Set.of(layer), new Layer("layer", 1)));
		}

		@Test
		void higher() {
			assertEquals(true, Layer.contains(Set.of(layer), new Layer("layer", 999)));
		}
	}
}
