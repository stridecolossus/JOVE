package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.*;

public class VulkanObjectTest {

	private static class MockVulkanObject extends VulkanObject {
		public boolean released;

		MockVulkanObject() {
			super(new Handle(2), new MockLogicalDevice());
		}

		@Override
		protected Destructor<?> destructor(VulkanLibrary lib) {
			return new Destructor<>() {
				@Override
				public void destroy(LogicalDevice device, VulkanObject object, Handle allocator) {
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

	@Test
	void hash() {
		final Handle handle = new Handle(2);
		assertEquals(handle.hashCode(), obj.hashCode());
	}

	@Test
	void equals() {
		assertEquals(obj, obj);
		assertNotEquals(obj, null);
		assertNotEquals(obj, new MockVulkanObject());
	}
}
