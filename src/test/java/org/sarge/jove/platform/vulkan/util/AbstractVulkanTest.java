package org.sarge.jove.platform.vulkan.util;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Base-class for a unit-test dependant on the Vulkan API and a logical device.
 * @author Sarge
 */
public abstract class AbstractVulkanTest {
	/**
	 * Identifier for a Vulkan integration test.
	 */
	public static final String INTEGRATION_TEST = "vulkan-integration-test";

	/**
	 * Arbitrary Vulkan format.
	 */
	public static final VkFormat FORMAT = VkFormat.R32G32B32A32_SFLOAT;

	protected LogicalDevice dev;
	protected VulkanLibrary lib;

	@BeforeEach
	private final void beforeVulkanTest() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(lib.factory()).thenReturn(factory);

		// Init reference types
		final IntByReference integer = new IntByReference(1);
		final PointerByReference pointer = new PointerByReference(new Pointer(2));
		final Pointer[] array = new Pointer[]{new Pointer(3)};
		when(factory.integer()).thenReturn(integer);
		when(factory.pointer()).thenReturn(pointer);
		when(factory.pointers(anyInt())).thenReturn(array);

		// Create logical device
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(new Pointer(4)));
		when(dev.library()).thenReturn(lib);
	}
}
