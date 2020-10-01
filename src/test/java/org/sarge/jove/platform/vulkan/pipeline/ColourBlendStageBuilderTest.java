package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatArrayEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkBlendFactor;
import org.sarge.jove.platform.vulkan.VkBlendOp;
import org.sarge.jove.platform.vulkan.VkLogicOp;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

public class ColourBlendStageBuilderTest {
	private static final float[] CONSTANTS = {1, 2, 3, 4};

	private ColourBlendStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new ColourBlendStageBuilder();
	}

	@Test
	void create() {
		// Build descriptor
		final var info = builder
				.logic(VkLogicOp.VK_LOGIC_OP_COPY)
				.constants(CONSTANTS)
				.attachment()
					.colour().sourceBlendFactor(VkBlendFactor.VK_BLEND_FACTOR_CONSTANT_COLOR)
					.alpha().operation(VkBlendOp.VK_BLEND_OP_BLUE_EXT)
					.build()
				.attachment()
					.build()
				.result();

		// Check descriptor
		assertNotNull(info);
		assertEquals(VulkanBoolean.TRUE, info.logicOpEnable);
		assertEquals(VkLogicOp.VK_LOGIC_OP_COPY, info.logicOp);
		assertFloatArrayEquals(CONSTANTS, info.blendConstants);
		assertEquals(2, info.attachmentCount);
		assertNotNull(info.pAttachments);

		// TODO - how to test attachments?
	}

	@Test
	void createDefaults() {
		final var info = builder.result();
		assertNotNull(info);
		assertEquals(VulkanBoolean.FALSE, info.logicOpEnable);
		assertEquals(VkLogicOp.VK_LOGIC_OP_NO_OP, info.logicOp);
		assertFloatArrayEquals(new float[]{0, 0, 0, 0}, info.blendConstants);
		assertEquals(1, info.attachmentCount);
		assertNotNull(info.pAttachments);
	}
}