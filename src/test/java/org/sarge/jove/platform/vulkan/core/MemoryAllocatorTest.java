package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.core.MemoryAllocator.Allocation;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class MemoryAllocatorTest extends AbstractVulkanTest {
	private MemoryAllocator allocator;
	private MemoryAllocator.Allocation allocation;

	@BeforeEach
	void before() {
		// Create a physical device
		final Handle handle = new Handle(new Pointer(2));
		final PhysicalDevice parent = mock(PhysicalDevice.class);
		when(parent.handle()).thenReturn(handle);
		when(dev.parent()).thenReturn(parent);

		// Create a memory type
		final Set<VkMemoryPropertyFlag> flags = Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		final VkMemoryType type = new VkMemoryType();
		type.propertyFlags = IntegerEnumeration.mask(flags);

		// Populate the memory types for the device
		final Answer<Void> answer = inv -> {
			final VkPhysicalDeviceMemoryProperties props = inv.getArgument(1);
			props.memoryTypeCount = 1;
			props.memoryTypes = new VkMemoryType[]{type};
			return null;
		};
		doAnswer(answer).when(lib).vkGetPhysicalDeviceMemoryProperties(eq(handle), isA(VkPhysicalDeviceMemoryProperties.class));

		// Create allocator
		allocator = MemoryAllocator.create(dev);

		// Init allocation request
		allocation = allocator.allocation()
				.filter(1)
				.size(2)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
	}

	@Test
	void constructor() {
		assertNotNull(allocator);
	}

	@Test
	void allocation() {
		assertNotNull(allocation);
		assertEquals(2, allocation.size());
	}

	@Test
	void allocate() {
		// Allocate memory
		final Pointer mem = allocation.allocate();
		assertEquals(factory.ptr.getValue(), mem);

		// Check API
		final ArgumentCaptor<VkMemoryAllocateInfo> captor = ArgumentCaptor.forClass(VkMemoryAllocateInfo.class);
		verify(lib).vkAllocateMemory(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

		// Check allocation descriptor
		final var info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.memoryTypeIndex);
		assertEquals(2, info.allocationSize);
	}

	@Test
	void init() {
		// Create memory requirements
		final VkMemoryRequirements reqs = new VkMemoryRequirements();
		reqs.size = 3;
		reqs.memoryTypeBits = 4;

		// Init memory requirements
		allocation.init(reqs);
		assertEquals(3, allocation.size());

		// Check allocation
		final Allocation expected = allocator.allocation()
				.size(3)
				.filter(4)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		assertEquals(expected, allocation);
	}

	@Test
	void allocateOutOfMemory() {
		// TODO
	}

	@Test
	void allocateEmptyAllocation() {
		assertThrows(IllegalArgumentException.class, () -> allocator.allocation().allocate());
	}

	@Test
	void allocateMemoryTypeNotAvailable() {
		allocation.filter(0);
		assertThrows(RuntimeException.class, () -> allocation.allocate());
	}

	@Test
	void allocateMemoryPropertyNotAvailable() {
		allocation.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_PROTECTED_BIT);
		assertThrows(RuntimeException.class, () -> allocation.allocate());
	}
}
