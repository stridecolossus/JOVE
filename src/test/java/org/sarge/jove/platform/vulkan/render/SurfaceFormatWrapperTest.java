package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

class SurfaceFormatWrapperTest {
	private SurfaceFormatWrapper wrapper;

	@BeforeEach
	void before() {
		wrapper = new SurfaceFormatWrapper(VkFormat.R32G32B32_SFLOAT, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);
	}

	@Test
	void equals() {
		assertEquals(wrapper, wrapper);
		assertEquals(wrapper, new SurfaceFormatWrapper(VkFormat.R32G32B32_SFLOAT, VkColorSpaceKHR.SRGB_NONLINEAR_KHR));
		assertNotEquals(wrapper, null);
		assertNotEquals(wrapper, new SurfaceFormatWrapper(VkFormat.UNDEFINED, VkColorSpaceKHR.SRGB_NONLINEAR_KHR));
	}

	@Test
	void structure() {
		final var structure = new VkSurfaceFormatKHR();
		structure.format = VkFormat.R32G32B32_SFLOAT;
		structure.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;
		assertEquals(wrapper, structure);
		assertNotEquals(structure, wrapper);
	}
}
