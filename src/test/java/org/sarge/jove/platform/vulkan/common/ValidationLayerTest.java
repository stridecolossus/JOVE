package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValidationLayerTest {
	private ValidationLayer layer;

	@BeforeEach
	void before() {
		layer = new ValidationLayer("layer", 42);
	}

	@Test
	void constructor() {
		assertEquals("layer", layer.name());
		assertEquals(42, layer.version());
	}

	@Test
	void isPresentSelf() {
		assertEquals(true, layer.isPresent(Set.of(layer)));
	}

	@Test
	void isPresentOther() {
		final ValidationLayer other = new ValidationLayer("other");
		assertEquals(false, layer.isPresent(Set.of(other)));
	}

	@Test
	void isPresentLower() {
		final ValidationLayer lower = new ValidationLayer("layer", 1);
		assertEquals(true, layer.isPresent(Set.of(lower)));
	}

	@Test
	void isPresentHigher() {
		final ValidationLayer higher = new ValidationLayer("layer", 999);
		assertEquals(false, layer.isPresent(Set.of(higher)));
	}
}
