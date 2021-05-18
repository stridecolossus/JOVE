package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.VkAttachmentLoadOp;
import org.sarge.jove.platform.vulkan.VkAttachmentStoreOp;
import org.sarge.jove.platform.vulkan.VkCullMode;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.Instance;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.core.Message.HandlerBuilder;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.core.Shader;
import org.sarge.jove.platform.vulkan.core.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.core.Work;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.render.FrameBuffer;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.util.DataSource;
import org.sarge.jove.util.ResourceLoader;

public class TriangleDemo {
	public static void main(String[] args) throws Exception {
		// Open desktop
		final Desktop desktop = Desktop.create();
		if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");

		// Create window
		final Window window = new Window.Builder(desktop)
				.title("demo")
				.size(new Dimensions(1280, 760))
				.property(Window.Property.DISABLE_OPENGL)
				.build();

		// Init Vulkan
		final VulkanLibrary lib = VulkanLibrary.create();

		// Create instance
		final Instance instance = new Instance.Builder()
				.vulkan(lib)
				.name("test")
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.extensions(desktop.extensions())
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();

		// Attach message handler
		instance.attach(HandlerBuilder.create());

		// Lookup surface
		final Handle surfaceHandle = window.surface(instance.handle());

		/**
		 *
		 * Selector graphics = Selector.of(VkQueueFlag.GRAPHICS);
		 * Selector present = Selector.presentation(surface);
		 *
		 * SelectedDevice selected = PhysicalDevice
		 * 		.enumerate(...)
		 * 		.map(SelectedDevice.of(graphics, present))
		 * 		.findAny();
		 *
		 * LogicalDevice dev = new Builder()
		 * 		.queue(selected.family(graphics))
		 * 		.queue(selected.family(present))
		 * 		.build();
		 *
		 * Queue graphicsQueue = dev.queue(selected.family(graphics));
		 * ...
		 *
		 */

//		// Create queue family predicates
//		final Selector graphics = Selector.of(VkQueueFlag.GRAPHICS);
//		final Selector present = Selector.of(surfaceHandle);
// OR somehow map(dev) -> selected-device + mapped queues?

		final var predicate = Family.predicate(VkQueueFlag.GRAPHICS);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(PhysicalDevice.filter(predicate))
				.filter(dev -> dev.presentation(surfaceHandle).isPresent())
				.findAny()
				.orElseThrow(() -> new RuntimeException("No GPU available"));

		Family graphics = gpu.families().stream().filter(predicate).findAny().orElseThrow();
		Family present = graphics; // gpu.presentation(surfaceHandle).orElseThrow();

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.queue(graphics)
				.queue(present)
				.build();

		// Create rendering surface
		final Surface surface = new Surface(surfaceHandle, gpu);

		// Create swap-chain
		final Swapchain chain = new Swapchain.Builder(dev, surface)
				.count(2)
				.clear(new Colour(0.3f, 0.3f, 0.3f, 1))
				.build();

		// Create render pass
		final RenderPass pass = new RenderPass.Builder(dev)
				.attachment()
					.format(Swapchain.DEFAULT_FORMAT)
					.load(VkAttachmentLoadOp.CLEAR)
					.store(VkAttachmentStoreOp.STORE)
					.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
					.build()
				.subpass()
					.colour(0, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
					.build()
				.build();

		// TODO - need to re-generate the shader to get correct triangle winding order???

		// Load shaders
		final DataSource src = DataSource.of("./src/test/resources/demo/triangle");
		final var loader = ResourceLoader.of(src, new ShaderLoader(dev));
		final Shader vert = loader.load("spv.triangle.vert");
		final Shader frag = loader.load("spv.triangle.frag");

		// Create pipeline
		final var layout = new PipelineLayout.Builder(dev).build();
		final Pipeline pipeline = new Pipeline.Builder()
				.layout(layout)
				.pass(pass)
				.viewport(chain.extents())
				.rasterizer()
					.cull(VkCullMode.NONE) // TODO
					.build()
				.shader()
					.stage(VkShaderStageFlag.VERTEX)
					.shader(vert)
					.build()
				.shader()
					.stage(VkShaderStageFlag.FRAGMENT)
					.shader(frag)
					.build()
				.build(dev);

		// Create frame buffers
		final var buffers = chain
				.views()
				.stream()
				.map(view -> FrameBuffer.create(List.of(view), pass))
				.collect(toList());

		// Create command pool
		final Queue queue = dev.queue(present);
		final Command.Pool pool = Command.Pool.create(queue);
		final List<Command.Buffer> commands = pool.allocate(buffers.size());

		// Record render commands
		final Command draw = (api, handle) -> api.vkCmdDraw(handle, 3, 1, 0, 0);		// TODO - builder
		for(int n = 0; n < commands.size(); ++n) {
			final Command.Buffer cb = commands.get(n);
			cb
				.begin()
					.add(pass.begin(buffers.get(n)))
					.add(pipeline.bind())
					.add(draw)
					.add(RenderPass.END_COMMAND)
				.end();
		}

//		for(int n = 0; n < 100; ++n) {
		final Semaphore semaphore = dev.semaphore();
			final int index = chain.acquire(semaphore, null);

			new Work.Builder()
					.add(commands.get(index))
					.build()
					.submit(null);

			queue.waitIdle();

//			Thread.sleep(50);

			chain.present(queue, Set.of());


//			dev.queue(present).waitIdle();
//		}
			Thread.sleep(1000);

		//////////////

		// Destroy window
		window.destroy();
		desktop.destroy();

		// Destroy render pass
		buffers.forEach(FrameBuffer::destroy);

		pool.free();

		pool.destroy();
		pass.destroy();
		semaphore.destroy();

		// Destroy pipeline
		vert.destroy();
		frag.destroy();
		pipeline.destroy();
		layout.destroy();

		chain.destroy();
		surface.destroy();

		// Destroy device
		dev.destroy();
		instance.destroy();
	}
}
