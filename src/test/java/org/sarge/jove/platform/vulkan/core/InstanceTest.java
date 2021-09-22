package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverity;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageType;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackData;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.Instance.Handler;
import org.sarge.jove.platform.vulkan.core.Instance.Handler.Message;
import org.sarge.jove.platform.vulkan.core.Instance.Handler.MessageCallback;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class InstanceTest {
	private static final Pointer POINTER = new Pointer(1);

	private VulkanLibrary lib;
	private Instance instance;
	private Handle handle;

	@BeforeEach
	void before() {
		// Init API
		lib = mock(VulkanLibrary.class);

		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
		when(lib.factory().pointer()).thenReturn(new PointerByReference(POINTER));
		handle = new Handle(POINTER);

		// Create instance
		instance = new Instance.Builder()
				.extension("ext")
				.layer(new ValidationLayer("layer"))
				.build(lib);
	}

	@Test
	void constructor() {
		assertNotNull(instance);
		assertEquals(lib, instance.library());
		assertEquals(handle, instance.handle());
	}

	@Test
	void create() {
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
		assertEquals("Unspecified", app.pApplicationName);
		assertNotNull(app.applicationVersion);
		assertEquals("JOVE", app.pEngineName);
		assertNotNull(app.engineVersion);
		assertEquals(VulkanLibrary.VERSION.toInteger(), app.apiVersion);
	}

	@Test
	void destroy() {
		instance.destroy();
		verify(lib).vkDestroyInstance(handle, null);
	}

	@Test
	void function() {
		final Pointer func = new Pointer(2);
		final String name = "name";
		when(lib.vkGetInstanceProcAddr(handle, name)).thenReturn(func);
		assertEquals(func, instance.function(name));
	}

	@Test
	void functionUnknown() {
		assertThrows(RuntimeException.class, () -> instance.function("cobblers"));
	}

	@Nested
	class MessageTests {
		@Test
		void string() {
			// Create message data
			final var data = new VkDebugUtilsMessengerCallbackData();
			data.pMessage = "message";
			data.pMessageIdName = "name";

			// Check message
			final Message msg = new Message(VkDebugUtilsMessageSeverity.WARNING, List.of(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION), data);
			assertEquals("WARNING:GENERAL-VALIDATION:name:message", msg.toString());
		}

		@Test
		void callback() {
			// Create callback
			final Consumer<Message> consumer = mock(Consumer.class);
			final MessageCallback callback = new MessageCallback(consumer);

			// Init VK message
			final var data = new VkDebugUtilsMessengerCallbackData();
			data.pMessage = "message";
			data.pMessageIdName = "name";

			// Invoke callback with message
			final var severity = VkDebugUtilsMessageSeverity.WARNING;
			final var types = List.of(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION);
			callback.message(severity.value(), IntegerEnumeration.mask(types), data, null);

			// Check message wrapper is created and passed to the handler
			final ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
			verify(consumer).accept(captor.capture());

			// Check message wrapper
			final Message msg = captor.getValue();
			assertEquals(severity, msg.severity());
			assertEquals(new TreeSet<>(types), msg.types());
			assertEquals(data, msg.data());
		}
	}

	@Nested
	class HandlerTests {
		private Handler handler;
		private Function func;

		@BeforeEach
		void before() {
			handler = new Handler();
			func = mock(Function.class);
			when(lib.vkGetInstanceProcAddr(handle, "vkCreateDebugUtilsMessengerEXT")).thenReturn(func);
		}

		@Test
		void constructor() {
			assertNotNull(handler);
		}

		@Test
		void attach() {
			// Attach handler
			handler.init().attach(instance);

			// Check API
			final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
			verify(func).invokeInt(captor.capture());

			// Check arguments
			final Object[] args = captor.getValue();
			assertNotNull(args);
			assertEquals(4, args.length);
			assertEquals(POINTER, args[0]);
			assertEquals(null, args[2]);
			assertEquals(lib.factory().pointer(), args[3]);

			// Check handler descriptor
			final var info = (VkDebugUtilsMessengerCreateInfoEXT) args[1];
			assertEquals(0, info.flags);
			assertNotNull(info.pfnUserCallback);
			assertEquals(null, info.pUserData);
			assertEquals(IntegerEnumeration.mask(VkDebugUtilsMessageSeverity.WARNING, VkDebugUtilsMessageSeverity.ERROR), info.messageSeverity);
			assertEquals(IntegerEnumeration.mask(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION), info.messageType);
		}

		@Test
		void destroy() {
			// Attach handler
			handler.init().attach(instance);

			// Destroy instance and handler
			when(lib.vkGetInstanceProcAddr(handle, "vkDestroyDebugUtilsMessengerEXT")).thenReturn(func);
			instance.destroy();

			// Check API
			final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
			verify(func).invoke(captor.capture());

			// Check arguments
			final Object[] args = captor.getValue();
			assertNotNull(args);
			assertEquals(3, args.length);
			assertEquals(POINTER, args[0]);
			assertEquals(lib.factory().pointer().getValue(), args[1]);
			assertEquals(null, args[2]);
		}

		@Test
		void attachEmptySeverities() {
			handler.type(VkDebugUtilsMessageType.GENERAL);
			assertThrows(IllegalArgumentException.class, () -> handler.attach(instance));
		}

		@Test
		void attachEmptyTypes() {
			handler.severity(VkDebugUtilsMessageSeverity.WARNING);
			assertThrows(IllegalArgumentException.class, () -> handler.attach(instance));
		}
	}

	@Tag(AbstractVulkanTest.INTEGRATION_TEST)
	@Test
	void build() {
		// Create real API
		lib = VulkanLibrary.create();

		// Create instance
		instance = new Instance.Builder()
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(lib);

		// Check instance
		assertNotNull(instance);
		assertNotNull(instance.handle());
		assertEquals(lib, instance.library());

		// Destroy instance
		instance.destroy();
	}
}
