package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VulkanLibrary.Version;

public class VulkanRenderingSystemTest {
	private VulkanRenderingSystem sys;
	private VulkanInstance instance;

	@BeforeEach
	public void before() {
		instance = mock(VulkanInstance.class);
		sys = new VulkanRenderingSystem(instance);
	}

	@Test
	public void constructor() {
		assertEquals("vulkan", sys.name());
		assertEquals(new Version(1, 0, 2).toString(), sys.version());
	}

	@Test
	public void close() {
		sys.close();
		verify(instance).destroy();
	}
}
