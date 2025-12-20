package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.*;

class ShaderTest {
	@SuppressWarnings("unused")
	static class MockShaderLibrary extends MockLibrary {
		public VkResult vkCreateShaderModule(LogicalDevice device, VkShaderModuleCreateInfo info, Handle pAllocator, Pointer shader) {
			assertEquals(VkStructureType.SHADER_MODULE_CREATE_INFO, info.sType);
			assertEquals(42L, info.codeSize);
			assertArrayEquals(new byte[42], info.pCode);
			init(shader);
			return VkResult.VK_SUCCESS;
		}
	}

	private Shader shader;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(new MockShaderLibrary(), Shader.Library.class);
		final var device = new MockLogicalDevice(mockery.proxy());
		shader = Shader.create(device, new byte[42]);
	}

	@Test
	void create() {
		assertFalse(shader.isDestroyed());
	}

	@Test
	void destroy() {
		shader.destroy();
		assertTrue(shader.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyShaderModule").count());
	}
}
