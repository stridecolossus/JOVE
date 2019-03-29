package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;

public class SurfaceTest extends AbstractVulkanTest {
	private Surface surface;
	private VulkanInstance instance;

	@BeforeEach
	public void before() {
		instance = mock(VulkanInstance.class);
		surface = new Surface(mock(Pointer.class), instance, new VkSurfaceCapabilitiesKHR(), List.of(new VkSurfaceFormatKHR()), Set.of(VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR));
	}

	@Test
	public void constructor() {
		assertNotNull(surface.handle());
		assertNotNull(surface.capabilities());
		assertNotNull(surface.formats());
		assertNotNull(surface.modes());
	}

	@Test
	public void create() {
		final VulkanInstance instance = mock(VulkanInstance.class);
		final PhysicalDevice dev = mock(PhysicalDevice.class);
		when(dev.vulkan()).thenReturn(vulkan);
		final Pointer handle = mock(Pointer.class);
		surface = Surface.create(handle, instance, dev);
		assertNotNull(surface);
		assertEquals(handle, surface.handle());
	}
}
