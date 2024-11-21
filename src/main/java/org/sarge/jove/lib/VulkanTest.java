package org.sarge.jove.lib;

import java.util.Arrays;

import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

public class VulkanTest {

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

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanTest")
				.extension("VK_EXT_debug_utils")
//				.extensions(extensions)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(vulkan);

		System.out.println("Attaching diagnostic handler...");
		instance.handler().build();

		/*
		System.out.println("Layers...");
		final var count = new IntegerReference();
		vulkan.library().vkEnumerateInstanceLayerProperties(count, null);

		final var layers = new VkLayerProperties[count.value()];
//		Arrays.setAll(layers, __ -> new VkLayerProperties());
		vulkan.library().vkEnumerateInstanceLayerProperties(count, layers);
		for(var layer : layers) {
			System.out.println(layer);
		}
//		System.out.println("layers"+Arrays.toString(layers));
		*/

		System.out.println("Cleanup...");
		instance.destroy();
//		window.destroy();
		desktop.destroy();

		System.out.println("Done...");
	}
}
