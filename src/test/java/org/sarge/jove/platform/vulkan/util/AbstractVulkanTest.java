package org.sarge.jove.platform.vulkan.util;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

import com.sun.jna.Pointer;

/**
 * Base-class for a unit-test dependant on the Vulkan API and a logical device.
 * @author Sarge
 */
public abstract class AbstractVulkanTest {
	protected MockReferenceFactory factory;
	protected LogicalDevice dev;
	protected VulkanLibrary lib;

	@BeforeEach
	private final void beforeVulkanTest() {
		// Create API
		lib = mock(VulkanLibrary.class);

		// Init reference factory
		factory = new MockReferenceFactory();
		when(lib.factory()).thenReturn(factory);

		// Create logical device
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(new Pointer(42)));
		when(dev.library()).thenReturn(lib);

		// Init supported features
		final var features = mock(DeviceFeatures.class);
		when(dev.features()).thenReturn(features);
		doCallRealMethod().when(features).check(anyString());
	}
}
