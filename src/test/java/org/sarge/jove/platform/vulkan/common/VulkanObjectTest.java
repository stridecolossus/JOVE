package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;

public class VulkanObjectTest {
	private static class MockVulkanObject extends VulkanObject {
		public boolean released;

		MockVulkanObject() {
			super(new Handle(2), new MockLogicalDevice());
		}

		@Override
		protected Destructor<?> destructor() {
			return new Destructor<>() {
				@Override
				public void destroy(DeviceContext device, VulkanObject object, Handle allocator) {
					final MockVulkanObject instance = MockVulkanObject.this;
					assertEquals(instance.device(), device);
					assertEquals(instance, object);
					assertEquals(null, allocator);
				}
			};
		}

		@Override
		protected void release() {
			released = true;
		}
	}

	private MockVulkanObject obj;

	@BeforeEach
	void before() {
		obj = new MockVulkanObject();
	}

	@Test
	void constructor() {
		assertEquals(new Handle(2), obj.handle());
		assertEquals(false, obj.isDestroyed());
		assertEquals(false, obj.released);
	}

	@Test
	void destroy() {
		obj.destroy();
		assertEquals(true, obj.isDestroyed());
		assertEquals(true, obj.released);
	}
}
