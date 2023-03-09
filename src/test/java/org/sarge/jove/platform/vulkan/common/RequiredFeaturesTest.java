package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

public class RequiredFeaturesTest {
	private RequiredFeatures features;

	@BeforeEach
	void before() {
		features = new RequiredFeatures(Set.of("samplerAnisotropy"));
	}

	@Test
	void features() {
		assertEquals(Set.of("samplerAnisotropy"), features.features());
	}

	@Test
	void structure() {
		final VkPhysicalDeviceFeatures struct = features.structure();
		assertEquals(true, struct.samplerAnisotropy);
	}

	@Test
	void require() {
		features.require("samplerAnisotropy");
	}

	@Test
	void missing() {
		assertThrows(IllegalStateException.class, () -> features.require("wideLines"));
	}
}
