package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

public class DeviceFeaturesTest {
	private static final String FEATURE = "samplerAnisotropy";

	private VkPhysicalDeviceFeatures struct;

	@BeforeEach
	void before() {
		struct = new VkPhysicalDeviceFeatures();
		struct.samplerAnisotropy = true;
	}

	@Nested
	class EmptyFeaturesTest {
		@Test
		void features() {
			assertEquals(Set.of(), DeviceFeatures.EMPTY.features());
		}

		@Test
		void descriptor() {
			assertNull(DeviceFeatures.EMPTY.descriptor());
		}

		@Test
		void contains() {
			assertEquals(true, DeviceFeatures.EMPTY.contains(DeviceFeatures.EMPTY));
			assertEquals(false, DeviceFeatures.EMPTY.contains(mock(DeviceFeatures.class)));
		}
	}

	@Nested
	class RequiredFeaturesTest {
		private DeviceFeatures required;

		@BeforeEach
		void before() {
			required = DeviceFeatures.required(Set.of(FEATURE));
		}

		@Test
		void features() {
			assertEquals(Set.of(FEATURE), required.features());
		}

		@Test
		void descriptor() {
			final VkPhysicalDeviceFeatures descriptor = required.descriptor();
			assertNotNull(descriptor);
			assertEquals(true, descriptor.samplerAnisotropy);
		}

		@Test
		void contains() {
			assertEquals(true, required.contains(required));
			assertEquals(true, required.contains(DeviceFeatures.required(Set.of(FEATURE))));
			assertEquals(true, required.contains(DeviceFeatures.supported(struct)));
			assertEquals(true, required.contains(DeviceFeatures.required(Set.of())));
			assertEquals(false, required.contains(DeviceFeatures.required(Set.of("wideLines"))));
		}
	}

	@Nested
	class SupportedFeaturesTest {
		private DeviceFeatures supported;

		@BeforeEach
		void before() {
			supported = DeviceFeatures.supported(struct);
		}

		@Test
		void features() {
			assertEquals(Set.of(FEATURE), supported.features());
		}

		@Test
		void descriptor() {
			final VkPhysicalDeviceFeatures descriptor = supported.descriptor();
			assertNotNull(descriptor);
			assertEquals(true, descriptor.samplerAnisotropy);
		}

		@Test
		void contains() {
			assertEquals(true, supported.contains(supported));
			assertEquals(true, supported.contains(DeviceFeatures.supported(struct)));
			assertEquals(true, supported.contains(DeviceFeatures.required(Set.of(FEATURE))));
			assertEquals(true, supported.contains(DeviceFeatures.supported(new VkPhysicalDeviceFeatures())));
			assertEquals(false, supported.contains(DeviceFeatures.required(Set.of("wideLines"))));
		}
	}
}
