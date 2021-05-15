package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverityFlagEXT.ERROR;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverityFlagEXT.INFO;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagEXT.GENERAL;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagEXT.VALIDATION;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.sarge.jove.platform.vulkan.core.Message.HandlerBuilder;

public class MessageTest {
	private Message message;
	private Collection<VkDebugUtilsMessageTypeFlagEXT> types;

	@BeforeEach
	void before() {
		final var data = new VkDebugUtilsMessengerCallbackDataEXT();
		data.pMessage = "message";
		data.pMessageIdName = "name";
		types = List.of(VALIDATION, GENERAL);
		message = new Message(INFO, types, data);
	}

	@Test
	void constructor() {
		assertEquals(INFO, message.severity());
		assertEquals(types, message.types());
		assertNotNull(message.data());
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
					.severity(ERROR)
					.type(GENERAL)
					.consumer(mock(Consumer.class))
					.build();

			// Verify handler
			assertNotNull(handler);
			assertEquals(0, handler.flags);
			assertEquals(ERROR.value(), handler.messageSeverity);
			assertEquals(GENERAL.value(), handler.messageType);
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
			builder.severity(ERROR);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void create() {
			final var expected = new HandlerBuilder().init().build();
			assertTrue(expected.dataEquals(HandlerBuilder.create()));
		}
	}
}
