package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFormatProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceType;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class PhysicalDeviceTest {
	private PhysicalDevice dev;
	private VulkanLibrary lib;
	private Instance instance;
	private Family family;

	@BeforeEach
	void before() {
		// Create Vulkan
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
		when(lib.factory().integer()).thenReturn(new IntByReference());

		// Create an instance
		instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create a queue family
		family = new Family(0, 1, Set.of(VkQueueFlag.GRAPHICS));

		// Create device
		dev = new PhysicalDevice(new Pointer(42), instance, List.of(family));
	}

	// TODO - test enumerate, queues, etc

	@Test
	void constructor() {
		assertNotNull(dev.handle());
		assertEquals(instance, dev.instance());
		assertEquals(lib, dev.library());
		assertEquals(List.of(family), dev.families());
	}

	@Nested
	class PresentationFamilyTests {
		private IntByReference supported;
		private Handle surface;

		@BeforeEach
		void before() {
			surface = new Handle(new Pointer(42));
			supported = new IntByReference();
			when(lib.factory().integer()).thenReturn(supported);
		}

		@DisplayName("Find the queue family that supports presentation")
		@Test
		void presentation() {
			supported.setValue(1);
			assertEquals(Optional.of(family), dev.presentation(surface));
		}

		@DisplayName("Queue family should be empty if the device does not support presentation")
		@Test
		void invalid() {
			assertEquals(Optional.empty(), dev.presentation(surface));
		}
	}

	@Test
	void features() {
		final var features = dev.features();
		assertNotNull(features);
		verify(lib).vkGetPhysicalDeviceFeatures(eq(dev.handle()), any());
	}

	@Test
	void properties() {
		// Init properties
		final Answer<Void> answer = inv -> {
			final VkPhysicalDeviceProperties props = inv.getArgument(1);
			props.deviceName = "device".getBytes();
			props.deviceType = VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
			return null;
		};
		doAnswer(answer).when(lib).vkGetPhysicalDeviceProperties(eq(dev.handle()), any());

		// Retrieve properties
		final var props = dev.properties();
		assertNotNull(props);
		clearInvocations(lib);

		// Check cached
		assertEquals(props, dev.properties());
		verifyNoInteractions(lib);

		// Check properties
		assertEquals("device", dev.properties().name());
		assertEquals(VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU, dev.properties().type());
		assertNotNull(props.limits());
	}

	@Test
	void extensions() {
		assertEquals(Set.of(), dev.extensions());
	}

	@Test
	void layers() {
		assertEquals(Set.of(), dev.layers());
	}

	@Test
	void formatProperties() {
		final VkFormatProperties props = dev.properties(VkFormat.D32_SFLOAT);
		verify(lib).vkGetPhysicalDeviceFormatProperties(dev.handle(), VkFormat.D32_SFLOAT, props);
	}
}
