package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.VkAttachmentLoadOp;
import org.sarge.jove.platform.vulkan.VkAttachmentStoreOp;
import org.sarge.jove.platform.vulkan.VkColorSpaceKHR;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Instance;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.core.Queue;
import org.sarge.jove.platform.vulkan.core.Shader;
import org.sarge.jove.platform.vulkan.core.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.core.Work;
import org.sarge.jove.platform.vulkan.pipeline.FrameBuffer;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass;
import org.sarge.jove.platform.vulkan.pipeline.Swapchain;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.util.DataSource;

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
		instance.handler().init().attach();

		// Lookup surface
		final Handle surfaceHandle = window.surface(instance.handle());

		// Create queue family predicates
		final var graphicsPredicate = Queue.Family.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
		final var transferPredicate = Queue.Family.predicate(VkQueueFlag.VK_QUEUE_TRANSFER_BIT);
		final var presentationPredicate = Queue.Family.predicate(surfaceHandle);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(PhysicalDevice.predicate(graphicsPredicate))
				.filter(PhysicalDevice.predicate(transferPredicate))
				.filter(PhysicalDevice.predicate(presentationPredicate))
				.findAny()
				.orElseThrow(() -> new RuntimeException("No GPU available"));

		// Lookup required queues
//		final QueueFamily graphics = gpu.find(graphicsPredicate, "Graphics family not available");
		final Queue.Family transfer = gpu.family(transferPredicate);
		final Queue.Family present = gpu.family(presentationPredicate);

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
//				.queue(graphics)
				.queue(transfer)
				.queue(present)
				.build();

		// Create rendering surface
		final Surface surface = new Surface(surfaceHandle, dev);

		// Specify required image format
		final VkFormat format = new FormatBuilder()
				.components(FormatBuilder.BGRA)
				.bytes(1)
				.signed(false)
				.type(FormatBuilder.Type.NORMALIZED)
				.build();

		// Create swap-chain
		final Swapchain chain = new Swapchain.Builder(dev, surface)
				.count(2)
				.format(format)
				.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				.clear(new Colour(0.3f, 0.3f, 0.3f, 1))
				.build();

		// Create render pass
		final RenderPass pass = new RenderPass.Builder(dev)
				.attachment()
					.format(format)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
					.build()
				.subpass()
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.build()
				.build();

		// Load shaders
		final DataSource src = DataSource.of("./src/test/resources/demo/triangle");
		final var loader = src.loader(new ShaderLoader(dev));
		final Shader vert = loader.load("spv.triangle.vert");
		final Shader frag = loader.load("spv.triangle.frag");

		// Create pipeline
		final Rectangle extent = new Rectangle(chain.extents());
		final Pipeline pipeline = new Pipeline.Builder(dev)
				.layout(new Pipeline.Layout.Builder(dev).build()) // TODO
				.pass(pass)
				.viewport()
					.viewport(extent)
					.build()
				.shader()
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
					.shader(vert)
					.build()
				.shader()
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
					.shader(frag)
					.build()
				.build();

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
			final int index = chain.acquire(null, null);

			new Work.Builder()
					.add(commands.get(index))
					.build()
					.submit(null);

			dev.queue(present).waitIdle();

//			Thread.sleep(50);

			chain.present(dev.queue(present), Set.of());


//			dev.queue(present).waitIdle();
//		}
			Thread.sleep(1000);

		//////////////

		// Destroy window
		window.destroy();
		desktop.destroy();

		// Destroy render pass
		buffers.forEach(FrameBuffer::destroy);
		pool.destroy();
		pass.destroy();

		// Destroy pipeline
		vert.destroy();
		frag.destroy();
		pipeline.destroy();

		chain.destroy();
		surface.destroy();

		// Destroy device
		dev.destroy();
		instance.destroy();
	}
}
