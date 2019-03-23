package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.Vulkan.ReferenceFactory;

public class VulkanTest {
	private Vulkan vulkan;
	private VulkanLibrary lib;

	@BeforeEach
	public void before() {
		lib = mock(VulkanLibrary.class);
		vulkan = new Vulkan(lib, ReferenceFactory.DEFAULT);
	}

	@Test
	public void constructor() {
		assertEquals(lib, vulkan.library());
		assertEquals(ReferenceFactory.DEFAULT, vulkan.factory());
	}

	@Test
	public void supported() {
		final Supported supported = vulkan.supported();
		assertNotNull(supported);
		// TODO - verify
	}

	@Test
	public void initMocked() {
		Vulkan.init(vulkan);
		assertEquals(vulkan, Vulkan.instance());
	}

	@Disabled
	@Tag("Vulkan")
	@Test
	// TODO
	public void init() {
		Vulkan.init();
		vulkan = Vulkan.instance();
		assertNotNull(vulkan);
		assertNotNull(vulkan.library());
		assertEquals(ReferenceFactory.DEFAULT, vulkan.factory());
		assertNotNull(vulkan.supported());
	}
}
