package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class InstanceTest {
	private VulkanLibrary lib;
	private Instance instance;
	private Handle handle;

	@BeforeEach
	void before() {
		// Init API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));

		final Pointer ptr = new Pointer(1);
		when(lib.factory().pointer()).thenReturn(new PointerByReference(ptr));
		handle = new Handle(ptr);

		// Create instance
		instance = new Instance.Builder()
				.vulkan(lib)
				.name("test")
				.extension("ext")
				.layer(new ValidationLayer("layer"))
				.build();
	}

	@Test
	void constructor() {
		assertNotNull(instance);
		assertEquals(lib, instance.library());
		assertEquals(handle, instance.handle());
	}

	@Test
	void create() {
		// Check API
		final ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
		verify(lib).vkCreateInstance(captor.capture(), isNull(), isA(PointerByReference.class));

		// Check instance descriptor
		final VkInstanceCreateInfo info = captor.getValue();
		assertEquals(1, info.enabledExtensionCount);
		assertEquals(1, info.enabledLayerCount);
		assertNotNull(info.ppEnabledExtensionNames);
		assertNotNull(info.ppEnabledLayerNames);

		// Check application descriptor
		final VkApplicationInfo app = info.pApplicationInfo;
		assertNotNull(app);
		assertEquals("test", app.pApplicationName);
		assertNotNull(app.applicationVersion);
		assertEquals("JOVE", app.pEngineName);
		assertNotNull(app.engineVersion);
		assertEquals(VulkanLibrary.VERSION.toInteger(), app.apiVersion);
	}

	@Test
	void destroy() {
		instance.destroy();
		verify(lib).vkDestroyInstance(handle, null);
	}

	@Test
	void function() {
		final Pointer func = new Pointer(2);
		final String name = "name";
		when(lib.vkGetInstanceProcAddr(handle, name)).thenReturn(func);
		assertEquals(func, instance.function(name));
	}

	@Test
	void functionUnknown() {
		assertThrows(RuntimeException.class, () -> instance.function("cobblers"));
	}

	@Test
	void attach() {
		// Init create handler extension function
		final Function func = mock(Function.class);
		when(lib.vkGetInstanceProcAddr(handle, "vkCreateDebugUtilsMessengerEXT")).thenReturn(func);

		// Attach a handler
		final VkDebugUtilsMessengerCreateInfoEXT handler = new VkDebugUtilsMessengerCreateInfoEXT();
		instance.attach(handler);
		verify(func).invokeInt(new Object[]{handle.toPointer(), handler, null, lib.factory().pointer()});

		// Destroy instance and check handler is also destroyed
		when(lib.vkGetInstanceProcAddr(handle, "vkDestroyDebugUtilsMessengerEXT")).thenReturn(func);
		instance.destroy();
		verify(func).invoke(new Object[]{handle.toPointer(), lib.factory().pointer().getValue(), null});
	}

	@Tag(AbstractVulkanTest.INTEGRATION_TEST)
	@Test
	void build() {
		// Create real API
		lib = VulkanLibrary.create();

		// Create instance
		instance = new Instance.Builder()
				.vulkan(lib)
				.name("test")
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();

		// Check instance
		assertNotNull(instance);
		assertNotNull(instance.handle());
		assertEquals(lib, instance.library());

		// Destroy instance
		instance.destroy();
	}
}
