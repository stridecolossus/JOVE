package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.pipeline.ColourBlendStage.ColourBlendAttachment;
import org.sarge.jove.util.EnumMask;

class ColourBlendStageTest {
	private ColourBlendStage stage;

	@BeforeEach
	void before() {
		stage = new ColourBlendStage();
	}

	@DisplayName("The default colour write mask enables all channels")
	@Test
	void mask() {
		final var expected = "RGBA"
				.chars()
				.mapToObj(n -> (char) n)
				.map(String::valueOf)
				.map(VkColorComponentFlags::valueOf)
				.collect(toSet());

		assertEquals(expected, ColourBlendAttachment.DEFAULT_WRITE_MASK);
	}

	@DisplayName("The global blend can be enabled essentially disabling any per-attachment configuration")
	@Test
	void global() {
		final VkPipelineColorBlendStateCreateInfo descriptor = stage.operation(VkLogicOp.COPY).descriptor();
		assertEquals(true, descriptor.logicOpEnable);
		assertEquals(VkLogicOp.COPY, descriptor.logicOp);
		assertArrayEquals(new float[]{1, 1, 1, 1}, descriptor.blendConstants);
		assertEquals(1, descriptor.attachmentCount);
		assertEquals(false, descriptor.pAttachments[0].blendEnable);
	}

	@DisplayName("The blend configuration can be configured for each colour attachment")
	@Test
	void attachment() {
		// Add explicit configuration for a colour attachment
		final var attachment = new ColourBlendAttachment.Builder().build();
		stage.add(attachment);

		// Check descriptor
		final VkPipelineColorBlendStateCreateInfo descriptor = stage.descriptor();
		assertEquals(false, descriptor.logicOpEnable);
		assertEquals(1, descriptor.attachmentCount);

		// Check attachment
		final VkPipelineColorBlendAttachmentState state = descriptor.pAttachments[0];
		assertEquals(true, state.blendEnable);
		assertEquals(new EnumMask<>(ColourBlendAttachment.DEFAULT_WRITE_MASK), state.colorWriteMask);

		// Check colour channels
		assertEquals(VkBlendFactor.SRC_ALPHA, state.srcColorBlendFactor);
		assertEquals(VkBlendFactor.ONE_MINUS_SRC_ALPHA, state.dstColorBlendFactor);
		assertEquals(VkBlendOp.ADD, state.colorBlendOp);

		// Check alpha channel
		assertEquals(VkBlendFactor.ONE, state.srcAlphaBlendFactor);
		assertEquals(VkBlendFactor.ZERO, state.dstAlphaBlendFactor);
		assertEquals(VkBlendOp.ADD, state.alphaBlendOp);
	}

	@DisplayName("A disabled attachment is injected if none have been configured")
	@Test
	void unspecified() {
		// Check descriptor
		final VkPipelineColorBlendStateCreateInfo descriptor = stage.descriptor();
		assertEquals(false, descriptor.logicOpEnable);
		assertEquals(1, descriptor.attachmentCount);

		// Check attachment
		final VkPipelineColorBlendAttachmentState state = descriptor.pAttachments[0];
		assertEquals(false, state.blendEnable);
		assertEquals(new EnumMask<>(ColourBlendAttachment.DEFAULT_WRITE_MASK), state.colorWriteMask);
	}
}
