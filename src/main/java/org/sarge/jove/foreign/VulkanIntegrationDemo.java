package org.sarge.jove.foreign;

import java.lang.foreign.MemorySegment;
import java.util.logging.LogManager;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.DeviceEnumerationHelper;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.util.IntEnum.ReverseMapping;

public class VulkanIntegrationDemo {

//	void main() throws Exception {
//		System.out.println("Initialising logging...");
//		try(final var config = VulkanIntegrationDemo.class.getResourceAsStream("/logging.properties")) {
//			LogManager.getLogManager().readConfiguration(config);
//		}
//
//		System.out.println("Initialising GLFW...");
//		final var desktop = Desktop.create();
//		System.out.println("version=" + desktop.version());
//		System.out.println("Vulkan=" + desktop.isVulkanSupported());
//
//		final var extensions = desktop.extensions();
//		System.out.println("extensions=" + extensions);
//
//		System.out.println("Opening window...");
//		final Window window = new Window.Builder()
//				.title("DesktopTestTemp")
//				.size(new Dimensions(1024, 768))
//				.hint(Hint.CLIENT_API, 0)
//				.hint(Hint.VISIBLE, 1)
//				.build(desktop.library());
//
//		/
//		// 		void event(Handle window, double x, double y);
//
//		//final DeviceLibrary deviceLibrary = desktop.library();
//
//		final var listener = new DeviceLibrary.MouseListener() {
//			@Override
//			public void event(MemorySegment window, double x, double y) {
//				System.out.println("event="+window+" "+x+","+y);
//			}
//		};
//
//		final MethodType type = MethodType.methodType(void.class, MemorySegment.class, double.class, double.class);
//		final MethodHandle callback = MethodHandles.lookup().findVirtual(DeviceLibrary.MouseListener.class, "event", type).bindTo(listener);
//		System.out.println("***** callback="+callback);
//
//		final MemorySegment stub = Linker.nativeLinker().upcallStub(
//				callback,
//				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE),
//				Arena.ofAuto()
//		);
//		System.out.println("stub="+stub);
//
//		window.register(new Handle(stub));
//		System.out.println("registered");


	private static void loop(MouseDevice mouse) throws InterruptedException {
		while(true) {
			mouse.poll();
			Thread.sleep(50);
		}
	}

	void main() throws Exception {
		System.out.println("Initialising logging...");
		try(final var config = VulkanIntegrationDemo.class.getResourceAsStream("/logging.properties")) {
			LogManager.getLogManager().readConfiguration(config);
		}

		System.out.println("Initialising GLFW...");
		final var desktop = Desktop.create();
		System.out.println("version=" + desktop.version());
		System.out.println("Vulkan=" + desktop.isVulkanSupported());

		final var extensions = desktop.extensions();
		System.out.println("extensions=" + extensions);

		System.out.println("Opening window...");
		final Window window = new Window.Builder()
				.title("DesktopTestTemp")
				.size(new Dimensions(1024, 768))
				.hint(Hint.CLIENT_API, 0)
				.hint(Hint.VISIBLE, 1)				// TODO
				.build(desktop);

		/////////////////

		final MouseDevice mouse = new MouseDevice(window);

		final var listener = new DeviceLibrary.MouseListener() {
			@Override
			public void event(MemorySegment window, double x, double y) {
				System.err.println("event "+window+" "+x+","+y);
			}
		};

		mouse.add(listener);

		desktop.error().ifPresent(System.err::println);

		loop(mouse);

		/////////////////

		System.out.println("Initialising Vulkan...");
		final var vulkan = Vulkan.create();

//		System.out.println("Supported instance layers...");
//		for(VkLayerProperties layer : Instance.layers(vulkan.get())) {
//			System.out.println(layer.layerName);
//		}
//		for(VkExtensionProperties ext : Instance.extensions(vulkan.get())) {
//			System.out.println("  " + ext.extensionName);
//		}

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanIntegrationDemo")
				.extension(DiagnosticHandler.EXTENSION)
				.extensions(extensions)
				.layer(Vulkan.STANDARD_VALIDATION)
				.build(vulkan);

		System.out.println("Attaching diagnostic handler...");
		final DiagnosticHandler handler = new DiagnosticHandler.Builder().build(instance, DefaultRegistry.create());			// TODO - registry -> library?

		System.out.println("Getting surface...");
		final Handle handle;
		try {
			handle = window.surface(instance.handle());
		}
		catch(RuntimeException e) {
			desktop.error().ifPresent(System.err::println);
			throw e;
		}

		System.out.println("Enumerating devices...");
		final PhysicalDevice physical = new DeviceEnumerationHelper(instance, vulkan)
				.enumerate()
				.filter(VulkanSurface.presentation(handle, vulkan))
				.toList()
				.getFirst();

//		System.out.println("Supported device layers...");
//		for(VkLayerProperties layer : physical.layers()) {
//			System.out.println("  " + layer.layerName);
//		}
//		System.out.println("Supported device extensions...");
//		for(VkExtensionProperties ext : physical.extensions("VK*")) {
//			System.out.println("  " + ext.extensionName);
//		}

		System.out.println("Device families...");
		for(var family : physical.families()) {
			System.out.println(family);
		}

		System.out.println("Device properties...");
		final var props = physical.properties();
		System.out.println("name="+props.deviceName);
		System.out.println("type="+props.deviceType);
		System.out.println("bufferImageGranularity="+props.limits.bufferImageGranularity);
		System.out.println("maxPushConstantsSize="+props.limits.maxPushConstantsSize);

		System.out.println("Device features...");
		System.out.println("  wideLines=" + physical.features().features().contains("wideLines"));

		System.out.println("Creating surface...");
		final var surface = new VulkanSurface(handle, physical, vulkan).load();

		System.out.println("Retrieving device memory properties...");
		final var memory = physical.memory();
		for(int n = 0; n < memory.memoryHeapCount; ++n) {
			System.out.println("- heap size=%d flags=%s".formatted(memory.memoryHeaps[n].size, memory.memoryHeaps[n].flags.enumerate(ReverseMapping.mapping(VkMemoryHeapFlag.class))));
		}
		for(int n = 0; n < memory.memoryTypeCount; ++n) {
			System.out.println("- type heap=%s props=%s".formatted(memory.memoryTypes[n].heapIndex, memory.memoryTypes[n].propertyFlags.enumerate(ReverseMapping.mapping(VkMemoryProperty.class))));
		}

		System.out.println("Creating logical device...");
		final var dev = new LogicalDevice.Builder(physical)
				.extension(Swapchain.EXTENSION)
				.queue(new RequiredQueue(physical.families().getFirst()))
				.build(vulkan);

		System.out.println("queues=" + dev.queues().values());

		System.out.println("Creating swapchain...");
		final var swapchain = new Swapchain.Builder(surface)
				.clipped(true)
//				.extent(new Dimensions(1024, 768))		// TODO - needed?
				.build(dev);

		System.out.println("views=" + swapchain.attachments());

		// Shaders

		// Pipeline

		System.out.println("Cleanup...");
		swapchain.destroy();
		dev.destroy();
		surface.destroy();
		handler.destroy();
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("DONE");
	}
}
