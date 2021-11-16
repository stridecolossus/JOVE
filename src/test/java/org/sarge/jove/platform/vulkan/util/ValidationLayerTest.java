package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.ValidationLayer.ValidationLayerSet;

import com.sun.jna.ptr.IntByReference;

public class ValidationLayerTest {
	private static final String NAME = "layer";
	private static final int VERSION = 2;

	private ValidationLayer layer;

	@BeforeEach
	void before() {
		layer = new ValidationLayer(NAME, VERSION);
	}

	@Test
	void constructor() {
		assertEquals(NAME, layer.name());
		assertEquals(VERSION, layer.version());
	}

	@Test
	void of() {
		final var props = new VkLayerProperties();
		props.layerName = NAME.getBytes();
		props.implementationVersion = VERSION;
		assertEquals(layer, ValidationLayer.of(props));
	}

	@Nested
	class ValidationLayerSetTests {
		private Set<ValidationLayer> set;

		@BeforeEach
		void before() {
			set = new ValidationLayerSet();
			set.add(layer);
		}

		@Test
		void enumerate() {
			assertEquals(Set.of(layer), set);
		}

		@Test
		void contains() {
			assertEquals(true, set.contains(layer));
			assertEquals(true, set.contains(new ValidationLayer(NAME, 2)));
		}

		@Test
		void containsLowerVersion() {
			assertEquals(true, set.contains(new ValidationLayer(NAME, 1)));
		}

		@Test
		void containsHigherVersion() {
			assertEquals(false, set.contains(new ValidationLayer(NAME, 3)));
		}

		@Test
		void containsOtherLayer() {
			assertEquals(false, set.contains(new ValidationLayer("other", 2)));
		}
	}

	@Test
	void enumerate() {
		final VulkanLibrary lib = mock(VulkanLibrary.class);
		final VulkanFunction<VkLayerProperties> func = mock(VulkanFunction.class);
		final IntByReference count = new IntByReference(0);
		final Set<ValidationLayer> layers = ValidationLayer.layers(lib, count, func);
		assertEquals(Set.of(), layers);
	}
}
