package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.IntFunction;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.CubeBuilder;
import org.sarge.jove.model.Model;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Resource;
import org.sarge.jove.platform.vulkan.pipeline.FrameBuffer;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass;
import org.sarge.jove.platform.vulkan.pipeline.Runner;
import org.sarge.jove.platform.vulkan.pipeline.Runner.Frame;
import org.sarge.jove.platform.vulkan.pipeline.Runner.FrameState;
import org.sarge.jove.platform.vulkan.pipeline.Sampler;
import org.sarge.jove.platform.vulkan.pipeline.Swapchain;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.scene.Projection;
import org.sarge.jove.util.DataSource;
import org.sarge.jove.util.Interpolator;
import org.sarge.jove.util.MathsUtil;

public class RotatingCubeDemo {


	public static View texture(LogicalDevice dev, Command.Pool pool) throws IOException {
		// Load image
		final Path dir = Paths.get("./src/test/resources");
		final var src = DataSource.of(dir);
		final var loader = DataSource.loader(src, new ImageData.Loader());
		final ImageData image = loader.load("thiswayup.png");
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
					.build()
				.build()
				.submit(pool);

		// Copy staging to texture
		new ImageCopyCommand.Builder(texture)
				.buffer(staging)
				.layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.build()
				.submit(pool);

		// Release staging
		staging.destroy();

		// Transition texture ready for sampling
		new Barrier.Builder()
				.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT)
				.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
				.barrier(texture)
					.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
					.source(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
					.destination(VkAccessFlag.VK_ACCESS_SHADER_READ_BIT)
					.build()
				.build()
				.submit(pool);

		return new View.Builder(dev, texture).build();
	}

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
		final Queue.Family graphics = gpu.family(graphicsPredicate);
		final Queue.Family transfer = gpu.family(transferPredicate);
		final Queue.Family present = gpu.family(presentationPredicate);

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder(gpu)
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
				.signed(true)
				.type(FormatBuilder.Type.RGB)
				.build();

		// Create swap-chain
		final Swapchain chain = new Swapchain.Builder(dev, surface)
				.count(2)
				.format(format)
				.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				.clear(new Colour(0.3f, 0.3f, 0.3f, 1))
				.build();

		final Rectangle rect = new Rectangle(chain.extents());

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
				.dependency(RenderPass.VK_SUBPASS_EXTERNAL, 0)
					.source().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destination().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destination().access(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
					.build()
				.build();

		// Load shaders
		final Path dir = new File("./src/test/resources/demo/cube.rotate").toPath(); // TODO - root + resolve
		final var src = DataSource.of(dir);
		final var shaderLoader = DataSource.loader(src, Shader.loader(dev));
		final Shader vert = shaderLoader.load("spv.cube.instanced.vert");
		final Shader frag = shaderLoader.load("spv.cube.frag");

		//////////////////

		// Buffer cube
		final Model cube = CubeBuilder.create();
		final var vertices = cube.vertices();

		// Create staging VBO
		final VertexBuffer staging = VertexBuffer.staging(dev, vertices.limit());

		// Load to staging
		staging.load(vertices);

		// Create device VBO
		final VertexBuffer dest = new VertexBuffer.Builder(dev)
				.length(vertices.limit())
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

		// Copy
		final Command.Pool copyPool = Command.Pool.create(dev.queue(transfer));
		Work.submit(staging.copy(dest), copyPool);
		staging.destroy();

		//////////////////

		final Command.Pool graphicsPool = Command.Pool.create(dev.queue(graphics));

		final View texture = texture(dev, graphicsPool);

		// Create descriptor layout
		final var samplerBinding = new DescriptorSet.Layout.Binding.Builder()
				.binding(0)
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
				.build();
		final var uniformBinding = new DescriptorSet.Layout.Binding.Builder()
				.binding(1)
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
				.build();
		final var setLayout = DescriptorSet.Layout.create(dev, List.of(samplerBinding, uniformBinding));

		// Create pool
		final var setPool = new DescriptorSet.Pool.Builder(dev)
				.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 3)
				.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 3)
				.max(2 * 3)
				.build();
				// .add(3, layout)

		final List<DescriptorSet> descriptors = setPool.allocate(setLayout, 3);

		// Create sampler
		final Sampler sampler = new Sampler.Builder(dev).build();

		// Create uniform buffer for projection, view and 4 models
		final long uniformLength = (2 + 4) * Matrix.IDENTITY.length();
		final VertexBuffer uniform = new VertexBuffer.Builder(dev)
				.length(uniformLength)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
				.build();

		// Init descriptor sets
		final Resource samplerResource = sampler.resource(texture);
		for(DescriptorSet set : descriptors) {
			set.set(samplerBinding, samplerResource);
			set.set(uniformBinding, uniform);
		}

		// Apply updates
		DescriptorSet.update(dev, descriptors);

		//////////////

		// Load the projection matrix
		final Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
		uniform.load(proj, proj.length(), 0);

		final Matrix pos = new Matrix.Builder()
				.identity()
				.row(0, Vector.X_AXIS)
				.row(1, Vector.Y_AXIS.invert())
				.row(2, Vector.Z_AXIS)
				.build();

		final Matrix trans = new Matrix.Builder()
				.identity()
				.column(3, new Point(0, 0, -3.5f))
				.build();

		final Matrix view = pos.multiply(trans);
		uniform.load(view, view.length(), view.length());

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
					.binding(cube.layout())
					.build()
				.assembly()
					.topology(cube.primitive())
					.build()
				.viewport()
					.viewport(rect)
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
				.map(v -> FrameBuffer.create(List.of(v), pass))
				.collect(toList());

		// Create command pool
		final Queue presentQueue = dev.queue(present);
		final Command.Pool pool = Command.Pool.create(presentQueue);
		final List<Command.Buffer> commands = pool.allocate(buffers.size());

		// Record render commands
		final Command draw = (api, handle) -> api.vkCmdDraw(handle, cube.count(), 4, 0, 0);
		for(int n = 0; n < commands.size(); ++n) {
			final Command.Buffer cb = commands.get(n);
			cb
				.begin()
					.add(pass.begin(buffers.get(n)))
					.add(pipeline.bind())
					.add(dest.bindVertexBuffer())
					.add(descriptors.get(n).bind(pipelineLayout))
					.add(draw)
					.add(RenderPass.END_COMMAND)
				.end();
		}

		///////////////////

		final long PERIOD = 5000;
		final long LENGTH = Matrix.IDENTITY.length();
		final long OFFSET = LENGTH * 2;

		final Interpolator linear = Interpolator.linear(0, MathsUtil.TWO_PI);
//		final Interpolator cosine = Interpolator.COSINE.andThen(linear);
//		final Interpolator squared = Interpolator.SQUARED.andThen(linear);
//		final Interpolator pulse = Interpolator.linear(1, 1.25f);

		// Create renderer
		final IntFunction<Frame> factory = idx -> new Frame() {

			@Override
			public void render(FrameState state, View view) {
				state.render(commands.get(idx));
			}

			@Override
			public boolean update() {
				// TODO
				presentQueue.waitIdle();

				// Handle input events
				desktop.poll();

				// Update rotation matrices
				final float time = System.currentTimeMillis() % PERIOD / (float) PERIOD;
				uniform.load(Matrix.rotation(Vector.X_AXIS, linear.interpolate(time)), LENGTH, OFFSET);
//				uniform.load(Matrix.rotation(Vector.Y_AXIS, cosine.interpolate(time)), LENGTH, OFFSET + LENGTH);
//				uniform.load(Matrix.rotation(Vector.Z_AXIS, squared.interpolate(time)), LENGTH, OFFSET + 2 * LENGTH);
//
//				// Update pulsing matrix
//				final float scale = Interpolator.SMOOTH.andThen(pulse).interpolate(time);
//				uniform.load(Matrix.scale(new Vector(scale, scale, scale)), LENGTH, OFFSET + 3 * LENGTH);

				return true;
			}
		};

		// Create render loop
		final Runner runner = new Runner(chain, 2, factory, dev.queue(present));

		// Create stop action
		window.keyboard().enable(ignored -> runner.stop());

		// Start render loop
		runner.start();
		presentQueue.waitIdle();

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
