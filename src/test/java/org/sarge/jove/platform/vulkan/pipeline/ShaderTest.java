package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;

class ShaderTest {
	private static final byte[] CODE = new byte[]{42};

	private static class MockShaderLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateShaderModule(LogicalDevice device, VkShaderModuleCreateInfo info, Handle pAllocator, Pointer shader) {
			assertEquals(0, info.flags);
			assertEquals(1, info.codeSize);
			assertArrayEquals(CODE, info.pCode);
			shader.set(new Handle(1));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroyShaderModule(LogicalDevice device, Shader shader, Handle pAllocator) {
		}
	}

	/**
	 *
	 * TODO
	 *
	 * can we just create an implementation like above and mix that with a generated proxy for the rest of the API?
	 * could even be partial, i.e. an abstract class with just the methods of interest implemented, others would be proxies
	 *
	 * =>
	 *
	 * reflect overall API (as we do now)
	 * create method wrappers for implementation(s)
	 * otherwise create proxy implementations with empty code and default return values
	 *
	 * Proxy.newProxyInstance(loader, new Class<?>[]{api}, handler);
	 *
	 * =>
	 *
	 * no need for nasty abstract MockVulkanLibrary
	 *
	 */

	private Shader shader;
	private LogicalDevice device;

	@BeforeEach
	void before() {
		device = new MockLogicalDevice(new MockShaderLibrary());
		shader = Shader.create(device, CODE);
	}

	@Test
	void shader() {
		assertEquals(new Handle(1), shader.handle());
	}

	@Test
	void destroy() {
		shader.destroy();
		assertEquals(true, shader.isDestroyed());
		// TODO
	}
}
