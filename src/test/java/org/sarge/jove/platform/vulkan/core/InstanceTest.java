package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

class InstanceTest {
	private Instance instance;
	private MockLibraryFactory factory;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		factory = new MockLibraryFactory(MockCreateInstance.class);
		lib = factory.proxy();
		instance = new Instance(new Handle(1), lib);
	}

	public static interface MockCreateInstance extends VulkanLibrary {
		@Override
		default VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
			// Check create descriptor
			assertEquals(1, pCreateInfo.enabledExtensionCount);
			assertEquals(1, pCreateInfo.enabledLayerCount);
			assertArrayEquals(new String[]{"extension"}, pCreateInfo.ppEnabledExtensionNames);
			assertArrayEquals(new String[]{"layer"}, pCreateInfo.ppEnabledLayerNames);

			// Check application descriptor
			final VkApplicationInfo app = pCreateInfo.pApplicationInfo;
			assertEquals("name", app.pApplicationName);
			assertEquals("JOVE", app.pEngineName);
			assertEquals(new Version(1, 2, 3).toInteger(), app.applicationVersion);
			assertEquals(new Version(1, 0, 0).toInteger(), app.engineVersion);
			assertEquals(new Version(1, 1, 0).toInteger(), app.apiVersion);

			// Create instance
			pInstance.set(new Handle(2));
			return null;
		}
	}

	@DisplayName("An instance can be configured and created via the builder")
	@Test
	void build() {
		final Instance instance = new Instance.Builder()
        		.name("name")
        		.version(new Version(1, 2, 3))
        		.api(VulkanLibrary.VERSION)
        		.extension("extension")
        		.layer(new ValidationLayer("layer"))
				.build(lib);

		assertEquals(new Handle(2), instance.handle());
		assertEquals(false, instance.isDestroyed());
		assertEquals(1, factory.get("vkCreateInstance").count());
	}

	@DisplayName("The required API version must be supported by the native library")
	@Test
	void api() {
		final var builder = new Instance.Builder();
		assertThrows(IllegalArgumentException.class, () -> builder.api(new Version(9, 0, 0)));
	}

	@DisplayName("An instance can be destroyed")
	@Test
	void destroy() {
		instance.destroy();
		assertEquals(true, instance.isDestroyed());
		assertEquals(1, factory.get("vkDestroyInstance").count());
	}

	@DisplayName("A function pointer can be retrieved by name from the instance")
	@Test
	void function() {
		final Handle handle = new Handle(3);
		factory.get("vkGetInstanceProcAddr").returns(handle);
		assertEquals(Optional.of(handle), instance.function("function"));
	}

	@DisplayName("An unknown function pointer cannot be retrieved from the instance")
	@Test
	void unknown() {
		assertEquals(Optional.empty(), instance.function("cobblers"));
	}
}
