package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.Support.Extensions;

import com.sun.jna.ptr.IntByReference;

public class SupportTest {
	private VulkanFunction<VkExtensionProperties> func;
	private IntByReference count;
	private VkExtensionProperties identity;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		count = new IntByReference(1);
		identity = new VkExtensionProperties();
		func = mock(VulkanFunction.class);
	}

	@Test
	void enumerate() {
		// Create support adapter that enumerates extension names
		final Support<VkExtensionProperties, String> support = new Support<>() {
			@Override
			public Set<String> enumerate(VulkanFunction<VkExtensionProperties> func) {
				throw new UnsupportedOperationException();
			}

			@Override
			protected String map(VkExtensionProperties obj) {
				return "string";
			}
		};

		// Enumerate extensions and check results
		final Set<String> results = support.enumerate(func, count, identity);
		assertEquals(Set.of("string"), results);
		verify(func).enumerate(count, identity);
	}

	@Test
	void extensions() {
		final Extensions extensions = new Extensions();
		final Set<String> results = extensions.enumerate(func, count, identity);
		assertNotNull(results);
		assertEquals(1, results.size());
		verify(func).enumerate(count, identity);
	}
}
