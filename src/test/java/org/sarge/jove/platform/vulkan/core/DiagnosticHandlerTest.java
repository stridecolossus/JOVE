package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverity.*;
import static org.sarge.jove.platform.vulkan.VkDebugUtilsMessageType.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.DiagnosticHandler.*;
import org.sarge.jove.platform.vulkan.core.InstanceTest.MockInstanceLibrary;
import org.sarge.jove.util.EnumMask;

class DiagnosticHandlerTest {
	private class MockHandlerLibrary implements HandlerLibrary {
		@Override
		public VkResult vkCreateDebugUtilsMessengerEXT(Instance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, Handle pAllocator, Pointer pHandler) {
			// Check create descriptor
			assertEquals(0, pCreateInfo.flags);
			assertEquals(new EnumMask<>(GENERAL, VALIDATION), pCreateInfo.messageType);
			assertEquals(new EnumMask<>(WARNING, ERROR), pCreateInfo.messageSeverity);
			assertNotNull(pCreateInfo.pfnUserCallback);
			assertEquals(null, pCreateInfo.pUserData);

			// Check parameters
			assertEquals(DiagnosticHandlerTest.this.instance, instance);
			assertEquals(null, pAllocator);

			// Init handler
			pHandler.set(new Handle(2));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroyDebugUtilsMessengerEXT(Instance instance, DiagnosticHandler handler, Handle pAllocator) {
			assertEquals(DiagnosticHandlerTest.this.instance, instance);
			destroyed = true;
		}
	}

	private DiagnosticHandler handler;
	private Instance instance;
	private HandlerLibrary lib;
	private boolean destroyed;

	@BeforeEach
	void before() {
		lib = new MockHandlerLibrary();
		instance = new Instance(new Handle(1), new MockInstanceLibrary());
		handler = new DiagnosticHandler(new Handle(2), instance, lib);
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
		final var data = new VkDebugUtilsMessengerCallbackData();
		data.pMessage = "message";
		data.pMessageIdName = "name";

		final var types = Set.of(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION);
		final Message message = new Message(VkDebugUtilsMessageSeverity.WARNING, types, data);
		assertEquals("WARNING:GENERAL-VALIDATION:name:message", message.toString());
	}

	@Test
	void destroy() {
		handler.destroy();
		assertEquals(true, handler.isDestroyed());
		assertEquals(true, destroyed);
	}
}
