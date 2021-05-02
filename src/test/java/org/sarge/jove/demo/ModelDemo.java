package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.IntFunction;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.Action.PositionAction;
import org.sarge.jove.control.Action.SimpleAction;
import org.sarge.jove.control.Action.ValueAction;
import org.sarge.jove.control.Bindings;
import org.sarge.jove.control.Button;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Matrix4;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.ModelLoader;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Message.HandlerBuilder;
import org.sarge.jove.platform.vulkan.core.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.pipeline.Runner.Frame;
import org.sarge.jove.platform.vulkan.pipeline.Runner.FrameState;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.scene.OrbitalCameraController;
import org.sarge.jove.scene.Projection;
import org.sarge.jove.util.DataSource;
import org.sarge.jove.util.MathsUtil;
import org.sarge.jove.util.ResourceLoader;

public class ModelDemo {
	public static View texture(LogicalDevice dev, Command.Pool pool) throws IOException {
		// Load image
		final Path dir = Paths.get("./src/test/resources");
		final var src = DataSource.of(dir);
		final var loader = ResourceLoader.of(src, new ImageData.Loader());
		final ImageData image = loader.load("demo/model/chalet.jpg");
		final VkFormat format = FormatBuilder.format(image);
//		//System.out.println(format); // VK_FORMAT_R8G8B8A8_SRGB
//		final VkFormat format = VkFormat.VK_FORMAT_B8G8R8A8_SRGB;

		// Copy image to staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, image.data().length());
		staging.load(image.data());

		// Create texture
		final Image texture = new Image.Builder(dev)
				.extents(Image.Extents.of(image.size()))
				.format(format)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_SAMPLED_BIT)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
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
		new ImageCopyCommand.Builder()
				.image(texture)
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

		// Create view
//		final VkComponentMapping mapping = new VkComponentMapping();
//		mapping.r = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_A;
//		mapping.g = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_B;
//		mapping.b = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_G;
//		mapping.a = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_R;
//		return new View.Builder(dev, texture).mapping(mapping).build();
		return new View.Builder(dev, texture).build();
	}

	private static VulkanBuffer loadBuffer(LogicalDevice dev, Bufferable obj, VkBufferUsageFlag usage, Command.Pool pool) {
		// Create staging VBO
		final VulkanBuffer staging = VulkanBuffer.staging(dev, obj.length());

		// Load to staging
		staging.load(obj);

		// Create device VBO
		final VulkanBuffer dest = new VulkanBuffer.Builder(dev)
				.length(obj.length())
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
				.usage(usage)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

		// Copy
		Work.submit(staging.copy(dest), pool);

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
			.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
			.build();

		// Create view
		return new View.Builder(dev, depth).build();
	}

	public static void main(String[] args) throws Exception {
		//System.setProperty("jna.library.path", "/VulkanSDK/1.1.101.0/Lib");
		//System.setProperty("jna.debug_load", "true");

		// Open desktop
		final Desktop desktop = Desktop.create();
		desktop.setErrorHandler(System.err::println);
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

		// Create queue selectors
		final var graphics = Queue.Selector.of(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
		final var transfer = Queue.Selector.of(VkQueueFlag.VK_QUEUE_TRANSFER_BIT);
		final var present = Queue.Selector.of(surfaceHandle);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(graphics)
				.filter(transfer)
				.filter(present)
				.findAny()
				.orElseThrow(() -> new RuntimeException("No GPU available"));

		// Init required features
		final var features = new VkPhysicalDeviceFeatures();
		features.samplerAnisotropy = VulkanBoolean.TRUE;
		gpu.features().check(features);

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.features(features)
				.queue(graphics)
				.queue(transfer)
				.queue(present)
				.build();

		// Create rendering surface
		final Surface surface = new Surface(surfaceHandle, gpu);

		// Create swap-chain
		final Swapchain swapchain = new Swapchain.Builder(dev, surface)
				.count(2)
				.clear(new Colour(0.1f, 0.1f, 0.1f, 1))
				.build();

		final View depth = depth(dev, Image.Extents.of(swapchain.extents()));

		// Create render pass
		final RenderPass pass = new RenderPass.Builder(dev)
				.attachment()
					.format(swapchain.format())
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
					.build()
				.attachment()
					.format(depth.image().descriptor().format())
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
					.build()
				.subpass()
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.depth(1)
					.build()
				.dependency(RenderPass.VK_SUBPASS_EXTERNAL, 0)
					.source().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destination().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destination().access(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
					.build()
				.build();

		// Load shaders
		final Path dir = new File("./src/test/resources/demo/model").toPath();
		final var src = DataSource.of(dir);
		final var loader = ResourceLoader.of(src, new ShaderLoader(dev));
		final Shader vert = loader.load("spv.chalet.vert");
		final Shader frag = loader.load("spv.chalet.frag");

		//////////////////

		// Load model
		/////
//		final Model obj = ResourceLoader.of(src, new ObjectModelLoader()).load("chalet.obj").iterator().next();
//		new ModelLoader().write(obj, new FileOutputStream(new File("./src/test/resources/demo/model/chalet.model")));
		/////
		final Model model = ResourceLoader.of(src, new ModelLoader()).load("chalet.model");

		// Load VBO
		final Command.Pool copyPool = Command.Pool.create(transfer.queue(dev)); //dev.queue(transfer));
		final VulkanBuffer vbo = loadBuffer(dev, model.vertexBuffer(), VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, copyPool);

		// Load IBO
		final VulkanBuffer index = loadBuffer(dev, model.indexBuffer().get(), VkBufferUsageFlag.VK_BUFFER_USAGE_INDEX_BUFFER_BIT, copyPool);

		//////////////////

		final Queue graphicsQueue = graphics.queue(dev); //dev.queue(graphics);
		final Command.Pool graphicsPool = Command.Pool.create(graphicsQueue);
		final View texture = texture(dev, graphicsPool);

		// Create descriptor layout
		final var samplerBinding = new DescriptorSet.Binding.Builder()
				.binding(0)
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
				.build();

		final var uniformBinding = new DescriptorSet.Binding.Builder()
				.binding(1)
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
				.build();

		final var layout = DescriptorSet.Layout.create(dev, List.of(samplerBinding, uniformBinding));

		// Create pool
		final DescriptorSet.Pool setPool = new DescriptorSet.Pool.Builder(dev)
				.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 2)
				.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 2)
				.max(2)
				.build();

		final List<DescriptorSet> descriptors = setPool.allocate(layout, swapchain.views().size());

		// Create sampler
		final Sampler sampler = new Sampler.Builder(dev)
				.anisotropy(16)
				.build();

		// Create uniform buffer for the projection matrix
		final VulkanBuffer uniform = new VulkanBuffer.Builder(dev)
				.length(Matrix4.IDENTITY.length())
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
				.build();

		// Create projection matrix
		final Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, swapchain.extents());

		// Init descriptor sets
		final DescriptorSet.Resource samplerResource = sampler.resource(texture);
		for(int n = 0; n < descriptors.size(); ++n) {
			final DescriptorSet ds = descriptors.get(n);
			ds.entry(samplerBinding).set(samplerResource);
			ds.entry(uniformBinding).set(uniform.uniform());
		}
		DescriptorSet.update(dev, descriptors);

		//////////////////

		// Create pipeline layout
		final Pipeline.Layout pipelineLayout = new Pipeline.Layout.Builder(dev)
				.add(layout)
				.build();

		// Create pipeline
		final Pipeline pipeline = new Pipeline.Builder(dev)
				.layout(pipelineLayout)
				.pass(pass)
				.input()
					.binding(model.header())
					.build()
				.assembly()
					.topology(model.header().primitive())
					.build()
				.viewport(swapchain.extents())
				.rasterizer()
					.clockwise(model.header().clockwise())
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
		final var buffers = swapchain
				.views()
				.stream()
				.map(v -> FrameBuffer.create(List.of(v, depth), pass))
				.collect(toList());

		// Create command pool
		final List<Command.Buffer> commands = graphicsPool.allocate(buffers.size());

		// Record render commands
		final Command draw = DrawCommand.of(model);
		for(int n = 0; n < commands.size(); ++n) {
			final Command.Buffer cb = commands.get(n);
			cb
				.begin()
					.add(pass.begin(buffers.get(n)))
					.add(pipeline.bind())
					.add(vbo.bindVertexBuffer())
					.add(index.bindIndexBuffer())
					.add(descriptors.get(n).bind(pipelineLayout))
					.add(draw)
					.add(RenderPass.END_COMMAND)
				.end();
		}

		///////////////////

		// Init camera
		final Camera cam = new Camera();

		// Init local model transform
		final Matrix rot = Matrix4.rotation(Vector.X_AXIS, -MathsUtil.HALF_PI);
		final Matrix trans = Matrix4.translation(new Vector(0, 0.5f, 0));
		final Matrix scale = Matrix4.scale(1);
		final Matrix modelMatrix = trans.multiply(rot).multiply(scale);

		/////////////////

		// Create action bindings
		final Bindings bindings = new Bindings();

		// Init mouse
		final var mouse = window.mouse();
		final var wheel = mouse.wheel();
		final var pointer = mouse.pointer();
		wheel.enable(bindings::accept);
		pointer.enable(bindings::accept);

		// Init keyboard
		final var keyboard = window.keyboard();
		keyboard.enable(bindings::accept);

		// Bind camera controller
		final OrbitalCameraController controller = new OrbitalCameraController(cam, swapchain.extents());
		controller.range(0.75f, 25);
		controller.scale(0.25f);
		controller.radius(3);
		bindings.bind(pointer, (PositionAction) controller::update);
		bindings.bind(wheel, (ValueAction) controller::zoom);

		/////////////////

		// Create render loop
		final IntFunction<Frame> factory = idx -> new Frame() {
			@Override
			public void render(FrameState state, View view) {
				state.render(commands.get(idx));
			}

			@Override
			public boolean update() {
				// Handle input events
				desktop.poll();

				// Update view matrix
				final Matrix matrix = proj.multiply(cam.matrix()).multiply(modelMatrix);
				uniform.load(matrix);

				return true;
			}
		};
		final Runner runner = new Runner(swapchain, 2, factory, present.queue(dev));

		// Bind run action
		//final StopAction stop = new StopAction();
		bindings.bind(Button.of("ESCAPE"), (SimpleAction) runner::stop); // TODO - stop action <-- runner

		// Start rendering
		runner.start();

		// Wait for pending work to complete
		dev.waitIdle();

		//////////////

		runner.destroy();

		texture.destroy();
		sampler.destroy();
		uniform.destroy();
		vbo.destroy();
		index.destroy();
		depth.destroy();

		setPool.destroy();
		layout.destroy();

		graphicsPool.destroy();
		copyPool.destroy();

		vert.destroy();
		frag.destroy();

		// Destroy render pass
		buffers.forEach(FrameBuffer::destroy);
		pass.destroy();

		// Destroy pipeline
		pipelineLayout.destroy();
		pipeline.destroy();
		swapchain.destroy();

		surface.destroy();
		window.destroy();
		desktop.destroy();

		// Destroy device
		dev.destroy();
		instance.destroy();
	}
}
