package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.MessageHandler.MessageCallback;

import com.sun.jna.Pointer;

public class MessageHandlerTest {
	private MessageCallback callback;

	@BeforeEach
	void before() {
		callback = mock(MessageCallback.class);
	}

	@Test
	void builder() {
		final MessageHandler handler = new MessageHandler.Builder()
				.callback(callback)
				.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
				.type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT)
				.build();
		assertNotNull(handler);
	}

	@Test
	void create() {
		// Create a handler with various properties
		final Pointer data = mock(Pointer.class);
		final MessageHandler handler = new MessageHandler.Builder()
				.callback(callback)
				.data(data)
				.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
				.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT)
				.type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT)
				.type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
				.build();

		// Check creation descriptor
		final var info = handler.create();
		assertNotNull(info);
		assertEquals(4112, info.messageSeverity);
		assertEquals(5, info.messageType);
		assertEquals(data, info.pUserData);
		assertEquals(callback, info.pfnUserCallback);
	}

	@Test
	void console() {
		// Create callback
		final StringWriter out = new StringWriter();
		callback = MessageHandler.writer(new PrintWriter(out));

		// Create message descriptor
		final var msg = new VkDebugUtilsMessengerCallbackDataEXT();
		msg.pMessageIdName = "name";
		msg.pMessage = "message";

		// Invoke callback
		final int severity = VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT.value();
		final int type = VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT.value() | VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT.value();
		callback.message(severity, type, msg, null);

		// Check formatted message
		assertEquals("ERROR:GENERAL-PERFORMANCE:name:message", out.toString().trim());
	}
}
