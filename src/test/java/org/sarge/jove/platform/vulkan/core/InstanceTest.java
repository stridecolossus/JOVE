package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.core.Instance.Builder;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

class InstanceTest {
	private Instance instance;
	private Vulkan vulkan;

	@BeforeEach
	void before() {
		vulkan = new MockVulkan();
		instance = new Instance(new Handle(1), vulkan);
	}

	@Test
	void constructor() {
//		assertEquals(lib, instance.library());
//		assertNotNull(instance.factory());
		assertEquals(new Handle(1), instance.handle());
		assertEquals(false, instance.isDestroyed());
	}

	@DisplayName("An instance can be destroyed")
	@Test
	void destroy() {
		instance.destroy();
		verify(vulkan.library()).vkDestroyInstance(instance, null);
	}

	@Nested
	class FunctionPointerTests {
		@BeforeEach
		void before() {
    		final var address = MemorySegment.ofAddress(2);
    		when(vulkan.library().vkGetInstanceProcAddr(instance, "function")).thenReturn(new Handle(address));
		}

    	@DisplayName("A function pointer can be looked up from the instance")
    	@Test
    	void function() {
    		assertNotNull(instance.function("function"));
    	}

    	@DisplayName("An unknown function pointer cannot be looked up from the instance")
    	@Test
    	void unknown() {
    		assertThrows(IllegalArgumentException.class, () -> instance.function("cobblers"));
    	}
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
				.api(new Version(1, 0, 1))
				.extension("ext")
				.layer(layer)
				.build(vulkan);

			// Check instance
//			assertEquals(lib, instance.library());
			assertEquals(false, instance.isDestroyed());

			// Check API
			final var expected = new VkInstanceCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check instance descriptor
					final var info = (VkInstanceCreateInfo) obj;
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
					assertEquals(new Version(1, 0, 1).toInteger(), app.apiVersion);

					return true;
				}
			};
			verify(vulkan.library()).vkCreateInstance(expected, null, vulkan.factory().pointer());
		}

		@Test
		void api() {
			assertThrows(IllegalStateException.class, () -> builder.api(new Version(9, 0, 0)));
		}
	}
}
