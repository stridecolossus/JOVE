package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.DiagnosticHandler.*;
import org.sarge.jove.util.*;

import com.sun.jna.*;

public class DiagnosticHandlerTest {
	private DiagnosticHandler handler;
	private Instance instance;
	private VulkanLibrary lib;
	private Function function;
	private Consumer<Message> consumer;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		lib = mock(VulkanLibrary.class);
		consumer = mock(Consumer.class);
		function = mock(Function.class);
		instance = new Instance(new Handle(1), lib, new MockReferenceFactory()) {
			@Override
			public Handle function(String name) {
				return function;
			}
		};
		handler = new DiagnosticHandler(new Handle(2), instance);
	}

	@Test
	void constructor() {
		assertEquals(false, handler.isDestroyed());
	}

	@DisplayName("A handler can be destroyed")
	@Test
	void destroy() {
		handler.destroy();
		assertEquals(true, handler.isDestroyed());
		final Object[] args = {instance, handler, null};
		final var options = Map.of(Library.OPTION_TYPE_MAPPER, VulkanLibrary.MAPPER);
		verify(function).invoke(Void.class, args, options);
	}

	@Nested
	class MessageTests {
    	@DisplayName("A diagnostics message has a human-readable description")
    	@Test
    	void message() {
    		// Init callback
    		final var data = new VkDebugUtilsMessengerCallbackData();
    		data.pMessage = "message";
    		data.pMessageIdName = "name";

    		// Check message wrapper
    		final var types = Set.of(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION);
    		final Message message = new Message(VkDebugUtilsMessageSeverity.WARNING, types, data);
    		assertEquals("WARNING:GENERAL-VALIDATION:name:message", message.toString());
    	}

    	@DisplayName("The diagnostics callback delegates a message to the configured consumer")
    	@Test
    	void callback() {
    		final var data = new VkDebugUtilsMessengerCallbackData();
    		final Message expected = new Message(VkDebugUtilsMessageSeverity.WARNING, Set.of(VkDebugUtilsMessageType.PERFORMANCE), data);
    		final MessageCallback callback = new MessageCallback(consumer);
    		callback.message(VkDebugUtilsMessageSeverity.WARNING.value(), VkDebugUtilsMessageType.PERFORMANCE.value(), data, null);
    		verify(consumer).accept(expected);
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
			// Init expected construction descriptor
			final var expected = new VkDebugUtilsMessengerCreateInfoEXT() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkDebugUtilsMessengerCreateInfoEXT) obj;
					assertEquals(0, info.flags);
					assertEquals(EnumMask.of(VkDebugUtilsMessageSeverity.ERROR), info.messageSeverity);
					assertEquals(EnumMask.of(VkDebugUtilsMessageType.GENERAL), info.messageType);
					assertNotNull(info.pfnUserCallback);
					assertNull(info.pUserData);
					return true;
				}
			};

			when(lib.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT")).thenReturn(new Pointer(42));

			// Init API
			final Object[] args = {instance, expected, null, instance.factory().pointer()};
			final var options = Map.of(Library.OPTION_TYPE_MAPPER, VulkanLibrary.MAPPER);
			when(function.invoke(Integer.class, args, options)).thenReturn(0);

			// Build handler
			final DiagnosticHandler handler = builder
					.severity(VkDebugUtilsMessageSeverity.ERROR)
					.type(VkDebugUtilsMessageType.GENERAL)
					.consumer(consumer)
					.build(instance);

			// Check handler
			assertEquals(false, handler.isDestroyed());
		}
	}
}
