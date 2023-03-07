package org.sarge.jove.platform.vulkan.common;

import static org.mockito.Mockito.mock;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.DeviceLimits;
import org.sarge.jove.util.*;

public class MockDeviceContext implements DeviceContext {
	private final VulkanLibrary lib = mock(VulkanLibrary.class);
	private final ReferenceFactory factory = new MockReferenceFactory();

	@Override
	public Handle handle() {
		return new Handle(1);
	}

	@Override
	public VulkanLibrary library() {
		return lib;
	}

	@Override
	public ReferenceFactory factory() {
		return factory;
	}

//	@Override
//	public AllocationService allocator() {
//		// TODO
//		final var service = mock(AllocationService.class);
//		when(service.allocate(any(VkMemoryRequirements.class), any(MemoryProperties.class))).thenReturn(new MockDeviceMemory());
//		return service;
//	}

	@Override
	public DeviceLimits limits() {
		// TODO
		return mock(DeviceLimits.class);
	}
}
