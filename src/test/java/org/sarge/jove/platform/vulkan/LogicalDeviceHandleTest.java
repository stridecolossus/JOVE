package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.LogicalDeviceHandle.Destructor;

import com.sun.jna.Pointer;

public class LogicalDeviceHandleTest extends AbstractVulkanTest {
	private LogicalDeviceHandle handle;
	private Pointer ptr;
	private Destructor destructor;
	private boolean cleaned;

	@BeforeEach
	public void before() {
		ptr = mock(Pointer.class);
		destructor = mock(Destructor.class);
		cleaned = false;
		handle = new LogicalDeviceHandle(ptr, device, lib -> destructor) {
			@Override
			protected void cleanup() {
				cleaned = true;
			}
		};
	}

	@Test
	public void constructor() {
		assertEquals(ptr, handle.handle());
		assertEquals(device, handle.dev);
		assertEquals(false, handle.isDestroyed());
	}

	@Test
	public void destroy() {
		handle.destroy();
		assertEquals(true, handle.isDestroyed());
		verify(destructor).destroy(device.handle(), ptr, null);
		assertEquals(true, cleaned);
	}
}
