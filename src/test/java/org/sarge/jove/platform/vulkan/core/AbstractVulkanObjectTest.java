package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject.Destructor;

import com.sun.jna.Pointer;

public class AbstractVulkanObjectTest {
	private AbstractVulkanObject obj;
	private LogicalDevice dev;
	private Destructor destructor;
	private Pointer ptr;

	@BeforeEach
	void before() {
		// Create object handle
		ptr = new Pointer(1);

		// Create logical device
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(new Pointer(2)));

		// Create destructor
		destructor = mock(Destructor.class);

		// Create object
		obj = new AbstractVulkanObject(ptr, dev, destructor) {
			// Mock implementation
		};
	}

	@Test
	void constructor() {
		assertNotNull(obj.handle());
		assertNotNull(obj.device());
		assertEquals(false, obj.isDestroyed());
	}

	@Test
	void handle() {
		assertEquals(obj.handle(), AbstractVulkanObject.handle(obj));
		assertEquals(null, AbstractVulkanObject.handle(null));
	}

	@Test
	void destroy() {
		final Handle handle = obj.handle();
		obj.destroy();
		assertEquals(true, obj.isDestroyed());
		verify(destructor).destroy(dev.handle(), handle, null);
	}

	@Test
	void destroyAlreadyDestroyed() {
		obj.destroy();
		assertThrows(IllegalStateException.class, () -> obj.destroy());
	}
}
