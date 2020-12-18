package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.INTEGRATION_TEST;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverityFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.Instance.Message;
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
	class MessageTests {
		private Message message;
		private Collection<VkDebugUtilsMessageTypeFlagEXT> types;

		@BeforeEach
		void before() {
			final var data = new VkDebugUtilsMessengerCallbackDataEXT();
			data.pMessage = "message";
			data.pMessageIdName = "name";
			types = List.of(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT, VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT);
			message = new Message(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT, types, data);
		}

		@Test
		void constructor() {
			assertEquals(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT, message.severity());
			assertEquals(types, message.types());
			assertNotNull(message.data());
		}

		@Test
		void toStringSeverity() {
			assertEquals("INFO", Message.toString(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT));
		}

		@Test
		void toStringType() {
			assertEquals("VALIDATION", Message.toString(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT));
		}

		@Test
		void build() {
			assertEquals("INFO:VALIDATION-GENERAL:name:message", message.toString());
		}

		@Test
		void writer() {
			final var out = new StringWriter();
			final Consumer<Message> handler = Message.writer(new PrintWriter(out));
			assertNotNull(handler);
			handler.accept(message);
			assertEquals(message.toString(), out.toString().trim());
		}
	}

	@Nested
	class HandlerTests {
		private Instance.Handler handler;
		private Function func;

		@BeforeEach
		void before() {
			// Create debug extension method
			func = mock(Function.class);

			// Partially mock the instance to register the extension methods
			final Instance spy = Mockito.spy(instance);
			doReturn(func).when(spy).function("vkCreateDebugUtilsMessengerEXT");
			doReturn(func).when(spy).function("vkDestroyDebugUtilsMessengerEXT");

			// Create message handler builder
			handler = spy.handler();
		}

		@Test
		void builder() {
			assertNotNull(handler);
		}

		@Test
		void build() {
			// Attach handler with default settings
			handler.init().attach();

			// Check API
			final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
			verify(func).invokeInt(captor.capture());

			// Check arguments (note that we cannot compare arrays directly as JNA structures are not equal)
			final Object[] args = captor.getValue();
			assertEquals(4, args.length);
			assertEquals(ref.getValue(), args[0]);
			assertNotNull(args[1]);
			assertEquals(null, args[2]);
			assertEquals(lib.factory().pointer(), args[3]);

			// Check descriptor
			final VkDebugUtilsMessengerCreateInfoEXT info = (VkDebugUtilsMessengerCreateInfoEXT) args[1];
			assertEquals(0, info.flags);
			assertEquals(4352, info.messageSeverity);
			assertEquals(3, info.messageType);
			assertNotNull(info.pfnUserCallback);
			assertEquals(null, info.pUserData);
		}

		@Test
		void buildEmptyMessageSeverity() {
			handler.type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT);
			assertThrows(IllegalArgumentException.class, () -> handler.attach());
		}

		@Test
		void buildEmptyMessageTypes() {
			handler.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
			assertThrows(IllegalArgumentException.class, () -> handler.attach());
		}

		// TODO - this is very awkward to test
		@Disabled("TODO")
		@Test
		void destroy() {
			handler.init().attach();
			instance.destroy();
			final Object[] args = new Object[]{ref.getValue(), lib.factory().pointer().getValue(), null};
			verify(func).invoke(args);
		}
	}
}
