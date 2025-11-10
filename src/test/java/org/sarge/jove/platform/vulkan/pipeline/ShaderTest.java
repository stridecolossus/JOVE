package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;

class ShaderTest {
	private static class MockShaderLibrary extends MockVulkanLibrary {
		private boolean destroyed;

		@Override
		public VkResult vkCreateShaderModule(LogicalDevice device, VkShaderModuleCreateInfo info, Handle pAllocator, Pointer shader) {
			assertNotNull(device);
			assertEquals(42L, info.codeSize);
			assertArrayEquals(new byte[42], info.pCode);
			shader.set(new Handle(2));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroyShaderModule(LogicalDevice device, Shader shader, Handle pAllocator) {
		}
	}

	private Shader shader;
	private LogicalDevice device;
	private MockShaderLibrary library;

	@BeforeEach
	void before() {
		library = new MockShaderLibrary();
		device = new MockLogicalDevice(library);
		shader = Shader.create(device, new byte[42]);
	}

	@Test
	void destroy() {
		shader.destroy();
		assertEquals(true, shader.isDestroyed());
	}
}
