package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.MockReferenceFactory;

import com.sun.jna.ptr.PointerByReference;

public class ShaderTest {
	private Shader shader;
	private LogicalDevice dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create device
		dev = mock(LogicalDevice.class);
		when(dev.library()).thenReturn(lib);

		// Create shader
		shader = Shader.create(dev, new byte[]{42});
	}

	@Test
	void constructor() {
		assertNotNull(shader.handle());
	}

	@Test
	void create() {
		// Check allocation
		final var captor = ArgumentCaptor.forClass(VkShaderModuleCreateInfo.class);
		verify(lib).vkCreateShaderModule(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check descriptor
		final var info = captor.getValue();
		assertNotNull(info);
		assertEquals(1, info.codeSize);
		assertNotNull(info.pCode);
	}
}
