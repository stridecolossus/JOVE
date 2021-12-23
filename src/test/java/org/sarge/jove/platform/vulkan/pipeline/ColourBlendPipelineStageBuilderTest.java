package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkBlendFactor;
import org.sarge.jove.platform.vulkan.VkBlendOp;
import org.sarge.jove.platform.vulkan.VkLogicOp;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

public class ColourBlendPipelineStageBuilderTest {
	private static final float[] CONSTANTS = {1, 2, 3, 4};

	private ColourBlendPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new ColourBlendPipelineStageBuilder();
	}

	@Test
	void create() {
		// Build descriptor
		final var info = builder
				.operation(VkLogicOp.COPY)
				.constants(CONSTANTS)
				.attachment()
					.colour().source(VkBlendFactor.CONSTANT_COLOR)
					.alpha().operation(VkBlendOp.BLUE_EXT)
					.mask("RGBA")
					.build()
				.attachment()
					.build()
				.get();

		// Check descriptor
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VulkanBoolean.TRUE, info.logicOpEnable);
		assertEquals(VkLogicOp.COPY, info.logicOp);
		assertArrayEquals(CONSTANTS, info.blendConstants);
		assertEquals(2, info.attachmentCount);
		assertNotNull(info.pAttachments);

		// TODO - how to test attachments?
	}

	@Test
	void createDefaults() {
		final var info = builder.get();
		assertNotNull(info);
		assertEquals(VulkanBoolean.FALSE, info.logicOpEnable);
		assertEquals(VkLogicOp.NO_OP, info.logicOp);
		assertArrayEquals(new float[]{0, 0, 0, 0}, info.blendConstants);
		assertEquals(1, info.attachmentCount);
		assertNotNull(info.pAttachments);
	}

	@Test
	void invalidColourWriteMask() {
		assertThrows(IllegalArgumentException.class, () -> builder.attachment().mask("cobblers"));
	}
}
