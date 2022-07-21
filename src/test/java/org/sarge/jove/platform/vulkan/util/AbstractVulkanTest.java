package org.sarge.jove.platform.vulkan.util;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.ReferenceFactory;

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
	protected ReferenceFactory factory;

	@BeforeEach
	private final void beforeVulkanTest() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Init reference factory
		factory = mock(ReferenceFactory.class);
		when(factory.integer()).thenReturn(INTEGER);
		when(factory.pointer()).thenReturn(POINTER);

		// Init device limits
		final DeviceLimits limits = mock(DeviceLimits.class);

		// Create logical device
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(1));
		when(dev.library()).thenReturn(lib);
		when(dev.factory()).thenReturn(factory);
		when(dev.limits()).thenReturn(limits);
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
