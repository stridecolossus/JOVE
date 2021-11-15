package org.sarge.jove.platform.vulkan.util;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

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

	/**
	 * Integer returned-by-reference.
	 */
	public static final IntByReference INTEGER = new IntByReference(1);

	/**
	 * Pointer returned-by-reference.
	 */
	public static final PointerByReference POINTER = new PointerByReference(new Pointer(2));

	protected LogicalDevice dev;
	protected VulkanLibrary lib;

	@BeforeEach
	private final void beforeVulkanTest() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(factory.integer()).thenReturn(INTEGER);
		when(factory.pointer()).thenReturn(POINTER);
		when(factory.array(anyInt())).thenReturn(new Pointer[]{new Pointer(3)});
		when(lib.factory()).thenReturn(factory);

		// Create logical device
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(1));
		when(dev.library()).thenReturn(lib);
	}
}
