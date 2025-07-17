package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.MockLibraryFactory;
import org.sarge.jove.foreign.MockLibraryFactory.MockedMethod;
import org.sarge.jove.foreign.NativeReference.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

class InstanceTest {
	/*
	private static class MockInstanceLibrary extends MockVulkanLibrary {
		public boolean destroyed;

		@Override
		public VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
			// Check instance descriptor
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
			pInstance.set(new Handle(1));

			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroyInstance(Instance instance, Handle pAllocator) {
			destroyed = true;
		}

		@Override
		public Handle vkGetInstanceProcAddr(Instance instance, String pName) {
			if(pName.equals("function")) {
				return new Handle(2);
			}
			else {
				return null;
			}
		}
	}
	*/

	private Instance instance;
	private MockLibraryFactory factory;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		factory = new MockLibraryFactory(VulkanLibrary.class);
		lib = factory.proxy();
		instance = new Instance(new Handle(1), lib);
	}

	public abstract class MockCreateInstance implements VulkanLibrary { // Instance.Library {
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
			pInstance.set(new Handle(2));
			return null;
		}
	}

	@Test
	void build() {

//		abstract class MockCreateInstance implements Instance.Library {
//			@Override
//			public VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
//				// Check create descriptor
//				assertEquals(1, pCreateInfo.enabledExtensionCount);
//				assertEquals(1, pCreateInfo.enabledLayerCount);
//				assertArrayEquals(new String[]{"extension"}, pCreateInfo.ppEnabledExtensionNames);
//				assertArrayEquals(new String[]{"layer"}, pCreateInfo.ppEnabledLayerNames);
//
//				// Check application descriptor
//				final VkApplicationInfo app = pCreateInfo.pApplicationInfo;
//				assertEquals("name", app.pApplicationName);
//				assertEquals("JOVE", app.pEngineName);
//				assertEquals(new Version(1, 2, 3).toInteger(), app.applicationVersion);
//				assertEquals(new Version(1, 0, 0).toInteger(), app.engineVersion);
//				assertEquals(new Version(1, 1, 0).toInteger(), app.apiVersion);
//
//				// Create instance
//				pInstance.set(new Handle(2));
//				return null;
//			}
//		}

		// Mock create instance API
		final MockedMethod create = factory.get("vkCreateInstance").implement(lib, MockCreateInstance.class);

		// Configure and build an instance
		final Instance instance = new Instance.Builder()
        		.name("name")
        		.version(new Version(1, 2, 3))
        		.api(VulkanLibrary.VERSION)
        		.extension("extension")
        		.layer(new ValidationLayer("layer"))
				.build(lib);

		// Check instance
		assertEquals(new Handle(2), instance.handle());
		assertEquals(false, instance.isDestroyed());
		assertEquals(1, create.count());

	}

	@Test
	void api() {
		final var builder = new Instance.Builder();
		assertThrows(IllegalArgumentException.class, () -> builder.api(new Version(9, 0, 0)));
	}

	@Test
	void destroy() {
		instance.destroy();
		assertEquals(true, instance.isDestroyed());
		assertEquals(1, factory.get("vkDestroyInstance").count());
	}

	@Test
	void function() {
		final Handle handle = new Handle(3);
		factory.get("vkGetInstanceProcAddr").returns(handle);
		assertEquals(Optional.of(handle), instance.function("function"));
	}

	@Test
	void unknown() {
		assertEquals(Optional.empty(), instance.function("cobblers"));
	}
}
