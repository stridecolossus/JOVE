package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject.Destructor;
import org.sarge.jove.util.PointerArray;

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

	@Test
	void toPointerArray() {
		final PointerArray array = AbstractVulkanObject.toPointerArray(List.of(obj, obj));
		assertNotNull(array);
		assertEquals(2 * 8, array.size());
	}

	@Test
	void toPointerArrayEmpty() {
		assertEquals(null, AbstractVulkanObject.toPointerArray(List.of()));
	}
}
