package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.control.Action;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.control.InputEvent.Type.Button;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.BufferedModel.ModelLoader;
import org.sarge.jove.model.Model;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.desktop.WindowDescriptor;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.QueueFamily;
import org.sarge.jove.platform.vulkan.core.Work.ImmediateCommand;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet;
import org.sarge.jove.platform.vulkan.pipeline.DrawCommand;
import org.sarge.jove.platform.vulkan.pipeline.FrameBuffer;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass;
import org.sarge.jove.platform.vulkan.pipeline.Sampler;
import org.sarge.jove.platform.vulkan.pipeline.SwapChain;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.Projection;
import org.sarge.jove.util.Loader;
import org.sarge.jove.util.MathsUtil;

public class ModelDemo {


	public static View texture(LogicalDevice dev, Command.Pool pool) throws IOException {
		// Load image
		final Path dir = Paths.get("./src/test/resources");
		final var src = Loader.DataSource.of(dir);
		final var loader = Loader.of(src, new ImageData.Loader());
		final ImageData image = loader.load("demo/model/chalet.jpg");
		final VkFormat format = FormatBuilder.format(image);

		// Copy image to staging buffer
		final VertexBuffer staging = VertexBuffer.staging(dev, image.data().limit());
		staging.load(image.data());

		// Create texture
		final Image texture = new Image.Builder(dev)
				.extents(Image.Extents.of(image.size()))
				.format(format)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_SAMPLED_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

		// Transition texture ready for copying
		new Barrier.Builder()
				.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
				.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT)
				.barrier(texture)
					.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
					.destination(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
					.subresource()
						.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)		// TODO - init from image?
						.build()
					.build()
				.build()
				.submit(pool, true);

		// Copy staging to texture
		new ImageCopyCommand.Builder()
				.buffer(staging)
				.image(texture)
				.layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.subresource()
					.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)		// TODO - init from image?
					.build()
				.build()
				.submit(pool, true);

		// Release staging
		staging.destroy();

		// Transition texture ready for sampling
		// TODO - source flag & access flag and old-layout could be initialised from previous barrier?
		new Barrier.Builder()
				.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT)
				.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
				.barrier(texture)
					.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
					.source(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
					.destination(VkAccessFlag.VK_ACCESS_SHADER_READ_BIT)
					.subresource()
						.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)		// TODO - init from image?
						.build()
					.build()
				.build()
				.submit(pool, true);

		final View view = new View.Builder(dev)
				.image(texture)
				.subresource()
					.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
					.build()
				.build();

		return view;
	}

	private static VertexBuffer loadBuffer(LogicalDevice dev, ByteBuffer bb, Command.Pool pool) {
		// Create staging VBO
		final VertexBuffer staging = VertexBuffer.staging(dev, bb.limit());

		// Load to staging
		staging.load(bb);

		// Create device VBO
		final VertexBuffer dest = new VertexBuffer.Builder(dev)
				.length(bb.limit())
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

		// Copy
		ImmediateCommand.of(staging.copy(dest)).submit(pool, true);

		// Release staging buffer
		staging.destroy();

		return dest;
	}

	private static View depth(LogicalDevice dev, Image.Extents extents) {
			// Create depth buffer image
			final Image depth = new Image.Builder(dev)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
				.extents(extents)
				.format(VkFormat.VK_FORMAT_D32_SFLOAT)
				.tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

			// Create view
			final View view = new View.Builder(dev)
					.image(depth)
					.subresource()
						.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
						.build()
					.build();

			/*
			depth
				.barrier()
				.layout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
				.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
				.destination(VkAccessFlag.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT)
				.destination(VkAccessFlag.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT)
				.transition(pool);
*/
			return view;

	}

	public static void main(String[] args) throws Exception {
		// Open desktop
		final Desktop desktop = Desktop.create();
		if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");

		// Create window
		final var descriptor = new WindowDescriptor.Builder()
				.title("demo")
				.size(new Dimensions(1280, 760))
				.property(WindowDescriptor.Property.DISABLE_OPENGL)
				.build();
		final Window window = desktop.window(descriptor);

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
		final var handler = new MessageHandler.Builder()
				.init()
				.callback(MessageHandler.CONSOLE)
				.build();
		instance.add(handler);

		// Lookup surface
		final Handle surfaceHandle = window.surface(instance.handle());

		// Create queue family predicates
		final var graphicsPredicate = PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
		final var transferPredicate = PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_TRANSFER_BIT);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(PhysicalDevice.predicate(graphicsPredicate))
				.filter(PhysicalDevice.predicate(transferPredicate))
				.filter(PhysicalDevice.predicatePresentationSupported(surfaceHandle))
				.findAny()
				.orElseThrow(() -> new RuntimeException("No GPU available"));

		// Lookup required queues
		final QueueFamily graphics = gpu.find(graphicsPredicate, "Graphics family not available");
		final QueueFamily transfer = gpu.find(transferPredicate, "Transfer family not available");
		final QueueFamily present = gpu.find(family -> family.isPresentationSupported(surfaceHandle), "Presentation family not available");

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder() // TODO - parent as ctor arg
				.parent(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				//.queue(graphics) TODO!!!
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
		final SwapChain chain = new SwapChain.Builder(surface)
				.count(2)
				.format(format)
				.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				.clear(new Colour(0.3f, 0.3f, 0.3f, 1))
				.build();

		final Rectangle rect = new Rectangle(chain.extents());

		final View depth = depth(dev, Image.Extents.of(chain.extents()));

		// Create render pass
		final RenderPass pass = new RenderPass.Builder(dev)
				.attachment()
					.format(format)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
					.build()
				.attachment()
					.format(VkFormat.VK_FORMAT_D32_SFLOAT)		// TODO - lookup optimal/available
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
					.build()
				.subpass()
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.depth(1)
					.build()
//				.dependency()
//					.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
//					.destination(0)
//					.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
//					.destination(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
//					.build()
				.build();

		// Load shaders
		final Path dir = new File("./src/test/resources/demo/cube.rotate").toPath(); // TODO - root + resolve
		final var src = Loader.DataSource.of(dir);
		final var shaderLoader = Loader.of(src, Shader.loader(dev));
		final Shader vert = shaderLoader.load("spv.cube.vert");
		final Shader frag = shaderLoader.load("spv.cube.frag");

		//////////////////

		// Load model
//		final ObjectModelLoader objLoader = new ObjectModelLoader();
//		final Model model = objLoader.load(new FileReader("./src/test/resources/demo/model/chalet.obj")).build();
		/////
		final ModelLoader loader = new ModelLoader();
//		writer.write(model, new FileOutputStream("./src/test/resources/demo/model/chalet.model"));
		final Model model = loader.load(new FileInputStream("./src/test/resources/demo/model/chalet.model"));

		// Load VBO
		final Command.Pool copyPool = Command.Pool.create(dev.queue(transfer));
		final VertexBuffer vbo = loadBuffer(dev, model.vertices(), copyPool);

		// Load IBO
		final VertexBuffer index = loadBuffer(dev, model.index().get(), copyPool);

		//////////////////

		final Command.Pool graphicsPool = Command.Pool.create(dev.queue(graphics));

		final View texture = texture(dev, graphicsPool);

		// Create descriptor layout
		final DescriptorSet.Layout setLayout = new DescriptorSet.Layout.Builder(dev)
				.binding(0)
					.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
					.build()
				.binding(1)
					.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
					.build()
				.build();

		// Create pool
		final DescriptorSet.Pool setPool = new DescriptorSet.Pool.Builder(dev)
				.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 3)
				.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 3)
				.max(2 * 3)
				.build();

		final List<DescriptorSet> descriptors = setPool.allocate(setLayout, 3);

		// Create sampler
		final Sampler sampler = new Sampler.Builder(dev).build();

		// Create uniform buffer for the projection matrix
		final VertexBuffer uniform = new VertexBuffer.Builder(dev)
				.length(Matrix.LENGTH * 3)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
				.build();

		// Load the projection matrix
		final Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
		uniform.load(proj, proj.length(), 0);

		// Create uniform buffer per swapchain image
//		final VertexBuffer[] uniforms = new VertexBuffer[3];
//		for(int n = 0; n < uniforms.length; ++n) {
//			uniforms[n] = new VertexBuffer.Builder(dev)
//					.length(projSize)
//					.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
//					.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
//					.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
//					.build();
//
//			uniforms[n].load(
//		}

		// Rotate model
		uniform.load(Matrix.rotation(Vector.X_AXIS, -MathsUtil.HALF_PI), Matrix.LENGTH, Matrix.LENGTH * 2);

		// Apply sampler to the descriptor sets
		new DescriptorSet.Update.Builder()
				.descriptors(descriptors)
				.add(0, sampler.update(texture))
				.add(1, uniform.update())
				.update(dev);

		//////////////////

		// Create pipeline layout
		final Pipeline.Layout pipelineLayout = new Pipeline.Layout.Builder(dev)
				.add(setLayout)
				.build();

		// Create pipeline
		final Pipeline pipeline = new Pipeline.Builder(dev)
				.layout(pipelineLayout)
				.pass(pass)
				.input()
					.binding(model.layout())
					.build()
				.assembly()
					.topology(model.primitive())
					.build()
				.viewport()
					.flip(true)
					.viewport(rect)
					.scissor(rect)
					.build()
				.rasterizer()
					.cullMode(VkCullModeFlag.VK_CULL_MODE_FRONT_BIT)
					.build()
				.depth()
					.enable(true)
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
				.map(v -> FrameBuffer.create(List.of(v, depth), pass))
				.collect(toList());

		// Create command pool
		final LogicalDevice.Queue presentQueue = dev.queue(present);
		final Command.Pool pool = Command.Pool.create(presentQueue);
		final List<Command.Buffer> commands = pool.allocate(buffers.size());

		// Record render commands
		for(int n = 0; n < commands.size(); ++n) {
			final Command.Buffer cb = commands.get(n);
			cb
				.begin()
					.add(pass.begin(buffers.get(n), rect))
					.add(pipeline.bind())
					.add(vbo.bind())
					.add(index.index())
					.add(descriptors.get(n).bind(pipelineLayout))
					.add(DrawCommand.of(model))
					.add(RenderPass.END_COMMAND)
				.end();
		}

//		final Semaphore ready = Semaphore.create(dev);
//		final Semaphore finished = Semaphore.create(dev);

		///////////////////

		final Action.Bindings bindings = new Action.Bindings();
		window.mouse().enable(InputEvent.Type.Position.class, bindings);
		window.keyboard().enable(Button.class, bindings);

		final AtomicBoolean running = new AtomicBoolean(true);
		bindings.bind(new Button(256, "escape", 0, 0), ignored -> running.set(false));

		final Camera cam = new Camera();
		cam.move(new Point(0, 0.5f, -2));

		bindings.bind(new Button(87, "W", 0, 0), ignored -> cam.move(+1));
		bindings.bind(new Button(83, "S", 0, 0), ignored -> cam.move(-1));
		bindings.bind(new Button(65, "A", 0, 0), ignored -> cam.strafe(+1));
		bindings.bind(new Button(68, "D", 0, 0), ignored -> cam.strafe(-1));

//		final MousePositionListener listener = (ptr, x, y) -> {
//			final float dx = (float) x / rect.width() * MathsUtil.PI;
//			cam.orientation(dx, 0);
//		};
//		window.setMouseMoveListener(listener);

		final Action controller = pos -> {
			final float dx = pos.x() / rect.width() * MathsUtil.PI;
			cam.orientation(dx, 0);
		};
		bindings.bind(InputEvent.Type.POSITION, controller);

		while(running.get()) {
			desktop.poll();

			uniform.load(cam.matrix(), Matrix.LENGTH, Matrix.LENGTH);

			final int idx = chain.acquire(null, null);

			new Work.Builder()
					.add(commands.get(idx))
//					.wait(ready)
//					.signal(finished)
					.stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
					.build()
					.submit();

			presentQueue.waitIdle();
//			Thread.sleep(50);

			chain.present(presentQueue, null);
			presentQueue.waitIdle();

			//Thread.sleep(50);
		}

		//////////////

		// Destroy window
		surface.destroy();
		window.destroy();
		desktop.destroy();

		final Image.DefaultImage img = (Image.DefaultImage) texture.image();
		img.destroy();
		texture.destroy();
		sampler.destroy();
		//Arrays.stream(uniforms).forEach(VertexBuffer::destroy);
		uniform.destroy();
		vbo.destroy();
		index.destroy();
		depth.destroy();

		setPool.destroy();
		setLayout.destroy();

		pool.destroy();
		copyPool.destroy();
		graphicsPool.destroy();

		vert.destroy();
		frag.destroy();

		// Destroy render pass
		buffers.forEach(FrameBuffer::destroy);
		pass.destroy();

		// Destroy pipeline
		pipelineLayout.destroy();
		pipeline.destroy();
		chain.destroy();

		// Destroy device
		dev.destroy();
		instance.destroy();
	}
}
