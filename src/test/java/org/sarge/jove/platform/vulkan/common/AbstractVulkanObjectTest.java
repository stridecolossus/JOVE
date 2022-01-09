package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject.Destructor;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.Pointer;

public class AbstractVulkanObjectTest {
	private AbstractVulkanObject obj;
	private DeviceContext dev;
	private Destructor<AbstractVulkanObject> destructor;
	private Pointer ptr;
	private boolean released;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		// Create object handle
		ptr = new Pointer(1);

		// Create context
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(2));

		// Create destructor
		destructor = mock(Destructor.class);
		released = false;

		// Create object
		obj = new AbstractVulkanObject(ptr, dev) {
			@Override
			protected Destructor<AbstractVulkanObject> destructor(VulkanLibrary lib) {
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
		verify(destructor).destroy(dev, obj, null);
		assertTrue(released);
	}

	@Test
	void hash() {
		assertEquals(1, obj.hashCode());
	}

	@Test
	void equals() {
		assertEquals(true, obj.equals(obj));
		assertEquals(false, obj.equals(null));
		assertEquals(false, obj.equals(mock(AbstractVulkanObject.class)));
	}
}
