package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

class AssemblyPipelineStageBuilderTest {
	private AssemblyPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new AssemblyPipelineStageBuilder();
	}

	@Test
	void build() {
		// Build descriptor
		final VkPipelineInputAssemblyStateCreateInfo info = builder
				.topology(Primitive.LINES)
				.restart(true)
				.get();

		// Check descriptor
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VkPrimitiveTopology.LINE_LIST, info.topology);
		assertEquals(VulkanBoolean.TRUE, info.primitiveRestartEnable);
	}

	@Test
	void buildDefault() {
		final VkPipelineInputAssemblyStateCreateInfo info = builder.get();
		assertNotNull(info);
		assertEquals(VkPrimitiveTopology.TRIANGLE_STRIP, info.topology);
		assertEquals(VulkanBoolean.FALSE, info.primitiveRestartEnable);
	}

	@Test
	void copy() {
		final AssemblyPipelineStageBuilder copy = new AssemblyPipelineStageBuilder();
		copy.copy(builder);
		assertNotNull(copy);
		assertNotNull(copy.get());
		assertNotSame(builder.get(), copy.get());
	}
}
