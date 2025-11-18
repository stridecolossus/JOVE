package org.sarge.jove.foreign;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.LogManager;

import org.sarge.jove.common.*;
import org.sarge.jove.control.*;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.pipeline.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.render.*;
import org.sarge.jove.platform.vulkan.render.FrameComposer.BufferPolicy;
import org.sarge.jove.platform.vulkan.render.SwapchainFactory.SwapchainConfiguration;

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

		final var extensions = desktop.extensions();
		System.out.println("extensions=" + extensions);

//		final var monitors = Monitor.monitors(desktop);
//		System.out.println(monitors);
//		monitors.stream().map(m -> m.mode(desktop)).forEach(System.out::println);

		System.out.println("Opening window...");
		final Window window = new Window.Builder()
				.title("DesktopTestTemp")
				.size(new Dimensions(1024, 768))
				.hint(Hint.CLIENT_API, 0)
				.hint(Hint.VISIBLE, 1)
				.build(desktop);

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
				.layer(DiagnosticHandler.STANDARD_VALIDATION)
				.build(vulkan);

		System.out.println("Attaching diagnostic handler...");
		final DiagnosticHandler handler = new DiagnosticHandler.Builder().build(instance, DefaultRegistry.create());			// TODO - registry -> library?
		// TODO - option to exit on errors?

		System.out.println("Getting surface...");
		final var surface = new VulkanSurface(window, instance, vulkan);

		System.out.println("Enumerating devices...");
		final Selector graphicsSelector = Selector.queue(VkQueueFlag.GRAPHICS);
		final Selector presentationSelector = new Selector(surface::isPresentationSupported);
		final PhysicalDevice physical = PhysicalDevice
				.enumerate(instance, vulkan)
				.filter(graphicsSelector)
				.filter(presentationSelector)
				.findAny()
				.orElseThrow();

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
		System.out.println("maxMemoryAllocationCount="+props.limits.maxMemoryAllocationCount);

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
				.layer(DiagnosticHandler.STANDARD_VALIDATION)
				.queue(new RequiredQueue(graphicsFamily))
				.build(vulkan);

		final WorkQueue graphicsQueue = device.queues().get(graphicsFamily).getFirst();
		final WorkQueue presentationQueue = device.queues().get(presentationFamily).getFirst();
		System.out.println("graphics=" + graphicsQueue);
		System.out.println("presentation=" + presentationQueue);

		System.out.println("Creating swapchain...");
		final var properties = surface.properties(physical);
		final var builder = new Swapchain.Builder()
				.clipped(true)
				.init(properties.capabilities())
				.clear(new Colour(0.3f, 0.3f, 0.3f, 1f));

		System.out.println("Creating swapchain factory...");
		final SwapchainConfiguration[] configuration = {
				new ImageCountSwapchainConfiguration(ImageCountSwapchainConfiguration.MIN),
				new SurfaceFormatSwapchainConfiguration(new SurfaceFormatWrapper(VkFormat.R32G32B32_SFLOAT, VkColorSpaceKHR.SRGB_NONLINEAR_KHR)),
				new PresentationModeSwapchainConfiguration(List.of(VkPresentModeKHR.MAILBOX_KHR)),
				new SharingModeSwapchainConfiguration(List.of(graphicsFamily, presentationFamily)),
				new ExtentSwapchainConfiguration()
		};
		final var factory = new SwapchainFactory(device, properties, builder, List.of(configuration));

		// Shaders
		System.out.println("Creating shaders...");
		final var shaderLoader = new ShaderLoader(device);
		final Shader vertex = shaderLoader.load(Path.of("../Demo/Triangle/src/main/resources/spv.triangle.vert"));
		final Shader fragment = shaderLoader.load(Path.of("../Demo/Triangle/src/main/resources/spv.triangle.frag"));

		// Render pass
		System.out.println("Building render pass...");
		final var colour = Attachment.colour(factory.swapchain().format());
		final Subpass subpass = new Subpass.Builder()
				.colour(colour)
				.build();
		final RenderPass pass = new RenderPass.Builder()
				.add(subpass)
				.build(device);

		// Pipeline
		System.out.println("Building pipeline...");
		final var pipelineLayout = new PipelineLayout.Builder().build(device);
		final var pipelineBuilder = new GraphicsPipelineBuilder();
		pipelineBuilder.assembly().topology(Primitive.TRIANGLE);
		pipelineBuilder.viewport().viewportAndScissor(factory.swapchain().extents().rectangle());
		pipelineBuilder.rasterizer().cull(VkCullMode.NONE);
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, vertex));
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStage.FRAGMENT, fragment));
		final Pipeline pipeline = pipelineBuilder
				.pass(pass)
				.layout(pipelineLayout)
				.build(device);

		// Command Pool
		System.out.println("Creating command pool...");
		final var pool = Command.Pool.create(device, graphicsQueue, VkCommandPoolCreateFlag.RESET_COMMAND_BUFFER);

		// Frame buffers
		System.out.println("Building frame buffers...");
		final var group = new Framebuffer.Group(factory.swapchain(), pass, List.of());

		// Sequence
		System.out.println("Recording render sequence...");
		final var draw = DrawCommand.draw(3, device);
		final Consumer<Command.Buffer> sequence = buffer -> {
			buffer.add(pipeline.bind());
			buffer.add(draw);
		};
		final var composer = new FrameComposer(pool, BufferPolicy.DEFAULT, sequence);
		final var render = new RenderTask(factory, group, composer);

		// Render...
		System.out.println("Rendering...");
		final var tracker = new Frame.Tracker();
		final var counter = new FrameCounter();
		tracker.add(counter);
		try(var loop = new RenderLoop(render, tracker)) {
			loop.start();
			Thread.sleep(2000);
		}
		System.out.println(counter);

		// Cleanup
		System.out.println("Cleanup...");
		render.destroy();
		pool.destroy();
		pipeline.destroy();
		pipelineLayout.destroy();
		group.destroy();
		pass.destroy();
		fragment.destroy();
		vertex.destroy();
		factory.destroy();
		device.destroy();
		surface.destroy();
		handler.destroy();
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("DONE");
	}
}

//
//	private static VulkanBuffer demo(LogicalDevice device, PhysicalDevice physical, WorkQueue queue) {
//
//		// Builds mesh
//
//		final Vertex[] vertices = {
//			new Vertex(new Point(-0.5f, -0.5f, 0), Coordinate2D.TOP_LEFT),
//			new Vertex(new Point(-0.5f, +0.5f, 0), Coordinate2D.BOTTOM_LEFT),
//			new Vertex(new Point(+0.5f, -0.5f, 0), Coordinate2D.TOP_RIGHT),
//			new Vertex(new Point(+0.5f, +0.5f, 0), Coordinate2D.BOTTOM_RIGHT),
//		};
//
////		final var mesh = new MutableMesh(Primitive.TRIANGLE_STRIP, List.of(Point.LAYOUT, Coordinate2D.LAYOUT));
////		for(Vertex v : vertices) {
////			mesh.add(v);
////		}
//
//		// Init memory
//
//		final var types = MemoryType.enumerate(physical.memory());
//		final var allocator = new Allocator(device, types);
//
//		// Create staging
//
//		final var stagingProperties = new MemoryProperties.Builder<VkBufferUsageFlag>()
//				.required(VkMemoryProperty.HOST_VISIBLE)
//				.optimal(VkMemoryProperty.DEVICE_LOCAL)
//				.usage(VkBufferUsageFlag.TRANSFER_SRC)
//				.build();
//
//		final var staging = VulkanBuffer.create(device, allocator, 4 * (3 + 2) * 4, stagingProperties);
//
//		final ByteBuffer bb = staging.buffer();
//		for(Vertex v : vertices) {
//			v.buffer(bb);
//		}
//
//		// Create VBO
//
//		final var destProperties = new MemoryProperties.Builder<VkBufferUsageFlag>()
//				.required(VkMemoryProperty.DEVICE_LOCAL)
//				.usage(VkBufferUsageFlag.TRANSFER_DST)
//				.usage(VkBufferUsageFlag.VERTEX_BUFFER)
//				.build();
//
//		final var dest = VulkanBuffer.create(device, allocator, 4 * (3 + 2) * 4, destProperties);
//
//		// Copy staging to VBO
//
//		final var pool = Command.Pool.create(device, queue);
//		Work.submit(staging.copy(dest), pool);
//
//		staging.destroy();
//		pool.destroy();
//
//		return dest;
//	}
//}
