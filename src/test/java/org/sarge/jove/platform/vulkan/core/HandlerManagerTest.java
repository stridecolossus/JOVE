package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverity;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageType;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackData;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.core.HandlerManager.Builder;
import org.sarge.jove.platform.vulkan.core.HandlerManager.Handler;
import org.sarge.jove.platform.vulkan.core.HandlerManager.Message;
import org.sarge.jove.platform.vulkan.core.HandlerManager.MessageCallback;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class HandlerManagerTest {
	private static final List<VkDebugUtilsMessageType> TYPES = List.of(VkDebugUtilsMessageType.GENERAL, VkDebugUtilsMessageType.VALIDATION);
	private static final List<VkDebugUtilsMessageSeverity> SEVERITY = List.of(VkDebugUtilsMessageSeverity.ERROR, VkDebugUtilsMessageSeverity.WARNING);

	private HandlerManager manager;
	private Instance instance;
	private Function func;

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
		func = mock(Function.class);
		when(instance.function(anyString())).thenReturn(func);

		// Create manager
		manager = new HandlerManager(instance);
	}

	@Test
	void constructor() {
		assertNotNull(manager.handlers());
		assertEquals(0, manager.handlers().count());
	}

	private Handler attach() {
		return manager.builder().init().build();
	}

	@Test
	void close() {
		final Handler handler = attach();
		manager.close();
		assertEquals(true, handler.isDestroyed());
		assertEquals(0, manager.handlers().count());
	}

	@Nested
	class HandlerTests {
		private Handler handler;

		@BeforeEach
		void before() {
			handler = attach();
		}

		@Test
		void constructor() {
			// Check handler
			assertNotNull(handler);
			assertEquals(false, handler.isDestroyed());
			assertArrayEquals(new Handler[]{handler}, manager.handlers().toArray());

			// Init expected create descriptor
			final var info = new VkDebugUtilsMessengerCreateInfoEXT() {
				@Override
				public boolean equals(Object obj) {
					final var that = (VkDebugUtilsMessengerCreateInfoEXT) obj;
					assertEquals(0, that.flags);
					assertEquals(IntegerEnumeration.mask(SEVERITY) , that.messageSeverity);
					assertEquals(IntegerEnumeration.mask(TYPES), that.messageType);
					assertNotNull(that.pfnUserCallback);
					assertEquals(null, that.pUserData);
					return true;
				}
			};

			// Check API
			final Pointer parent = instance.handle().toPointer();
			final PointerByReference ref = instance.factory().pointer();
			final Object[] args = {parent, info, null, ref};
			verify(func).invokeInt(args);
		}

		@Test
		void destroy() {
			// Destroy handler
			handler.destroy();
			assertEquals(true, handler.isDestroyed());
			assertEquals(0, manager.handlers().count());

			// Check API
			final Object[] args = {instance.handle().toPointer(), handler.handle().toPointer(), null};
			verify(func).invoke(args);
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = manager.builder();
		}

		@Test
		void constructor() {
			assertNotNull(builder);
		}

		@Test
		void build() {
			final Handler handler = builder.init().build();
			assertNotNull(handler);
		}

		@Test
		void buildEmptySeverity() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildEmptyTypes() {
			builder.severity(VkDebugUtilsMessageSeverity.ERROR);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}

	@Nested
	class MessageTests {
		private Message message;
		private VkDebugUtilsMessengerCallbackData data;

		@BeforeEach
		void before() {
			// Create callback data
			data = new VkDebugUtilsMessengerCallbackData();
			data.pMessage = "message";
			data.pMessageIdName = "name";

			// Create message wrapper
			message = new Message(VkDebugUtilsMessageSeverity.WARNING, TYPES, data);
		}

		@Test
		void constructor() {
			assertEquals(VkDebugUtilsMessageSeverity.WARNING, message.severity());
			assertEquals(TYPES, message.types());
			assertEquals(data, message.data());
		}

		@Test
		void string() {
			assertEquals("WARNING:GENERAL-VALIDATION:name:message", message.toString());
		}
	}

	@Nested
	class CallbackTests {
		private MessageCallback callback;
		private Consumer<Message> consumer;

		@BeforeEach
		void before() {
			consumer = mock(Consumer.class);
			callback = new MessageCallback(consumer);
		}

		@Test
		void callback() {
			final var data = new VkDebugUtilsMessengerCallbackData();
			callback.message(VkDebugUtilsMessageSeverity.WARNING.value(), VkDebugUtilsMessageType.PERFORMANCE.value(), data, null);
			verify(consumer).accept(new Message(VkDebugUtilsMessageSeverity.WARNING, Set.of(VkDebugUtilsMessageType.PERFORMANCE), data));
		}
	}
}
