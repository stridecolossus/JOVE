package org.sarge.jove.platform.vulkan.util;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.AllocationService;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

/**
 * Base-class for a unit-test dependant on the Vulkan API and a logical device.
 * @author Sarge
 */
public abstract class AbstractVulkanTest {
	/**
	 * Arbitrary Vulkan format.
	 */
	public static final VkFormat FORMAT = VkFormat.R32G32B32A32_SFLOAT;

	protected LogicalDevice dev;
	protected VulkanLibrary lib;
	protected ReferenceFactory factory;
	protected AllocationService allocator;

	@BeforeEach
	final void beforeVulkanTest() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Init reference factory
		factory = mock(ReferenceFactory.class);
		when(factory.integer()).thenReturn(new IntByReference(1));
		when(factory.pointer()).thenReturn(new PointerByReference(new Pointer(2)));

		// Init device limits
		final DeviceLimits limits = mock(DeviceLimits.class);

		// Init memory allocator
		allocator = mock(AllocationService.class);

		// Create logical device
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(3));
		when(dev.library()).thenReturn(lib);
		when(dev.factory()).thenReturn(factory);
		when(dev.limits()).thenReturn(limits);
		when(dev.allocator()).thenReturn(allocator);
	}

	/**
	 * Sets a device limit.
	 * @param name		Limit name
	 * @param value		Limit
	 */
	protected void limit(String name, Number value) {
		when(dev.limits().value(name)).thenReturn(value);
	}
}
