package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VulkanHandle.Destructor;

import com.sun.jna.Pointer;

public class SurfaceTest extends AbstractVulkanTest {
	private Surface surface;

	@BeforeEach
	public void before() {
		final VulkanHandle handle = new VulkanHandle(new Pointer(42), mock(Destructor.class));
		surface = new Surface(handle, new VkSurfaceCapabilitiesKHR(), List.of(new VkSurfaceFormatKHR()), Set.of(VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR));
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
