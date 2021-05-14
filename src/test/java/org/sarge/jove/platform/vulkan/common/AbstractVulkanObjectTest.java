package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject.Destructor;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

import com.sun.jna.Pointer;

public class AbstractVulkanObjectTest {
	private AbstractVulkanObject obj;
	private DeviceContext dev;
	private Destructor destructor;
	private Pointer ptr;
	private boolean released;

	@BeforeEach
	void before() {
		// Create object handle
		ptr = new Pointer(1);

		// Create context
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(new Pointer(2)));

		// Create destructor
		destructor = mock(Destructor.class);
		released = false;

		// Create object
		obj = new AbstractVulkanObject(ptr, dev) {
			@Override
			protected Destructor destructor(VulkanLibrary lib) {
				return destructor;
			}

			@Override
			protected void release() {
				assertFalse(released);
				released = true;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(new Handle(ptr), obj.handle());
		assertEquals(dev, obj.device());
		assertEquals(destructor, obj.destructor(null));
		assertEquals(false, obj.isDestroyed());
	}

	@Test
	void destroy() {
		obj.destroy();
		assertEquals(true, obj.isDestroyed());
		verify(destructor).destroy(dev.handle(), obj.handle(), null);
		assertTrue(released);
	}
}
