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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverity;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageType;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackData;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.core.Instance.Builder;
import org.sarge.jove.platform.vulkan.core.Instance.Handler;
import org.sarge.jove.platform.vulkan.core.Instance.Message;
import org.sarge.jove.platform.vulkan.core.Instance.MessageCallback;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Function;
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
			handler = instance.handler();
			func = mock(Function.class);
			when(lib.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT")).thenReturn(func);
		}

		@Test
		void constructor() {
			assertNotNull(handler);
		}

		@Test
		void attach() {
			// Attach handler
			handler.init().attach();

			// Check API
			final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
			verify(func).invokeInt(captor.capture());

			// Check arguments
			final Object[] args = captor.getValue();
			assertNotNull(args);
			assertEquals(4, args.length);
			assertEquals(instance.handle().toPointer(), args[0]);
			assertEquals(null, args[2]);
			assertEquals(POINTER, args[3]);

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
			handler.init().attach();

			// Destroy instance and handler
			when(lib.vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT")).thenReturn(func);
			instance.destroy();

			// Check API
			final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
			verify(func).invoke(captor.capture());

			// Check arguments
			final Object[] args = captor.getValue();
			assertNotNull(args);
			assertEquals(3, args.length);
			assertEquals(instance.handle().toPointer(), args[0]);
			assertEquals(POINTER, args[1]);
			assertEquals(null, args[2]);
		}

		@Test
		void attachEmptySeverities() {
			handler.type(VkDebugUtilsMessageType.GENERAL);
			assertThrows(IllegalArgumentException.class, () -> handler.attach());
		}

		@Test
		void attachEmptyTypes() {
			handler.severity(VkDebugUtilsMessageSeverity.WARNING);
			assertThrows(IllegalArgumentException.class, () -> handler.attach());
		}
	}
}
