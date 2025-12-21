package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.MockLibrary;

class InstanceTest {
	static class MockInstanceLibrary extends MockLibrary implements Instance.Library {
		private Handle function;

		@Override
		public VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
			// Check descriptor
			assertEquals(VkStructureType.INSTANCE_CREATE_INFO, pCreateInfo.sType);
			assertEquals(0, pCreateInfo.flags);

			// Check extensions and layers
			assertEquals(1, pCreateInfo.enabledExtensionCount);
			assertEquals(1, pCreateInfo.enabledLayerCount);
			assertArrayEquals(new String[]{"extension"}, pCreateInfo.ppEnabledExtensionNames);
			assertArrayEquals(new String[]{"layer"}, pCreateInfo.ppEnabledLayerNames);

			// Check application descriptor
			final VkApplicationInfo app = pCreateInfo.pApplicationInfo;
			assertEquals(VkStructureType.APPLICATION_INFO, app.sType);
			assertEquals("name", app.pApplicationName);
			assertEquals("JOVE", app.pEngineName);
			assertEquals(new Version(1, 2, 3).toInteger(), app.applicationVersion);
			assertEquals(new Version(1, 0, 0).toInteger(), app.engineVersion);
			assertEquals(new Version(1, 1, 0).toInteger(), app.apiVersion);

			// Create instance
			init(pInstance);
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkDestroyInstance(Instance instance, Handle pAllocator) {
		}

		@Override
		public VkResult vkEnumerateInstanceExtensionProperties(String pLayerName, IntegerReference pPropertyCount, VkExtensionProperties[] pProperties) {
			pPropertyCount.set(1);
			init(pProperties, new VkExtensionProperties());
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkEnumerateInstanceLayerProperties(IntegerReference pPropertyCount, VkLayerProperties[] pProperties) {
			pPropertyCount.set(1);
			init(pProperties, new VkLayerProperties());
			return VkResult.VK_SUCCESS;
		}

		@Override
		public Handle vkGetInstanceProcAddr(Instance instance, String pName) {
			return function;
		}
	}

	private Instance instance;
	private MockInstanceLibrary library;

	@BeforeEach
	void before() {
		library = new MockInstanceLibrary();

		instance = new Instance.Builder()
        		.name("name")
        		.version(new Version(1, 2, 3))
        		.api(Vulkan.VERSION)
        		.extension("extension")
        		.layer("layer")
				.build(library);
	}


	@DisplayName("An instance can be configured and created via the builder")
	@Test
	void constructor() {
		assertFalse(instance.isDestroyed());
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
		assertTrue(instance.isDestroyed());
	}

	@DisplayName("A function pointer can be retrieved by name from the instance")
	@Test
	void function() {
		library.function = new Handle(42);
		assertEquals(Optional.of(library.function), instance.function("function"));
	}

	@DisplayName("An unknown function pointer cannot be retrieved from the instance")
	@Test
	void unknown() {
		assertEquals(Optional.empty(), instance.function("cobblers"));
	}

	@Test
	void extensions() {
		final VkExtensionProperties[] extensions = Instance.extensions(library, null);
		assertEquals(1, extensions.length);
		assertNotNull(extensions[0]);
	}

	@Test
	void layers() {
		final VkLayerProperties[] layers = Instance.layers(library);
		assertEquals(1, layers.length);
		assertNotNull(layers[0]);
	}
}
