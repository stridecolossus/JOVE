package org.sarge.jove.platform.vulkan.util;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

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

		// Init properties and features
		final VulkanProperty.Provider provider = mock(VulkanProperty.Provider.class);

		// Create logical device
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(1));
		when(dev.library()).thenReturn(lib);
		when(dev.factory()).thenReturn(factory);
		when(dev.provider()).thenReturn(provider);
	}

	/**
	 * Sets a device property.
	 * @param key			Property key
	 * @param value			Value
	 * @param enabled		Whether the optional feature is enabled
	 */
	protected void property(VulkanProperty.Key key, Number value, boolean enabled) {
		// Create property
		final VulkanProperty prop = mock(VulkanProperty.class);
		when(dev.provider().property(key)).thenReturn(prop);
		when(prop.get()).thenReturn(value);

		// Mock disabled features
		if(!enabled) {
			doThrow(IllegalStateException.class).when(prop).validate();
		}

		// Mock argument validation
		final Answer<Void> answer = inv -> {
			if(!enabled) throw new IllegalStateException("Mocked property disabled");
			final float arg = inv.getArgument(0);
			if(arg > value.floatValue()) throw new IllegalArgumentException("Mocked property out-of-range");
			return null;
		};
		doAnswer(answer).when(prop).validate(anyFloat());
	}
}
