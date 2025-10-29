package org.sarge.jove.foreign;

import java.util.Arrays;
import java.util.logging.LogManager;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

public class VulkanIntegrationDemo {

	boolean stop = true;

	void main() throws Exception {
		System.out.println("Initialising logging...");
		try(final var config = VulkanIntegrationDemo.class.getResourceAsStream("/logging.properties")) {
			LogManager.getLogManager().readConfiguration(config);
		}

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
		final VulkanLibrary vulkan = VulkanLibrary.create();

//		System.out.println("Supported validation layers...");
//		System.out.println(Arrays.toString(vulkan.layers()));

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanIntegrationDemo")
				.extension(DiagnosticHandler.EXTENSION)
//				.extension("VK_KHR_surface")
//				.extension("VK_KHR_win32_surface")
				.extensions(extensions)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(vulkan);

		System.out.println("Attaching diagnostic handler...");
		final DiagnosticHandler handler = new DiagnosticHandler.Builder().build(instance);

		/*
		System.out.println("Enumerating devices...");
		final PhysicalDevice physical = PhysicalDevice.enumerate(instance).toList().getFirst();

		// TODO
		System.out.println("families...");
		for(var family : physical.families()) {
			System.out.println(family);
		}
//System.out.println("features=" + physical.features().features());
*/
		/*

		System.out.println("Retrieving surface...");
		final Handle surface = window.surface(instance.handle());
		System.out.println("presentation=" + physical.isPresentationSupported(surface, physical.families().getFirst()));
		// TODO - do we need to destroy the surface?! [yes]

//		System.out.println("Retrieving device memory properties...");
//		final var memory = physical.memory();
//		System.out.println("types=" + memory.memoryTypeCount);
//		System.out.println("heaps=" + memory.memoryHeapCount);

		System.out.println("Creating logical device...");
		final var dev = new LogicalDevice.Builder(physical)
				.queue(new RequiredQueue(physical.families().getFirst()))
				.build();

TODO - does this NEED to be done here as well?
        .layer(ValidationLayer.STANDARD_VALIDATION)

		System.out.println("queues=" + dev.queues().values());

		*/

		System.out.println("Cleanup...");
		// TODO - surface
//		dev.destroy();
		handler.destroy();
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("Done...");
	}
}
