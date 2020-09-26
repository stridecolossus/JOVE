package org.sarge.jove.platform.vulkan;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
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

		// Init Vulkan
		final VulkanLibrary lib = VulkanLibrary.create();

		// Create instance
		final var builder = new Instance.Builder()
				.vulkan(lib)
				.name("test")
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.layer(ValidationLayer.STANDARD_VALIDATION);
		Arrays.stream(desktop.extensions()).forEach(builder::extension); // TODO - helper?
		final Instance instance = builder.build();

		// Lookup surface
		final Pointer surfaceHandle = window.surface(instance.handle(), PointerByReference::new);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(dev -> dev.families().stream().anyMatch(q -> q.isPresentationSupported(surfaceHandle)))
				.findAny()
				.orElseThrow(() -> new ServiceException("No GPU available"));

		// Lookup required queues
		final QueueFamily graphics = gpu.find(PhysicalDevice.filter(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT), "Graphics family not available");
		final QueueFamily transfer = gpu.find(PhysicalDevice.filter(VkQueueFlag.VK_QUEUE_TRANSFER_BIT), "Transfer family not available");

		// Create device
		// TODO - check for EXTENSION_DEBUG_UTILS at device level
		final LogicalDevice dev = new LogicalDevice.Builder()
				.parent(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.queue(graphics)
				.queue(transfer)
				.build();

		// Create rendering surface
		final Surface surface = new Surface(surfaceHandle, gpu);

		//////////////

		// Destroy window
		surface.destroy();
		window.destroy();
		desktop.close();

		// Destroy device
		dev.destroy();
		instance.destroy();
	}
}
