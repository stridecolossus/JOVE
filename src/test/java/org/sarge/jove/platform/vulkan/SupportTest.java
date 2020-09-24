package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.Support.Extensions;

import com.sun.jna.ptr.IntByReference;

public class SupportTest {
	private static final String RESULT = "result";

	private Vulkan vulkan;
	private VulkanFunction<VkExtensionProperties> func;
	private IntByReference count;
	private VkExtensionProperties identity;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		// Create Vulkan
		vulkan = mock(Vulkan.class);

		// Init counter
		count = new IntByReference(1);
		when(vulkan.integer()).thenReturn(count);

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
		final Set<String> results = support.enumerate(vulkan, func);
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
		final Set<String> results = extensions.enumerate(vulkan, func);
		assertEquals(Set.of(RESULT), results);
	}
}
