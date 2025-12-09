package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;

class InstanceTest {
	private Instance instance;
	private MockInstanceLibrary library;

	@BeforeEach
	void before() {
		library = new MockInstanceLibrary();
		instance = new Instance(new Handle(1), library);
	}

	public // TODO
	static class MockInstanceLibrary implements Instance.Library {
		private final Map<String, Handle> functions = new HashMap<>();
		boolean destroyed;

		@Override
		public VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
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
			pInstance.set(MemorySegment.ofAddress(2));
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkDestroyInstance(Instance instance, Handle pAllocator) {
			destroyed = true;
		}

		@Override
		public VkResult vkEnumerateInstanceExtensionProperties(String pLayerName, IntegerReference pPropertyCount, VkExtensionProperties[] pProperties) {
			if(pProperties == null) {
				pPropertyCount.set(1);
			}
			else {
				pProperties[0] = new VkExtensionProperties();
			}
			return VkResult.VK_SUCCESS;
		}

		@Override
		public VkResult vkEnumerateInstanceLayerProperties(IntegerReference pPropertyCount, VkLayerProperties[] pProperties) {
			if(pProperties == null) {
				pPropertyCount.set(1);
			}
			else {
				pProperties[0] = new VkLayerProperties();
			}
			return VkResult.VK_SUCCESS;
		}

		@Override
		public Handle vkGetInstanceProcAddr(Instance instance, String pName) {
			return functions.get(pName);
		}

		/**
		 * Adds a function pointer.
		 */
		public void function(String name, Handle handle) {
			functions.put(name, handle);
		}
	}

	@DisplayName("An instance can be configured and created via the builder")
	@Test
	void build() {
		final Instance instance = new Instance.Builder()
        		.name("name")
        		.version(new Version(1, 2, 3))
        		.api(Vulkan.VERSION)
        		.extension("extension")
        		.layer("layer")
				.build(library);

		assertEquals(new Handle(2), instance.handle());
		assertEquals(false, instance.isDestroyed());
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
		assertEquals(true, library.destroyed);
	}

	@DisplayName("A function pointer can be retrieved by name from the instance")
	@Test
	void function() {
		final var handle = new Handle(3);
		library.function("function", handle);
		assertEquals(Optional.of(handle), instance.function("function"));
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
