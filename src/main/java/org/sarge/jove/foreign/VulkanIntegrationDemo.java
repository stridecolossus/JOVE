package org.sarge.jove.foreign;

import java.util.Arrays;
import java.util.logging.LogManager;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

public class VulkanIntegrationDemo {

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
		final NativeLibrary vulkan = Vulkan.create();

//		System.out.println("Supported validation layers...");
//		System.out.println(Arrays.toString(vulkan.layers()));

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanIntegrationDemo")
				.extension(DiagnosticHandler.EXTENSION)
				.extensions(extensions)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(vulkan.get());

		System.out.println("Attaching diagnostic handler...");
		final DiagnosticHandler handler = new DiagnosticHandler.Builder().build(instance, DefaultRegistry.create());			// TODO - registry -> library?

		System.out.println("Enumerating devices...");
		final PhysicalDevice physical = PhysicalDevice
				.enumerate(vulkan.get(), instance)
				.toList()
				.getFirst();

		// TODO
		System.out.println("families...");
		for(var family : physical.families()) {
			System.out.println(family);
		}
		//System.out.println("features=" + physical.features().supported());

/*
TODO - arrays!!!
		final var props = physical.properties();
		System.out.println("id="+props.deviceID);
		System.out.println("name="+new String(props.deviceName));
		System.out.println("type="+props.deviceType);
		System.out.println("api="+props.apiVersion);
		System.out.println("driver="+props.driverVersion);
		System.out.println("vendor="+props.vendorID);
		System.out.println("cache="+props.pipelineCacheUUID.length);
*/

		System.out.println("Creating surface...");
		final Handle handle = window.surface(instance.handle());
		final var surface = new VulkanSurface(handle, instance.handle(), physical, vulkan.get());
		System.out.println("presentation=" + surface.isPresentationSupported(physical.families().getFirst()));

		System.out.println("Retrieving device memory properties...");
		final var memory = physical.memory();
		System.out.println("heaps="+Arrays.toString(memory.memoryHeaps));
		System.out.println("types="+Arrays.toString(memory.memoryTypes));

		System.out.println("Creating logical device...");
		final var dev = new LogicalDevice.Builder(physical)
				.queue(new RequiredQueue(physical.families().getFirst()))
				.build(vulkan.get());

// TODO - does this NEED to be done here as well?
//        .layer(ValidationLayer.STANDARD_VALIDATION)
		System.out.println("queues=" + dev.queues().values());

		System.out.println("Cleanup...");
		dev.destroy();
		surface.destroy();
		handler.destroy();
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("Done...");
	}
}
