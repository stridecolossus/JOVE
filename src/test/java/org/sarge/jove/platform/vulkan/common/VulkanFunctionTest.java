package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.common.VulkanFunction;

class VulkanFunctionTest {
	@Test
	void invoke() {
		final VulkanFunction<String[]> function = (count, data) -> {
			if(data == null) {
				count.set(1);
			}
			else {
				data[0] = "string";
			}
		};
		assertArrayEquals(new String[]{"string"}, VulkanFunction.invoke(function, String[]::new));
	}
}
