package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverityFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.sarge.jove.platform.vulkan.core.Message.HandlerBuilder;

@SuppressWarnings("static-method")
public class MessageTest {
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
		final Consumer<Message> handler = msg -> out.append(msg.toString());
		assertNotNull(handler);
		handler.accept(message);
		assertEquals(message.toString(), out.toString().trim());
	}

	@Nested
	class HandlerBuilderTests {
		private HandlerBuilder builder;

		@BeforeEach
		void before() {
			builder = new HandlerBuilder();
		}

		@SuppressWarnings("unchecked")
		@Test
		void build() {
			// Build handler descriptor
			final var handler = builder
					.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
					.type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT)
					.consumer(mock(Consumer.class))
					.build();

			// Verify handler
			assertNotNull(handler);
			assertEquals(0, handler.flags);
			assertEquals(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT.value(), handler.messageSeverity);
			assertEquals(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT.value(), handler.messageType);
			assertNotNull(handler.pfnUserCallback);
			assertEquals(null, handler.pUserData);
		}

		@Test
		void init() {
			final var handler = builder.init().build();
			assertEquals(256 | 4096, handler.messageSeverity);
			assertEquals(1 | 2, handler.messageType);
		}

		@Test
		void buildEmptySeverities() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyTypes() {
			builder.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void create() {
			final var expected = new HandlerBuilder().init().build();
			assertTrue(expected.dataEquals(HandlerBuilder.create()));
		}
	}
}
