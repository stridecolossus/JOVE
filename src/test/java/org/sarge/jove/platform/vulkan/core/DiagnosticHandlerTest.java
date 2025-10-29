package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.DiagnosticHandler.*;
import org.sarge.jove.util.EnumMask;

class DiagnosticHandlerTest {

//	private Consumer<Message> listener;
	private Instance instance;
	private VulkanLibrary lib;

	public static interface MockDiagnosticHandlerLibrary extends VulkanLibrary, HandlerLibrary {
		@Override
		default Handle vkGetInstanceProcAddr(Instance instance, String pName) {
			return null;
		}

		@Override
		default VkResult vkCreateDebugUtilsMessengerEXT(Instance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, Handle pAllocator, Pointer pHandler) {
			System.out.println("create");

			assertEquals(0, pCreateInfo.flags);
			assertEquals(new EnumMask<>(VkDebugUtilsMessageType.VALIDATION), pCreateInfo.messageType);
			assertEquals(new EnumMask<>(VkDebugUtilsMessageSeverity.ERROR), pCreateInfo.messageSeverity);
			assertEquals(new Handle(3), pCreateInfo.pfnUserCallback);
			assertEquals(null, pCreateInfo.pUserData);

			pHandler.set(new Handle(4));

			return null;
		}

		@Override
		default void vkDestroyDebugUtilsMessengerEXT(Instance instance, DiagnosticHandler handler, Handle pAllocator) {
			System.out.println("destroy");
		}
	}

	@BeforeEach
	void before() {

		lib = new MockLibraryFactory(MockDiagnosticHandlerLibrary.class).proxy();

		instance = new Instance(new Handle(1), lib);

//		instance = new Instance(new Handle(1), lib) {
//			@Override
//			public Optional<Handle> function(String name) {
//				final Handle handle = switch(name) {
//					case "vkCreateDebugUtilsMessengerEXT" -> new Handle(2);
//					case "vkDestroyDebugUtilsMessengerEXT" -> new Handle(3);
//					default -> null;
//				};
//				return Optional.ofNullable(handle);
//			}
//		};
	}

	@Test
	void build() {
		final DiagnosticHandler handler = new DiagnosticHandler.Builder()
//				.consumer(listener)
				.build(instance);

		System.out.println(handler);

		handler.destroy();

	}

	///////////////

	@DisplayName("A diagnostics report can be rendered as a human-readable message")
	//@Test
	void message() {
		final var data = new VkDebugUtilsMessengerCallbackData();
		data.pMessage = "message";
		data.pMessageIdName = "name";

		final var types = Set.of(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION);
		final Message message = new Message(VkDebugUtilsMessageSeverity.WARNING, types, data);
		assertEquals("WARNING:GENERAL-VALIDATION:name:message", message.toString());
	}
}
