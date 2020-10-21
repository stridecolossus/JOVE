package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.INTEGRATION_TEST;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.MockReferenceFactory;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class InstanceTest {
	private VulkanLibrary lib;
	private Instance instance;

	@BeforeEach
	void before() {
		// Init API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create instance
		instance = new Instance.Builder()
				.vulkan(lib)
				.name("test")
				.extension("ext")
				.layer(new ValidationLayer("layer"))
				.build();
	}

	@Test
	void constructor() {
		assertNotNull(instance);
		assertEquals(lib, instance.library());
		assertEquals(lib.factory().pointer().getValue(), instance.handle());
	}

	@Test
	void create() {
		// Check API invocation
		final PointerByReference ref = lib.factory().pointer();
		final ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
		verify(lib).vkCreateInstance(captor.capture(), isNull(), eq(ref));

		// Check instance descriptor
		final VkInstanceCreateInfo info = captor.getValue();
		assertEquals(1, info.enabledExtensionCount);
		assertEquals(1, info.enabledLayerCount);
		assertNotNull(info.ppEnabledExtensionNames);
		assertNotNull(info.ppEnabledLayerNames);

		// Check application descriptor
		final VkApplicationInfo app = info.pApplicationInfo;
		assertNotNull(app);
		assertEquals("test", app.pApplicationName);
		assertEquals("JOVE", app.pEngineName);
		assertEquals(VulkanLibrary.VERSION.toInteger(), app.apiVersion);
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
		when(lib.vkGetInstanceProcAddr(instance.handle(), name)).thenReturn(func);
		assertEquals(func, instance.function(name));
	}

	@Test
	void functionUnknown() {
		assertThrows(ServiceException.class, () -> instance.function("cobblers"));
	}

	@Tag(INTEGRATION_TEST)
	@Test
	void build() {
		// Create real API
		lib = VulkanLibrary.create();

		// Create instance
		instance = new Instance.Builder()
				.vulkan(lib)
				.name("test")
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();

		// Check instance
		assertNotNull(instance);
		assertNotNull(instance.handle());
		assertEquals(lib, instance.library());

		// Destroy instance
		instance.destroy();
	}

	@Nested
	class HandlerTests {
		private MessageHandler handler;
		private Function func;

		@BeforeEach
		void before() {
			// Create a handler
			handler = new MessageHandler.Builder().init().build();

			// Partially mock the instance so we can mock the function pointer method (which is impossible otherwise due to how the JNA function is created from the underlying pointer)
			instance = Mockito.spy(instance);
			func = mock(Function.class);
			doReturn(func).when(instance).function("vkCreateDebugUtilsMessengerEXT");
			doReturn(func).when(instance).function("vkDestroyDebugUtilsMessengerEXT");
		}

		@Test
		void add() {
			// Add handler
			instance.add(handler);

			// Check API
			final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
			verify(func).invokeInt(captor.capture());

			// Check arguments (note that we cannot compare arrays directly as JNA structures are not equal)
			final Object[] args = captor.getValue();
			assertEquals(4, args.length);
			assertEquals(instance.handle(), args[0]);
			assertTrue(handler.create().dataEquals((VkDebugUtilsMessengerCreateInfoEXT) args[1]));
			assertEquals(null, args[2]);
			assertEquals(lib.factory().pointer(), args[3]);
		}

		@Test
		void alreadyAdded() {
			instance.add(handler);
			assertThrows(IllegalArgumentException.class, () -> instance.add(handler));
		}

		@Test
		void remove() {
			final Pointer handle = instance.add(handler);
			instance.remove(handler);
			verify(func).invoke(new Object[]{instance.handle(), handle, null});
		}

		@Test
		void removeNotPresent() {
			assertThrows(IllegalArgumentException.class, () -> instance.remove(handler));
		}

		@Test
		void destroy() {
			final Pointer handle = instance.add(handler);
			instance.destroy();
			verify(func).invoke(new Object[]{instance.handle(), handle, null});
		}
	}
}
