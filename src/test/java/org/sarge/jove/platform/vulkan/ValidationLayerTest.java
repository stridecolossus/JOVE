package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.ptr.IntByReference;

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

	@SuppressWarnings("unchecked")
	@Test
	void supported() {
		final VulkanFunction<VkLayerProperties> func = mock(VulkanFunction.class);
		final IntByReference count = new IntByReference(1);
		final VkLayerProperties layer = new VkLayerProperties();
		layer.implementationVersion = 42;
		layer.layerName = "layer".getBytes();
		final var results = ValidationLayer.SUPPORTED_LAYERS.enumerate(func, count, layer);
		assertEquals(Set.of(new ValidationLayer("layer", 42)), results);
	}
}
