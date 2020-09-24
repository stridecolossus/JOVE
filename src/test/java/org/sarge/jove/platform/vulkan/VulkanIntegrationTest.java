package org.sarge.jove.platform.vulkan;

public class VulkanIntegrationTest {



	public void run() {
		System.out.println("Initialising Vulkan API");
		final Vulkan vulkan = new Vulkan(VulkanLibrary.create());

		System.out.println("Creating instance");
		final Instance instance = new Instance.Builder(vulkan)
				.name("demo")
				.extension(Vulkan.DEBUG_UTILS)
				//.extension(Vulkan.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();

		System.out.println("Enumerating physical devices");
		final var devices = PhysicalDevice.devices(instance);

		System.out.println(devices);
	}


	public static void main(String[] args) {
		new VulkanIntegrationTest().run();
	}
}
