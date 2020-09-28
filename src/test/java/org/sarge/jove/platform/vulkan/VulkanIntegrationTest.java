package org.sarge.jove.platform.vulkan;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.DesktopService;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.Window;
import org.sarge.jove.platform.glfw.FrameworkDesktopService;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class VulkanIntegrationTest {
	@Test
	void test() {
		// Open desktop
		final DesktopService desktop = FrameworkDesktopService.create();
		if(!desktop.isVulkanSupported()) throw new ServiceException("Vulkan not supported");

		// Create window
		final var descriptor = new Window.Descriptor.Builder()
				.title("demo")
				.size(new Dimensions(1280, 760))
				.property(Window.Descriptor.Property.DISABLE_OPENGL)
				.build();
		final Window window = desktop.window(descriptor);
		// TODO - any point in separate Window class? does it help at all?

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
		final Pointer surfaceHandle = window.surface(instance.handle(), PointerByReference::new);

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
				.orElseThrow(() -> new ServiceException("No GPU available"));

		// Lookup required queues
		final QueueFamily graphics = gpu.find(graphicsPredicate, "Graphics family not available");
		final QueueFamily transfer = gpu.find(transferPredicate, "Transfer family not available");

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder()
				.parent(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.queue(graphics)
				.queue(transfer)
				.build();

		// Create rendering surface
		final Surface surface = new Surface(surfaceHandle, dev);

		// Specify required image format
		final VkFormat format = new FormatBuilder()
				.components(FormatBuilder.BGRA)
				.bytes(1)
				.signed(false)
				.type(Vertex.Component.Type.NORM)
				.build();

		// Create swap-chain
		final SwapChain chain = new SwapChain.Builder(surface)
				.format(format)
				.count(2)
				.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				.build();

		//////////////

		// Destroy window
		surface.destroy();
		window.destroy();
		desktop.close();

		// Destroy Vulkan objects
		chain.destroy();
		dev.destroy();
		instance.destroy();
	}
}
