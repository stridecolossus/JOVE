package org.sarge.jove.platform.vulkan;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VulkanHandle.Destructor;

import com.sun.jna.Pointer;

public class VulkanHandleTest extends AbstractVulkanTest {
	private VulkanHandle handle;
	private Pointer ptr;
	private Destructor destructor;

	@BeforeEach
	public void before() {
		destructor = mock(Destructor.class);
		ptr = mock(Pointer.class);
		handle = new VulkanHandle(ptr, destructor);
	}

	@Test
	public void destroy() {
		handle.destroy();
		verify(destructor).destroy();
	}
}
