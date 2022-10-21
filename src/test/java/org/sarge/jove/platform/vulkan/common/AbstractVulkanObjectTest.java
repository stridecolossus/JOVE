package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject.Destructor;
import org.sarge.jove.platform.vulkan.core.*;

public class AbstractVulkanObjectTest {
	private AbstractVulkanObject obj;
	private DeviceContext dev;
	private Destructor<AbstractVulkanObject> destructor;
	private Handle handle;
	private boolean released;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		// Create object handle
		handle = new Handle(1);

		// Create context
		dev = mock(LogicalDevice.class);
		when(dev.handle()).thenReturn(new Handle(2));

		// Create destructor
		destructor = mock(Destructor.class);
		released = false;

		// Create object
		obj = new AbstractVulkanObject(handle, dev) {
			@Override
			protected Destructor<AbstractVulkanObject> destructor(VulkanLibrary lib) {
				return destructor;
			}

			@Override
			protected void release() {
				assertFalse(released);
				released = true;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(handle, obj.handle());
		assertEquals(dev, obj.device());
		assertEquals(destructor, obj.destructor(null));
		assertEquals(false, obj.isDestroyed());
	}

	@Test
	void destroy() {
		obj.destroy();
		assertEquals(true, obj.isDestroyed());
		verify(destructor).destroy(dev, obj, null);
		assertTrue(released);
	}

	@Test
	void hash() {
		assertEquals(1, obj.hashCode());
	}

	@Test
	void equals() {
		assertEquals(true, obj.equals(obj));
		assertEquals(false, obj.equals(null));
		assertEquals(false, obj.equals(mock(AbstractVulkanObject.class)));
	}
}
