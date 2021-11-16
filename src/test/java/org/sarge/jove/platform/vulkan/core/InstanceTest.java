package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.core.Instance.Builder;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class InstanceTest {
	private static final PointerByReference POINTER = new PointerByReference(new Pointer(1));

	private VulkanLibrary lib;
	private Instance instance;

	@BeforeEach
	void before() {
		// Create Vulkan API
		lib = mock(VulkanLibrary.class);

		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(factory.pointer()).thenReturn(POINTER);

		// Create instance
		instance = new Instance(new Handle(1), lib, factory);
	}

	@Test
	void constructor() {
		assertEquals(lib, instance.library());
		assertEquals(new Handle(1), instance.handle());
		assertNotNull(instance.factory());
		assertEquals(false, instance.isDestroyed());
	}

	@Test
	void destroy() {
		instance.destroy();
		verify(lib).vkDestroyInstance(instance.handle(), null);
	}

	@Test
	void function() {
		final Pointer func = new Pointer(2);
		final String name = "name";
		when(lib.vkGetInstanceProcAddr(instance, name)).thenReturn(func);
		assertEquals(func, instance.function(name));
	}

	@Test
	void functionUnknown() {
		assertThrows(RuntimeException.class, () -> instance.function("cobblers"));
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			// Init instance properties
			final Version ver = new Version(1, 2, 3);
			final ValidationLayer layer = new ValidationLayer("layer");

			// Create instance
			instance = builder
				.name("name")
				.version(ver)
				.extension("ext")
				.layer(layer)
				.build(lib);

			// Check instance
			assertNotNull(instance);
			assertEquals(lib, instance.library());
			assertEquals(false, instance.isDestroyed());

			// Check API
			final ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
			verify(lib).vkCreateInstance(captor.capture(), isNull(), isA(PointerByReference.class));

			// Check instance descriptor
			final VkInstanceCreateInfo info = captor.getValue();
			assertEquals(1, info.enabledExtensionCount);
			assertEquals(1, info.enabledLayerCount);
			assertNotNull(info.ppEnabledExtensionNames);
			assertNotNull(info.ppEnabledLayerNames);

			// Check application descriptor
			final VkApplicationInfo app = info.pApplicationInfo;
			assertNotNull(app);
			assertEquals("name", app.pApplicationName);
			assertEquals(ver.toInteger(), app.applicationVersion);
			assertEquals("JOVE", app.pEngineName);
			assertEquals(new Version(1, 0, 0).toInteger(), app.engineVersion);
			assertEquals(VulkanLibrary.VERSION.toInteger(), app.apiVersion);
		}

		@Test
		void buildDefaults() {
			// Create instance with default properties
			instance = builder.build(lib);

			// Check instance
			assertNotNull(instance);
			assertEquals(lib, instance.library());
			assertEquals(false, instance.isDestroyed());

			// Check API
			final ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
			verify(lib).vkCreateInstance(captor.capture(), isNull(), isA(PointerByReference.class));

			// Check instance descriptor
			final VkInstanceCreateInfo info = captor.getValue();
			assertEquals(0, info.enabledExtensionCount);
			assertEquals(0, info.enabledLayerCount);
			assertNotNull(info.ppEnabledExtensionNames);
			assertNotNull(info.ppEnabledLayerNames);

			// Check application descriptor
			final VkApplicationInfo app = info.pApplicationInfo;
			assertNotNull(app);
			assertEquals("Unspecified", app.pApplicationName);
			assertEquals(new Version(1, 0, 0).toInteger(), app.applicationVersion);
			assertEquals("JOVE", app.pEngineName);
			assertEquals(new Version(1, 0, 0).toInteger(), app.engineVersion);
			assertEquals(VulkanLibrary.VERSION.toInteger(), app.apiVersion);
		}
	}
}
