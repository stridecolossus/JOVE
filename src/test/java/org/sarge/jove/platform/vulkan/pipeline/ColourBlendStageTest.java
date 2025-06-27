package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.pipeline.ColourBlendStage.*;
import org.sarge.jove.util.EnumMask;

public class ColourBlendStageTest {
	private ColourBlendStage stage;

	@BeforeEach
	void before() {
		stage = new ColourBlendStage();
	}

	@Test
	void empty() {
		final VkPipelineColorBlendStateCreateInfo info = stage.descriptor();
		assertEquals(false, info.logicOpEnable);
		assertEquals(VkLogicOp.COPY, info.logicOp);
		assertEquals(1, info.pAttachments.length);

		final VkPipelineColorBlendAttachmentState attachment = info.pAttachments[0];
		assertEquals(false, attachment.blendEnable);

		final var constants = new float[4];
		Arrays.fill(constants, 1);
		assertArrayEquals(constants, info.blendConstants);

	}

	@Test
	void constants() {
		final float[] constants = new float[4];
		stage.constants(constants);
		assertArrayEquals(constants, stage.descriptor().blendConstants);
	}

	@Test
	void attachment() {
		final VkPipelineColorBlendStateCreateInfo info = stage
				.add(new ColourBlendAttachment(BlendOperation.colour(), BlendOperation.alpha(), ColourBlendAttachment.DEFAULT_WRITE_MASK))
				.descriptor();

		assertEquals(1, info.pAttachments.length);

		final VkPipelineColorBlendAttachmentState attachment = info.pAttachments[0];
		assertEquals(true, attachment.blendEnable);
	}

	@Nested
	class BlendOperationTest {
		@Test
		void colour() {
			final var colour = new BlendOperation(VkBlendFactor.SRC_ALPHA, VkBlendOp.ADD, VkBlendFactor.ONE_MINUS_SRC_ALPHA);
			assertEquals(colour, BlendOperation.colour());
		}

		@Test
		void alpha() {
			final var alpha = new BlendOperation(VkBlendFactor.ONE, VkBlendOp.ADD, VkBlendFactor.ZERO);
			assertEquals(alpha, BlendOperation.alpha());
		}
	}

	@Nested
	class ColourBlendAttachmentTest {
		private ColourBlendAttachment.Builder builder;

		@BeforeEach
		void before() {
			builder = new ColourBlendAttachment.Builder();
		}

		@Test
		void populate() {
			final VkPipelineColorBlendAttachmentState descriptor = builder
					.colour(BlendOperation.colour())
					.alpha(BlendOperation.alpha())
					.mask("R")
					.build()
					.populate();

			assertEquals(true, descriptor.blendEnable);
			assertEquals(VkBlendFactor.SRC_ALPHA, descriptor.srcColorBlendFactor);
			assertEquals(VkBlendFactor.ONE_MINUS_SRC_ALPHA, descriptor.dstColorBlendFactor);
			assertEquals(VkBlendOp.ADD, descriptor.colorBlendOp);
			assertEquals(VkBlendFactor.ONE, descriptor.srcAlphaBlendFactor);
			assertEquals(VkBlendFactor.ZERO, descriptor.dstAlphaBlendFactor);
			assertEquals(VkBlendOp.ADD, descriptor.alphaBlendOp);
			assertEquals(new EnumMask<>(VkColorComponent.R), descriptor.colorWriteMask);
		}
	}
}
