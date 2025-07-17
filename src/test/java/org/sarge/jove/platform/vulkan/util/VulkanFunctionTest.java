package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

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
