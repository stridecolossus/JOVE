package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class SurfaceTest {
	private Surface surface;
	private Pointer handle;
	private PhysicalDevice dev;
	private Instance instance;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create instance
		instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create device
		dev = mock(PhysicalDevice.class);
		when(dev.instance()).thenReturn(instance);

		// Create surface
		handle = new Pointer(42);
		surface = new Surface(handle, dev);
	}

	@Test
	void constructor() {
		assertEquals(handle, surface.handle());
	}

	@Test
	void capabilities() {
		final var caps = surface.capabilities();
		assertNotNull(caps);
		verify(lib).vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.handle(), surface.handle(), caps);
	}

	@Test
	void formats() {
		final var formats = surface.formats();
		assertNotNull(formats);
		verify(lib).vkGetPhysicalDeviceSurfaceFormatsKHR(eq(dev.handle()), eq(surface.handle()), isA(IntByReference.class), isA(VkSurfaceFormatKHR.class));
	}

	@Test
	void modes() {
		final var modes = surface.modes();
		assertNotNull(modes);
		verify(lib).vkGetPhysicalDeviceSurfacePresentModesKHR(eq(dev.handle()), eq(surface.handle()), isA(IntByReference.class), isA(VkPresentModeKHR[].class));
	}

	@Test
	void destroy() {
		surface.destroy();
		verify(lib).vkDestroySurfaceKHR(instance.handle(), handle, null);
	}
}
