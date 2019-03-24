package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.common.ScreenCoordinate;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.DesktopService;
import org.sarge.jove.platform.Device;
import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.Service;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.Window;
import org.sarge.jove.platform.glfw.FrameworkDesktopService;
import org.sarge.jove.platform.vulkan.Feature.Extension;
import org.sarge.jove.platform.vulkan.Feature.ValidationLayer;
import org.sarge.jove.platform.vulkan.FrameState.FrameTracker;
import org.sarge.jove.platform.vulkan.FrameState.FrameTracker.DefaultFrameTracker;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;

import com.sun.jna.Pointer;

public class VulkanIntegrationTest {

	private Vulkan vulkan;
	private VulkanLibrary lib;
	private LogicalDevice dev;

	public static void main(String[] args) throws Exception {
		final VulkanIntegrationTest test = new VulkanIntegrationTest();
		test.run();
	}

	public void run() throws Exception {
		// Init Vulkan
		System.out.println("Initialising Vulkan");
		Vulkan.init();
		vulkan = Vulkan.instance();
		lib = vulkan.library();

		System.out.println("Creating instance");
		final VulkanInstance instance = instance();

		System.out.println("Initialising debug handler");
		instance.handlerFactory()
			.builder()
			.init()
			//.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT)
			.build();

		final PhysicalDevice physical = physical(instance);

		// Create window and surface
		final DesktopService desktop = desktop();
		final Window window = window(desktop);
		final Surface surface = surface(instance, physical, desktop, window);

		// Create logical device with appropriate queues
		final QueueFamily family = family(physical, surface);
		dev = logical(physical, family);

		final SwapChain chain = chain(dev, surface);

		System.out.println("Creating shaders");
		final VulkanShader vert = VulkanShader.create(dev, Files.readAllBytes(new File("src/test/resources/triangle.vert.spv").toPath()));
		final VulkanShader frag = VulkanShader.create(dev, Files.readAllBytes(new File("src/test/resources/triangle.frag.spv").toPath()));

		final RenderPass pass = pass();

		final Pipeline pipeline = pipeline(vert, frag, chain.extent(), pass);

		System.out.println("Creating command pool");
		final Command.Pool pool = Command.Pool.create(dev, family);
		final List<Command.Buffer> cmds = pool.allocate(3, true);

		// TODO - how to wrap 3 x FB and 3 x commands -> object?
		System.out.println("Creating frame buffer and commands");
		final FrameBuffer[] frameBuffers = new FrameBuffer[cmds.size()];
		for(int n = 0; n < cmds.size(); ++n) {
			final FrameBuffer fb = frameBuffer(pass, chain.extent(), chain.images().get(n));
			frameBuffers[n] = fb;

			final Command.Buffer cb = cmds.get(n);
			record(cb, fb, pass, pipeline);
		}

		System.out.println("Creating frame tracker");
		final WorkQueue queue = dev.queue(family, 0);
		final FrameTracker tracker = new DefaultFrameTracker(dev, 2, queue);

		final AtomicBoolean running = new AtomicBoolean(true);

		final Event.Handler hdlr = event -> {
			if(event.key().identifier() == 256) {
				running.set(false);
			}
		};
		final Device<?> input = window.device();
		input.bind(Event.Category.BUTTON, hdlr);

		// Start render loop
		System.out.println("Starting render loop...");
		while(running.get()) {
			//
			final FrameState frame = tracker.waitReady();

			// Acquire next image
			final int index = chain.next(frame.available(), null);

			// Submit render command
			final Command.Buffer cmd = cmds.get(index);
			final FrameState next = tracker.submit(cmd);

			// Present next image
			chain.present(next, queue);

			// Poll input events
			window.poll();
		}

		/////

		System.out.println("Waiting...");
		lib.vkDeviceWaitIdle(dev.handle());

		System.out.println("Cleaning up...");

		tracker.destroy();

		pool.destroy();
		pipeline.destroy();
		pass.destroy();

		vert.destroy();
		frag.destroy();

		for(FrameBuffer fb : frameBuffers) {
			fb.destroy();
		}

		chain.destroy();
		surface.destroy();
		window.destroy();
		desktop.close();

		dev.destroy();
		instance.destroy();

		System.out.println("Finished");
	}

	// https://www.eshayne.com/jnaex/index.html?example=10
	// https://stackoverflow.com/questions/10109723/using-jna-to-access-a-struct-containing-an-array-of-structs

// missing???
//	1 [VK_KHX_device_group_creation]
//	1 [VK_KHX_external_semaphore_capabilities]

// instance extensions
//	VK_EXT_debug_report
//	VK_KHR_win32_surface
//	VK_KHR_surface
//	VK_EXT_display_surface_counter
//	VK_NV_external_memory_capabilities
//	VK_KHR_get_physical_device_properties2
//	VK_EXT_debug_utils

// instance layers
//	VK_LAYER_NV_optimus 1
//	VK_LAYER_LUNARG_vktrace 1
//	VK_LAYER_GOOGLE_threading 1
//	VK_LAYER_LUNARG_device_simulation 1
//	VK_LAYER_LUNARG_screenshot 1
//	VK_LAYER_LUNARG_parameter_validation 1
//	VK_LAYER_VALVE_steam_fossilize 1
//	VK_LAYER_GOOGLE_unique_objects 1
//	VK_LAYER_VALVE_steam_overlay 1
//	VK_LAYER_LUNARG_core_validation 1
//	VK_LAYER_LUNARG_object_tracker 1
//	VK_LAYER_LUNARG_api_dump 2
//	VK_LAYER_LUNARG_standard_validation 1
//	VK_LAYER_LUNARG_monitor 1
//	VK_LAYER_LUNARG_assistant_layer 1

// device extensions
//VK_KHR_swapchain,
//VK_NV_win32_keyed_mutex,
//VK_KHX_external_memory,
//VK_KHX_external_memory_win32,
//VK_KHX_device_group,
//VK_KHX_external_semaphore_win32,
//VK_EXT_shader_subgroup_vote,
//VK_KHR_descriptor_update_template,
//VK_NV_glsl_shader,
//VK_KHX_multiview,
//VK_NV_dedicated_allocation,
//VK_KHR_sampler_mirror_clamp_to_edge,
//VK_NV_viewport_array2,
//VK_NV_external_memory,
//VK_KHX_external_semaphore,
//VK_NVX_multiview_per_view_attributes,
//VK_NV_sample_mask_override_coverage,
//VK_NV_geometry_shader_passthrough,
//VK_KHR_push_descriptor,
//VK_KHX_win32_keyed_mutex,
//VK_NV_external_memory_win32,
//VK_EXT_discard_rectangles,
//VK_NVX_device_generated_commands,
//VK_NV_viewport_swizzle,
//VK_EXT_shader_subgroup_ballot,
//VK_KHR_shader_draw_parameters,
//VK_KHR_maintenance1

// device layers
//VK_LAYER_GOOGLE_threading,
//VK_LAYER_VALVE_steam_overlay,
//VK_LAYER_LUNARG_object_tracker,
//VK_LAYER_LUNARG_standard_validation,
//VK_LAYER_LUNARG_parameter_validation,
//VK_LAYER_GOOGLE_unique_objects,
//VK_LAYER_NV_optimus,
//VK_LAYER_LUNARG_core_validation

	/**
	 * Initialises the vulkan instance.
	 */
	private VulkanInstance instance() {
		// Create builder
		System.out.println("Creating instance builder");
		final VulkanInstance.Builder builder = new VulkanInstance.Builder(vulkan)
			.extension("VK_KHR_surface")
			.extension("VK_KHR_win32_surface")
			.extension(Extension.DEBUG_UTILS)
			.layer(ValidationLayer.STANDARD_VALIDATION)
			.layer("VK_LAYER_VALVE_steam_overlay", 1);

		// Create instance
		System.out.println("Creating instance");
		final VulkanInstance instance = builder.build();
		assertNotNull(instance);

		return instance;
	}

	private PhysicalDevice physical(VulkanInstance instance) {
		System.out.println("Enumerating physical devices");
		final var devices = PhysicalDevice.create(instance);
		for(PhysicalDevice dev : devices) {
			// TODO
			//if(!dev.features().geometryShader) continue;
			return dev;
		}
		throw new ServiceException("No suitable device");
	}

	private QueueFamily family(PhysicalDevice dev, Surface surface) {
		System.out.println("Enumerating queue families");
		final var families = dev.families();
		for(QueueFamily family : families) {
			// TODO - assumes same family for both, should select family for each requirement
			if(!family.flags().contains(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT)) continue;
			if(!family.isPresentationSupported(surface)) continue;
			return family;
		}
		throw new ServiceException("No suitable queue");
	}

	private LogicalDevice logical(PhysicalDevice physical, QueueFamily family) {
		System.out.println("Creating logical device");

		final VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
		features.geometryShader = VulkanBoolean.TRUE;

		final LogicalDevice dev = new LogicalDevice.Builder(physical)
			.queue(family)
			// TODO - others?
			.extension(Extension.SWAP_CHAIN)
			.layer(ValidationLayer.STANDARD_VALIDATION)
			.layer("VK_LAYER_VALVE_steam_overlay", 1)
			.features(features)
			.build();

		dev.queues();

		return dev;
	}

	private DesktopService desktop() {
		System.out.println("Initialised desktop service");
		final DesktopService service = FrameworkDesktopService.create();
		if(!service.isVulkanSupported()) throw new Service.ServiceException("Vulkan not supported");
		return service;
	}

	private Window window(DesktopService service) {
		System.out.println("Creating window");
		final Window.Properties props = new Window.Properties("demo", new Dimensions(640, 480), null);
		// TODO - glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); -> properties
		final Window window = service.window(props);
		return window;
	}

	private Surface surface(VulkanInstance instance, PhysicalDevice dev, DesktopService service, Window window) {
		System.out.println("Creating surface");
		final Handle handle = (Handle) window;
		final Pointer ptr = service.surface(instance.handle(), handle.handle());
		return Surface.create(ptr, dev);
	}

	private SwapChain chain(LogicalDevice dev, Surface surface) {
		System.out.println("Creating swap-chain");
		return new SwapChain.Builder(dev, surface)
			.format(VkFormat.VK_FORMAT_B8G8R8A8_UNORM)
			.build();
	}

	private RenderPass pass() {
		System.out.println("Creating render pass");
		return new RenderPass.Builder(dev)
			.attachment()
				.format(VkFormat.VK_FORMAT_B8G8R8A8_UNORM)
				.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
				.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
				.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
				.build()
			.subpass()
				.attachment(0)			// TODO - or move this into the attachment builder somehow?
				.dependency()
					.sourceStage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destinationStage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destinationAccess(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT)
					.destinationAccess(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
					.build()
				.build()
			.build();
	}

	private FrameBuffer frameBuffer(RenderPass pass, Dimensions extent, ImageView view) {
		return new FrameBuffer.Builder(dev, pass)
			.extent(extent)
			.view(view)
			.build();
	}

	private Pipeline pipeline(VulkanShader vert, VulkanShader frag, Dimensions extent, RenderPass pass) {
		System.out.println("Creating pipeline");
		final Rectangle rect = new Rectangle(new ScreenCoordinate(0, 0), extent);
		return new Pipeline.Builder(dev, pass)
			.shader()
				.module(vert)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
				.build()
			.shader()
				.module(frag)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
				.build()
			.viewport()
				.viewport(rect)
				.scissor(rect)
				.build()
			.build();
	}

	private void record(Command.Buffer buffer, FrameBuffer fb, RenderPass pass, Pipeline pipeline) {
		System.out.println("Recording command");

		final Rectangle extent = new Rectangle(0, 0, 640, 480);
		final Colour[] clear = {new Colour(0.3f, 0.3f, 0.3f, 1)};

		// TODO - created from VBO?
		final Command draw = (api, cb) -> api.vkCmdDraw(cb, 3, 1, 0, 0);

		buffer
			.begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT)
			.add(pass.begin(fb, extent, clear))
			.add(pipeline.bind())
			.add(draw)
			.add(RenderPass.END_COMMAND)
			.end();
	}
}
