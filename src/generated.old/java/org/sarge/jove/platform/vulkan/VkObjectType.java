package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkObjectType implements IntegerEnumeration {
 	VK_OBJECT_TYPE_UNKNOWN(0), 	
 	VK_OBJECT_TYPE_INSTANCE(1), 	
 	VK_OBJECT_TYPE_PHYSICAL_DEVICE(2), 	
 	VK_OBJECT_TYPE_DEVICE(3), 	
 	VK_OBJECT_TYPE_QUEUE(4), 	
 	VK_OBJECT_TYPE_SEMAPHORE(5), 	
 	VK_OBJECT_TYPE_COMMAND_BUFFER(6), 	
 	VK_OBJECT_TYPE_FENCE(7), 	
 	VK_OBJECT_TYPE_DEVICE_MEMORY(8), 	
 	VK_OBJECT_TYPE_BUFFER(9), 	
 	VK_OBJECT_TYPE_IMAGE(10), 	
 	VK_OBJECT_TYPE_EVENT(11), 	
 	VK_OBJECT_TYPE_QUERY_POOL(12), 	
 	VK_OBJECT_TYPE_BUFFER_VIEW(13), 	
 	VK_OBJECT_TYPE_IMAGE_VIEW(14), 	
 	VK_OBJECT_TYPE_SHADER_MODULE(15), 	
 	VK_OBJECT_TYPE_PIPELINE_CACHE(16), 	
 	VK_OBJECT_TYPE_PIPELINE_LAYOUT(17), 	
 	VK_OBJECT_TYPE_RENDER_PASS(18), 	
 	VK_OBJECT_TYPE_PIPELINE(19), 	
 	VK_OBJECT_TYPE_DESCRIPTOR_SET_LAYOUT(20), 	
 	VK_OBJECT_TYPE_SAMPLER(21), 	
 	VK_OBJECT_TYPE_DESCRIPTOR_POOL(22), 	
 	VK_OBJECT_TYPE_DESCRIPTOR_SET(23), 	
 	VK_OBJECT_TYPE_FRAMEBUFFER(24), 	
 	VK_OBJECT_TYPE_COMMAND_POOL(25), 	
 	VK_OBJECT_TYPE_SAMPLER_YCBCR_CONVERSION(1000156000), 	
 	VK_OBJECT_TYPE_DESCRIPTOR_UPDATE_TEMPLATE(1000085000), 	
 	VK_OBJECT_TYPE_SURFACE_KHR(1000000000), 	
 	VK_OBJECT_TYPE_SWAPCHAIN_KHR(1000001000), 	
 	VK_OBJECT_TYPE_DISPLAY_KHR(1000002000), 	
 	VK_OBJECT_TYPE_DISPLAY_MODE_KHR(1000002001), 	
 	VK_OBJECT_TYPE_DEBUG_REPORT_CALLBACK_EXT(1000011000), 	
 	VK_OBJECT_TYPE_OBJECT_TABLE_NVX(1000086000), 	
 	VK_OBJECT_TYPE_INDIRECT_COMMANDS_LAYOUT_NVX(1000086001), 	
 	VK_OBJECT_TYPE_DEBUG_UTILS_MESSENGER_EXT(1000128000), 	
 	VK_OBJECT_TYPE_VALIDATION_CACHE_EXT(1000160000), 	
 	VK_OBJECT_TYPE_ACCELERATION_STRUCTURE_NV(1000165000), 	
 	VK_OBJECT_TYPE_DESCRIPTOR_UPDATE_TEMPLATE_KHR(1000085000), 	
 	VK_OBJECT_TYPE_SAMPLER_YCBCR_CONVERSION_KHR(1000156000), 	
 	VK_OBJECT_TYPE_BEGIN_RANGE(0), 	
 	VK_OBJECT_TYPE_END_RANGE(25), 	
 	VK_OBJECT_TYPE_RANGE_SIZE(26), 	
 	VK_OBJECT_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkObjectType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
