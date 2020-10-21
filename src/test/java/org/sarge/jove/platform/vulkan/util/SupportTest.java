package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.Support.Extensions;

public class SupportTest {
	private static final String RESULT = "result";

	private VulkanLibrary lib;
	private VulkanFunction<VkExtensionProperties> func;
	private VkExtensionProperties identity;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create enumeration function
		func = mock(VulkanFunction.class);

		// Create identity structure and array factory
		identity = mock(VkExtensionProperties.class);
		when(identity.toArray(1)).thenReturn(new VkExtensionProperties[]{identity});
	}

	@Test
	void enumerate() {
		final var support = new Support<VkExtensionProperties, String>() {
			@Override
			protected VkExtensionProperties identity() {
				return identity;
			}

			@Override
			protected String map(VkExtensionProperties struct) {
				return RESULT;
			}
		};
		final Set<String> results = support.enumerate(lib, func);
		assertEquals(Set.of(RESULT), results);
	}

	@Test
	void extensions() {
		final Extensions extensions = new Extensions() {
			@Override
			protected String map(VkExtensionProperties struct) {
				return RESULT;
			}
		};
		final Set<String> results = extensions.enumerate(lib, func);
		assertEquals(Set.of(RESULT), results);
	}
}
