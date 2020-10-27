package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
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
import org.sarge.jove.common.NativeObject.Handle;
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
	private PointerByReference ref;

	@BeforeEach
	void before() {
		// Init API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Note instance handle
		ref = lib.factory().pointer();

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
		assertEquals(new Handle(ref.getValue()), instance.handle());
	}

	@Test
	void create() {
		// Check API invocation
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
		assertNotNull(app.applicationVersion);
		assertEquals("JOVE", app.pEngineName);
		assertNotNull(app.engineVersion);
		assertEquals(VulkanLibrary.VERSION.toInteger(), app.apiVersion);
	}

	@Test
	void destroy() {
		instance.destroy();
		verify(lib).vkDestroyInstance(ref.getValue(), null);
	}

	@Test
	void function() {
		final Pointer func = new Pointer(2);
		final String name = "name";
		when(lib.vkGetInstanceProcAddr(ref.getValue(), name)).thenReturn(func);
		assertEquals(func, instance.function(name));
	}

	@Test
	void functionUnknown() {
		assertThrows(RuntimeException.class, () -> instance.function("cobblers"));
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

			// Mock the create/destroy function
			func = mock(Function.class);

			// Partially mock the instance so we can mock the extension functions
			instance = Mockito.spy(instance);
			doReturn(func).when(instance).function("vkCreateDebugUtilsMessengerEXT");
			doReturn(func).when(instance).function("vkDestroyDebugUtilsMessengerEXT");
		}

		@Test
		void add() {
			// Add handler
			when(func.invokeInt(isA(Object[].class))).thenReturn(VulkanLibrary.SUCCESS);
			instance.handlers().add(handler);

			// Check API
			final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
			verify(func).invokeInt(captor.capture());

			// Check arguments (note that we cannot compare arrays directly as JNA structures are not equal)
			final Object[] args = captor.getValue();
			assertEquals(4, args.length);
			assertEquals(ref.getValue(), args[0]);
			assertTrue(handler.create().dataEquals((VkDebugUtilsMessengerCreateInfoEXT) args[1]));
			assertEquals(null, args[2]);
			assertEquals(lib.factory().pointer(), args[3]);
		}

		@Test
		void alreadyAdded() {
			instance.handlers().add(handler);
			assertThrows(IllegalArgumentException.class, () -> instance.handlers().add(handler));
		}

		@Test
		void remove() {
			// Add a handler
			instance.handlers().add(handler);

			// Remove handler
			final Pointer handle = lib.factory().pointer().getValue();
			instance.handlers().remove(handler);

			// Check handler is destroyed
			final Object[] args = new Object[]{ref.getValue(), handle, null};
			verify(func).invoke(args);
		}

		@Test
		void removeNotPresent() {
			assertThrows(IllegalArgumentException.class, () -> instance.handlers().remove(handler));
		}

		@Test
		void destroy() {
			instance.handlers().add(handler);
			instance.destroy();
		}
	}
}
