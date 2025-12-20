package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverityFlagsEXT.*;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagsEXT.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.DiagnosticHandler.*;
import org.sarge.jove.util.*;

class DiagnosticHandlerTest {
	private class MockHandlerLibrary extends MockLibrary implements HandlerLibrary {
		@Override
		public VkResult vkCreateDebugUtilsMessengerEXT(Instance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, Handle pAllocator, Pointer pHandler) {
			assertEquals(VkStructureType.DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT, pCreateInfo.sType);
			assertEquals(0, pCreateInfo.flags);
			assertEquals(new EnumMask<>(GENERAL_EXT, VALIDATION_EXT), pCreateInfo.messageType);
			assertEquals(new EnumMask<>(WARNING_EXT, ERROR_EXT), pCreateInfo.messageSeverity);
			assertNotNull(pCreateInfo.pfnUserCallback);
			assertEquals(null, pCreateInfo.pUserData);
			assertEquals(DiagnosticHandlerTest.this.instance, instance);
			init(pHandler);
			return VkResult.VK_SUCCESS;
		}

		@Override
		public void vkDestroyDebugUtilsMessengerEXT(Instance instance, DiagnosticHandler handler, Handle pAllocator) {
			assertEquals(DiagnosticHandlerTest.this.instance, instance);
		}
	}

	private DiagnosticHandler handler;
	private Instance instance;
	private MockHandlerLibrary library;

	@BeforeEach
	void before() {
		library = new MockHandlerLibrary();
		instance = new MockInstance();
		handler = new DiagnosticHandler(new Handle(2), instance, library);
	}

	@Test
	void create() {
		final var builder = new DiagnosticHandler.Builder() {
			@Override
			protected HandlerLibrary library(Instance instance, Registry registry) {
				return new MockHandlerLibrary();
			}
		};

		builder.build(instance, DefaultRegistry.create());
	}

	@DisplayName("A diagnostics report can be rendered as a human-readable message")
	@Test
	void message() {
		final var data = new VkDebugUtilsMessengerCallbackDataEXT();
		data.pMessage = "message";
		data.pMessageIdName = "name";

		final var types = Set.of(VkDebugUtilsMessageTypeFlagsEXT.GENERAL_EXT, VkDebugUtilsMessageTypeFlagsEXT.VALIDATION_EXT);
		final Message message = new Message(VkDebugUtilsMessageSeverityFlagsEXT.WARNING_EXT, types, data);
		assertEquals("WARNING:GENERAL-VALIDATION:name:message", message.toString());
	}

	@Test
	void destroy() {
		handler.destroy();
		assertTrue(handler.isDestroyed());
	}
}
