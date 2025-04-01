package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.DiagnosticHandler.Message;

class DiagnosticHandlerTest {
	@DisplayName("A diagnostics report can be rendered as a human-readable message")
	@Test
	void message() {
		final var data = new VkDebugUtilsMessengerCallbackData();
		data.pMessage = "message";
		data.pMessageIdName = "name";

		final var types = Set.of(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION);
		final Message message = new Message(VkDebugUtilsMessageSeverity.WARNING, types, data);
		assertEquals("WARNING:GENERAL-VALIDATION:name:message", message.toString());
	}
}
