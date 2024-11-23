package org.sarge.jove.foreign;

import java.util.Arrays;

import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

public class VulkanIntegrationDemo {

	public static void main(String[] args) {
		System.out.println("Initialising GLFW...");
		final var desktop = Desktop.create();

		System.out.println("version=" + desktop.version());
		System.out.println("Vulkan=" + desktop.isVulkanSupported());

		final String[] extensions = desktop.extensions();
		System.out.println("extensions=" + Arrays.toString(extensions));

//		System.out.println("Opening window...");
//		final Window window = new Window.Builder()
//				.title("DesktopTestTemp")
//				.size(new Dimensions(1024, 768))
//				.hint(Hint.CLIENT_API, 0)
//				.build(desktop);

		System.out.println("Initialising Vulkan...");
		final Vulkan vulkan = Vulkan.create();

		System.out.println("Supported validation layers...");
		System.out.println(Arrays.toString(vulkan.layers()));

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanTest")
				.extension("VK_EXT_debug_utils")
//				.extensions(extensions)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(vulkan);

		System.out.println("Attaching diagnostic handler...");
		instance.handler().build();

//		///////////////////////

//		final var count = instance.vulkan().factory().integer();
//		Vulkan.check(instance.vulkan().library().vkEnumerateInstanceLayerProperties(count, null));
//		System.out.println("layers="+count.value());
//
//		final var layers = new VkLayerProperties[count.value()];
////		Arrays.setAll(layers, n -> new VkLayerProperties());
//		Vulkan.check(instance.vulkan().library().vkEnumerateInstanceLayerProperties(count, layers));
//		System.out.println(Arrays.toString(layers));
//
//		// TODO - allocate 'empty' structures? or framework allocates on unmarshal? [JNA previously probably empty] => NULL avoids pointless marshalling to native?
//
//		///////////////////////

		System.out.println("Enumerating devices...");
		final PhysicalDevice dev = PhysicalDevice.devices(instance).toList().getFirst();

		final var props = dev.memory();
		System.out.println("types="+props.memoryTypeCount);
		System.out.println("heaps="+props.memoryHeapCount);

		///////////////////////

		/*
//		final Handle bodge = new Handle(Arena.ofAuto().allocate(ValueLayout.ADDRESS));
		final var count = dev.instance().vulkan().factory().integer();
		instance.vulkan().library().vkGetPhysicalDeviceQueueFamilyProperties(dev.handle(), count, null);
		System.out.println("queue.props.count="+count.value());

		final VkQueueFamilyProperties[] array = new VkQueueFamilyProperties[count.value()];
		instance.vulkan().library().vkGetPhysicalDeviceQueueFamilyProperties(dev.handle(), count, array);

//		final MemorySegment address = context.allocator().allocate(layout, array.length);
		*/

		///////////////////////

		System.out.println("Cleanup...");
		instance.destroy();
//		window.destroy();
		desktop.destroy();

		System.out.println("Done...");
	}
}
