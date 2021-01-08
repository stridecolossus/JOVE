package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.ptr.IntByReference;

public class SupportedTest extends AbstractVulkanTest {
	private Supported supported;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void before() {
		// Init extensions
		final VulkanFunction<VkExtensionProperties> extensionsFunction = mock(VulkanFunction.class);
		final Answer<Integer> extensionsAnswer = inv -> {
			final VkExtensionProperties ext = inv.getArgument(2);
			ext.extensionName = "ext".getBytes();
			return VulkanLibrary.SUCCESS;
		};
		when(extensionsFunction.enumerate(eq(lib), isA(IntByReference.class), isA(VkExtensionProperties.class))).thenAnswer(extensionsAnswer);

		// Init layers
		final VulkanFunction<VkLayerProperties> layersFunction = mock(VulkanFunction.class);
		final Answer<Integer> layersAnswer = inv -> {
			final VkLayerProperties layer = inv.getArgument(2);
			layer.layerName = "layer".getBytes();
			layer.implementationVersion = 2;
			return VulkanLibrary.SUCCESS;
		};
		when(layersFunction.enumerate(eq(lib), isA(IntByReference.class), isA(VkLayerProperties.class))).thenAnswer(layersAnswer);

		// Create supported wrapper
		supported = new Supported(lib, extensionsFunction, layersFunction);
	}

	@Test
	void extensions() {
		assertEquals(Set.of("ext"), supported.extensions());
	}

	@Test
	void layers() {
		assertEquals(Set.of(new ValidationLayer("layer", 2)), supported.layers());
	}
}
