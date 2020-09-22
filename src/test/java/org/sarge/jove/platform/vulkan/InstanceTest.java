package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sarge.jove.platform.Service.ServiceException;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class InstanceTest {
	private Vulkan vulkan;
	private VulkanLibrary api;
	private PointerByReference ref;
	private Instance instance;

	@BeforeEach
	void before() {
		// Init Vulkan
		vulkan = mock(Vulkan.class);

		// Init API
		api = mock(VulkanLibrary.class);
		when(vulkan.api()).thenReturn(api);

		// Init pointer factory
		ref = new PointerByReference(new Pointer(42));
		when(vulkan.pointer()).thenReturn(ref);

		// Create instance
		instance = new Instance.Builder(vulkan)
				.name("test")
				.version(VulkanLibrary.VERSION)
				.extension("ext")
				.layer(new ValidationLayer("layer"))
				.build();
	}

	@Test
	void constructor() {
		assertNotNull(instance);
		assertEquals(ref.getValue(), instance.handle());
		assertEquals(vulkan, instance.vulkan());
	}

	@Test
	void create() {
		// Check API invocation
		final ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
		verify(api).vkCreateInstance(captor.capture(), isNull(), eq(ref));

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
		verify(api).vkDestroyInstance(instance.handle(), null);
	}

	@Test
	void function() {
		final Pointer func = new Pointer(2);
		final String name = "name";
		when(api.vkGetInstanceProcAddr(instance.handle(), name)).thenReturn(func);
		assertEquals(func, instance.function(name));
	}

	@Test
	void functionUnknown() {
		assertThrows(ServiceException.class, () -> instance.function("cobblers"));
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
			assertEquals(ref, args[3]);
		}

		@Test
		void alreadyAdded() {
			instance.add(handler);
			assertThrows(IllegalArgumentException.class, () -> instance.add(handler));
		}

		@Test
		void remove() {
			instance.add(handler);
			instance.remove(handler);
			verify(func).invoke(new Object[]{instance.handle(), ref.getValue(), null});
		}

		@Test
		void removeNotPresent() {
			assertThrows(IllegalArgumentException.class, () -> instance.remove(handler));
		}

		@Test
		void destroy() {
			instance.add(handler);
			instance.destroy();
			verify(func).invoke(new Object[]{instance.handle(), ref.getValue(), null});
		}
	}
}
