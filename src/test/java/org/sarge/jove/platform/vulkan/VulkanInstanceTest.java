package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.Feature.Extension;
import org.sarge.jove.platform.vulkan.Feature.FeatureSet;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.Feature.ValidationLayer;
import org.sarge.jove.platform.vulkan.VulkanHandle.Destructor;
import org.sarge.jove.platform.vulkan.VulkanInstance.MessageCallback;
import org.sarge.jove.platform.vulkan.VulkanInstance.MessageHandlerFactory;
import org.sarge.jove.platform.vulkan.VulkanLibrary.Version;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class VulkanInstanceTest extends AbstractVulkanTest {
	private VulkanInstance instance;
	private Pointer handle;
	private Vulkan vulkan;

	@BeforeEach
	public void before() {
		vulkan = Vulkan.instance();
		handle = mock(Pointer.class);
		instance = new VulkanInstance(new VulkanHandle(handle, Destructor.NULL), vulkan);
	}

	@Test
	public void devices() {
		when(library.vkEnumeratePhysicalDevices(handle, factory.integer(), factory.pointers(1))).thenReturn(0);
		assertEquals(Arrays.asList(factory.pointers(1)), instance.devices());
	}

	@Test
	public void handleFactory() {
		when(library.vkGetInstanceProcAddr(handle, "vkCreateDebugUtilsMessengerEXT")).thenReturn(new Pointer(1));
		when(library.vkGetInstanceProcAddr(handle, "vkDestroyDebugUtilsMessengerEXT")).thenReturn(new Pointer(2));
		assertNotNull(instance.handlerFactory());
	}

	@Test
	public void destroy() {
		instance.destroy();
	}

	@Nested
	class BuilderTests {
		private VulkanInstance.Builder builder;

		@BeforeEach
		public void before() {
			final FeatureSet<Extension> extensions = new FeatureSet<>(Set.of(new Extension("ext")));
			final FeatureSet<ValidationLayer> layers = new FeatureSet<>(Set.of(new ValidationLayer("layer", 2)));
			final Supported supported = new Supported(extensions, layers);
			when(vulkan.supported()).thenReturn(supported);
			builder = new VulkanInstance.Builder(vulkan);
		}

		@Test
		public void build() {
			// Build instance
			instance = builder
				.name("name")
				.version(new Version(1, 2, 3))
				.extension("ext")
				.layer("layer", 1)
				.build();

			// Check instance
			assertNotNull(instance);
		}

		@Test
		public void buildFailed() {
			when(library.vkCreateInstance(any(VkInstanceCreateInfo.class), isNull(), any(PointerByReference.class))).thenReturn(999);
			assertThrows(ServiceException.class, () -> builder.build());
		}

		@Test
		public void extensionNotSupported() {
			assertThrows(ServiceException.class, () -> builder.extension("missing").build());
		}

		@Test
		public void layerNotSupported() {
			assertThrows(ServiceException.class, () -> builder.layer("layer", 3).build());
		}
	}

	@Nested
	class FactoryTests {
		private MessageHandlerFactory handlerFactory;
		private Function func;

		@BeforeEach
		public void before() {
			func = mock(Function.class);
			handlerFactory = instance.new MessageHandlerFactory(func, func);
		}

		@Test
		public void create() {
			final Handle handler = handlerFactory.builder()
				.callback(MessageCallback.CONSOLE)
				.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
				.type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT)
				.build();
			assertNotNull(handler);
		}

		@Test
		public void destroy() {
			final Handle handler = handlerFactory.builder().init().build();
			handler.destroy();
			assertEquals(true, handler.isDestroyed());
		}

		@Test
		public void destroyHandlers() {
			final Handle handler = handlerFactory.builder().init().build();
			handlerFactory.destroyHandlers();
			assertEquals(true, handler.isDestroyed());
		}
	}

	@Nested
	class DebugCallbackHelperTests {
		@Test
		public void severity() {
			assertEquals("ERROR", MessageCallback.toString(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT));
			assertEquals("WARN", MessageCallback.toString(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT));
			assertEquals("INFO", MessageCallback.toString(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT));
			assertEquals("VERBOSE", MessageCallback.toString(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT));
			assertEquals(String.valueOf(Integer.MAX_VALUE), MessageCallback.toString(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_FLAG_BITS_MAX_ENUM_EXT));
		}

		@Test
		public void type() {
			assertEquals("GENERAL", MessageCallback.toString(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT));
			assertEquals("VALIDATION", MessageCallback.toString(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT));
			assertEquals("PERFORMANCE", MessageCallback.toString(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT));
			assertEquals(String.valueOf(Integer.MAX_VALUE), MessageCallback.toString(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_FLAG_BITS_MAX_ENUM_EXT));
		}

		@Test
		public void typesMask() {
			final int types = IntegerEnumeration.mask(Set.of(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT, VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT));
			final String[] tokens = MessageCallback.toString(types).split("-");
			assertNotNull(tokens);
			assertEquals(Set.of("VALIDATION", "GENERAL"), Set.of(tokens));
		}
	}
}
