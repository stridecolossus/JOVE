package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.ValidationLayer.ValidationLayerSupport;

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
	void support() {
		// Create API
		final VulkanLibrary lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create support function
		final VulkanFunction<VkLayerProperties> func = mock(VulkanFunction.class);

		// Create support helper
		final ValidationLayerSupport support = new ValidationLayerSupport() {
			@Override
			protected ValidationLayer map(VkLayerProperties struct) {
				return new ValidationLayer("layer", 1);
			}
		};

		// Enumerate layers
		final Set<ValidationLayer> layers = support.enumerate(lib, func);
		assertNotNull(layers);
	}
}
