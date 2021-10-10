package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageUsage;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class AllocationServiceTest extends AbstractVulkanTest {
	private AllocationService service;
	private Allocator allocator;
	private MemoryType type;
	private VkMemoryRequirements reqs;
	private MemoryProperties.Builder<VkImageUsage> props;

	@BeforeEach
	void before() {
		// Init memory requirements
		reqs = new VkMemoryRequirements();
		reqs.memoryTypeBits = 1;
		reqs.size = 42;

		// Init memory properties
		props = new MemoryProperties.Builder<>();
		props.usage(VkImageUsage.COLOR_ATTACHMENT);

		// Create memory type
		final Heap heap = new Heap(0, 0, Set.of());
		type = new MemoryType(0, heap, Set.of(VkMemoryProperty.DEVICE_LOCAL));

		// Create service
		allocator = mock(Allocator.class);
		service = new AllocationService(allocator, List.of(type));
	}

	@DisplayName("Allocate device memory with the specified minimal memory type")
	@Test
	void allocate() {
		props.required(VkMemoryProperty.DEVICE_LOCAL);
		service.allocate(reqs, props.build());
		verify(allocator).allocate(type, reqs.size);
	}

	@DisplayName("Allocate device memory with the specified optimal memory type")
	@Test
	void allocateOptimal() {
		props.optimal(VkMemoryProperty.DEVICE_LOCAL);
		service.allocate(reqs, props.build());
		verify(allocator).allocate(type, reqs.size);
	}

	@DisplayName("Fail if the memory type is filtered by the requirements")
	@Test
	void allocateTypeNotAvailable() {
		reqs.memoryTypeBits = 0;
		assertThrows(AllocationException.class, () -> service.allocate(reqs, props.build()));
	}

	@DisplayName("Fail if there is no memory type with the required properties")
	@Test
	void allocatePropertyNotAvailable() {
		props.required(VkMemoryProperty.HOST_CACHED);
		assertThrows(AllocationException.class, () -> service.allocate(reqs, props.build()));
	}

	@Test
	void create() {
		service = AllocationService.create(dev, allocator);
		assertNotNull(service);
		verify(lib).vkGetPhysicalDeviceMemoryProperties(eq(dev.parent()), isA(VkPhysicalDeviceMemoryProperties.class));
	}

	@Test
	void pool() {
		// Init hardware limits
		final var limits = new VkPhysicalDeviceLimits();
		limits.bufferImageGranularity = 1;
		limits.maxMemoryAllocationCount = 2;

		// Init device properties
		final PhysicalDevice.Properties props = mock(PhysicalDevice.Properties.class);
		when(props.limits()).thenReturn(limits);

		// Create physical device
		final PhysicalDevice physical = mock(PhysicalDevice.class);
		when(physical.properties()).thenReturn(props);
		when(dev.parent()).thenReturn(physical);

		// Create memory pool
		assertNotNull(AllocationService.pool(dev));
	}
}
