package org.sarge.jove.demo;

import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.Instance;
import org.sarge.jove.platform.vulkan.core.Instance.Handler;

/**
 * This demo illustrates creating a Vulkan instance, including retrieving the required extensions for the execution platform and attaching a debugging handler.
 * @author Sarge
 */
public class InstanceDemo {
	public static void main(String[] args) throws Exception {
		// Init
		ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);

		// Open desktop
		final Desktop desktop = Desktop.create();
		if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");
		System.out.println("desktop=" + desktop);

		// Lookup required extensions
		final String[] extensions = desktop.extensions();
		System.out.println("extensions=" + Arrays.toString(extensions));

		// Init Vulkan
		final VulkanLibrary lib = VulkanLibrary.create();
		System.out.println("library=" + lib);

		// Create instance
		final Instance instance = new Instance.Builder()
				.name("InstanceDemo")
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.extensions(desktop.extensions())
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build(lib);

		// Attach message handler
		new Handler().init().attach(instance);
		System.out.println("instance=" + instance);

		// Cleanup
		instance.destroy();
		desktop.destroy();
	}
}
