package org.sarge.jove.foreign;

import java.util.Arrays;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
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

		System.out.println("Opening window...");
		final Window window = new Window.Builder()
				.title("DesktopTestTemp")
				.size(new Dimensions(1024, 768))
				.hint(Hint.CLIENT_API, 0)
				.hint(Hint.VISIBLE, 0)
				.build(desktop);

		System.out.println("Initialising Vulkan...");
		final Vulkan vulkan = Vulkan.create();

//		System.out.println("Supported validation layers...");
//		System.out.println(Arrays.toString(vulkan.layers()));

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanTest")
				.extension(DiagnosticHandler.EXTENSION)
				.extensions(extensions)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(vulkan);

		System.out.println("Attaching diagnostic handler...");
		instance.handler().build();

		System.out.println("Enumerating devices...");
		final PhysicalDevice dev = PhysicalDevice.devices(instance).toList().getFirst();

		System.out.println("Retrieving surface...");
		final Handle surface = window.surface(instance.handle());
		System.out.println("presentation=" + dev.isPresentationSupported(surface, dev.families().getFirst()));

//		final var props = dev.memory();
//		System.out.println("types="+props.memoryTypeCount);
//		System.out.println("heaps="+props.memoryHeapCount);

		System.out.println("Cleanup...");
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("Done...");
	}
}
