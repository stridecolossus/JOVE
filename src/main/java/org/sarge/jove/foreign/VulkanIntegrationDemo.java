package org.sarge.jove.foreign;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.LogManager;

import org.sarge.jove.common.*;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.pipeline.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.render.*;
import org.sarge.jove.platform.vulkan.render.Subpass.AttachmentReference;

public class VulkanIntegrationDemo {

	static void loop(Window window) throws InterruptedException {
		while(true) {
			Thread.sleep(100);
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

//		final var monitors = Monitor.monitors(desktop);
//		System.out.println(monitors);
//		monitors.stream().map(m -> m.mode(desktop)).forEach(System.out::println);

		System.out.println("Opening window...");
		final Dimensions size = new Dimensions(1024, 768);
		final Window window = new Window.Builder()
				.title("DesktopTestTemp")
				.size(size)
				.hint(Hint.CLIENT_API, 0)
				.hint(Hint.VISIBLE, 1)
				.build(desktop);

//		window.fiddle();

//		/////////////////
//
//		final MouseDevice mouse = new MouseDevice(window);
//
//		final var listener = new DeviceLibrary.MouseListener() {
//			@Override
//			public void event(MemorySegment window, double x, double y) {
//				System.err.println("event "+window+" "+x+","+y);
//			}
//		};
//
//		mouse.add(listener);
//
//		desktop.error().ifPresent(System.err::println);
//
//		loop(mouse);
//
//		/////////////////

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
		// TODO - option to exit on errors?

		System.out.println("Getting surface...");
		final Handle handle = window.surface(instance.handle());

		System.out.println("Enumerating devices...");
		final Selector graphicsSelector = PhysicalDevice.Selector.queue(Set.of(VkQueueFlag.GRAPHICS));
		final Selector presentationSelector = VulkanSurface.presentation(handle, vulkan);
		final PhysicalDevice physical = new DeviceEnumerationHelper(instance, vulkan)
				.enumerate()
				.filter(graphicsSelector)
				.filter(presentationSelector)
				.toList()
				.getFirst();

		System.out.println("Extracting families...");
		final Family graphicsFamily = graphicsSelector.family(physical);
		final Family presentationFamily = presentationSelector.family(physical);
		System.out.println("graphics=" + graphicsFamily);
		System.out.println("presentation" + presentationFamily);

//		System.out.println("Supported device layers...");
//		for(VkLayerProperties layer : physical.layers()) {
//			System.out.println("  " + layer.layerName);
//		}
//		System.out.println("Supported device extensions...");
//		for(VkExtensionProperties ext : physical.extensions("VK*")) {
//			System.out.println("  " + ext.extensionName);
//		}

		System.out.println("Device properties...");
		final var props = physical.properties();
		System.out.println("name="+props.deviceName);
		System.out.println("type="+props.deviceType);
		System.out.println("bufferImageGranularity="+props.limits.bufferImageGranularity);
		System.out.println("maxPushConstantsSize="+props.limits.maxPushConstantsSize);

		System.out.println("Creating surface...");
		final var surface = new VulkanSurface(handle, physical, vulkan).load();

//		System.out.println("Retrieving device memory properties...");
//		final var memory = physical.memory();
//		for(int n = 0; n < memory.memoryHeapCount; ++n) {
//			System.out.println("- heap size=%d flags=%s".formatted(memory.memoryHeaps[n].size, memory.memoryHeaps[n].flags.enumerate(ReverseMapping.mapping(VkMemoryHeapFlag.class))));
//		}
//		for(int n = 0; n < memory.memoryTypeCount; ++n) {
//			System.out.println("- type heap=%s props=%s".formatted(memory.memoryTypes[n].heapIndex, memory.memoryTypes[n].propertyFlags.enumerate(ReverseMapping.mapping(VkMemoryProperty.class))));
//		}

		System.out.println("Creating logical device...");
		final var device = new LogicalDevice.Builder(physical)
				.extension(Swapchain.EXTENSION)
				.queue(new RequiredQueue(graphicsFamily))
				//.queue(new RequiredQueue(presentationFamily))			// TODO - queue family must be unique! =? need to check if graphics == presentation
				.build(vulkan);

		final WorkQueue graphicsQueue = device.queues().get(graphicsFamily).getFirst();
		final WorkQueue presentationQueue = device.queues().get(presentationFamily).getFirst();
		System.out.println("graphics=" + graphicsQueue);
		System.out.println("presentation=" + presentationQueue);

		System.out.println("Creating swapchain...");
		//https://vulkan-tutorial.com/en/Drawing_a_triangle/Presentation/Swap_chain
		// TODO - select extents
		// TODO - sharing mode ~ whether queue family is for both render & presentation
		final var swapchain = new Swapchain.Builder(surface)
				.clipped(true)
				.presentation(VkPresentModeKHR.MAILBOX_KHR)
				.clear(new Colour(0.6f, 0.6f, 0.6f, 1))
				.build(device);

		// Shaders
		System.out.println("Creating shaders...");
		final var shaderLoader = new ShaderLoader(device);
		final Shader vertex = shaderLoader.load(Path.of("../Demo/Triangle/src/main/resources/spv.triangle.vert"));
		final Shader fragment = shaderLoader.load(Path.of("../Demo/Triangle/src/main/resources/spv.triangle.frag"));

		// Render pass
		System.out.println("Building render pass...");
		final var colour = Attachment.colour(swapchain.format());
		final Subpass subpass = new Subpass(List.of(new AttachmentReference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)), null, Set.of());
//		final Dependency dependency = new Dependency(
//				new Dependency.Properties(Dependency.VK_SUBPASS_EXTERNAL, Set.of(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT), Set.of()),
//				new Dependency.Properties(subpass, Set.of(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT), Set.of(VkAccess.COLOR_ATTACHMENT_WRITE)),
//				Set.of()
//		);
		final RenderPass pass = new RenderPass.Builder()
				.add(subpass)
//				.dependency(dependency)
				.build(device);

		// Pipeline
		// TODO - this still all sucks
		System.out.println("Building pipeline...");
		final var pipelineLayout = new PipelineLayout.Builder().build(device);
		final var pipelineBuilder = new GraphicsPipelineBuilder();
		pipelineBuilder.assembly().topology(Primitive.TRIANGLE);
		pipelineBuilder.viewport().viewportAndScissor(new Rectangle(size));
		pipelineBuilder.rasterizer().winding(VkFrontFace.CLOCKWISE);
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, vertex, "main", null));
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStage.FRAGMENT, fragment, "main", null));
		final Pipeline pipeline = pipelineBuilder
				.pass(pass)
				.layout(pipelineLayout)
				.build(device);

		// Command Pool
		System.out.println("Creating command pool...");
		final var commandPool = Command.Pool.create(device, graphicsQueue, VkCommandPoolCreateFlag.RESET_COMMAND_BUFFER);

		// Frame buffers
		System.out.println("Building frame buffers...");
		final var group = new FrameBuffer.Group(swapchain, pass, List.of());

		// Sequence
		System.out.println("Recording render sequence...");
		final var draw = new DrawCommand.Builder().vertexCount(3).build(device);		// TODO - helper
		final var sequence = commandPool.allocate(group.size(), true);
		for(int n = 0; n < group.size(); ++n) {
			final Command.Buffer cb = sequence.get(n);
			final FrameBuffer fb = group.get(n);
    		cb
        		.begin()
        			.add(fb.begin(VkSubpassContents.INLINE))		// TODO - default
        				.add(pipeline.bind())
        				.add(draw)		// TODO - cannot draw if no pipeline bound!?
        			.add(fb.end())
        		.end();
		}

		// Render
		System.out.println("Rendering...");
		final var available = VulkanSemaphore.create(device);
		final var ready = VulkanSemaphore.create(device);
		final var fence = Fence.create(device);
		for(int n = 0; n < 10; ++n) {
    		final int index = swapchain.acquire(available, null);
    		device.waitIdle();

    		new Work.Builder()
    				.add(sequence.get(index))
    				.wait(available, Set.of(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT))
    				.signal(ready)
    				.build()
    				.submit(fence);
    		device.waitIdle();

    		swapchain.present(presentationQueue, index, ready);		// TODO - null ready semaphore?
    		device.waitIdle();

    		fence.reset();
    		Thread.sleep(50);
		}

		// Cleanup
		System.out.println("Cleanup...");
		///////////////
		available.destroy();
		ready.destroy();
		fence.destroy();
		///////////////
		commandPool.destroy();
		pipeline.destroy();
		pipelineLayout.destroy();
		group.destroy();
		pass.destroy();
		fragment.destroy();
		vertex.destroy();
		swapchain.destroy();
		device.destroy();
		surface.destroy();
		handler.destroy();
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("DONE");
	}
}
