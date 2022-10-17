package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Handler.*;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

public class HandlerTest {
	private Instance instance;
	private Function function;
	private Consumer<Message> consumer;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(factory.pointer()).thenReturn(new PointerByReference(new Pointer(1)));

		// Create instance
		instance = mock(Instance.class);
		when(instance.handle()).thenReturn(new Handle(2));
		when(instance.factory()).thenReturn(factory);

		// Init create/destroy API method
		function = mock(Function.class);
		when(instance.function("vkDestroyDebugUtilsMessengerEXT")).thenReturn(function);
		when(instance.function("vkCreateDebugUtilsMessengerEXT")).thenReturn(function);

		// Create message consumer
		consumer = mock(Consumer.class);
	}

	@DisplayName("A diagnostics message has a human-readable description")
	@Test
	@SuppressWarnings("static-method")
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

	@DisplayName("A handler can be destroyed")
	@Test
	void destroy() {
		// Destroy handler
		final Handler handler = new Builder(instance).build();
		handler.destroy();
		assertEquals(true, handler.isDestroyed());

		// Check API
		final Object[] args = {instance.handle().pointer(), new Pointer(1), null};
		verify(function).invoke(args);
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder(instance);
		}

		@Test
		void build() {
			// Build handler
			final Handler handler = builder
					.severity(VkDebugUtilsMessageSeverity.ERROR)
					.type(VkDebugUtilsMessageType.GENERAL)
					.consumer(consumer)
					.build();

			// Check handler
			assertNotNull(handler);
			assertEquals(false, handler.isDestroyed());

			// Init expected construction descriptor
			final var expected = new VkDebugUtilsMessengerCreateInfoEXT() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkDebugUtilsMessengerCreateInfoEXT) obj;
					assertEquals(0, info.flags);
					assertEquals(VkDebugUtilsMessageSeverity.ERROR.value(), info.messageSeverity);
					assertEquals(VkDebugUtilsMessageType.GENERAL.value(), info.messageType);
					assertNotNull(info.pfnUserCallback);
					assertNull(info.pUserData);
					return true;
				}
			};

			// Check API
			final Object[] args = {instance.handle().pointer(), expected, null, instance.factory().pointer()};
			verify(function).invokeInt(args);
		}

		@Test
		void defaults() {
			builder.build();
		}
	}
}
