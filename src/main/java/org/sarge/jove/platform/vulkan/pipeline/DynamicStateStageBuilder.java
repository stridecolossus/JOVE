package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.IntegerArray;

/**
 * Builder for the dynamic state pipeline stage.
 * @see VkPipelineDynamicStateCreateInfo
 * @author Sarge
 */
public class DynamicStateStageBuilder extends AbstractStageBuilder<VkPipelineDynamicStateCreateInfo> {
	private final Set<VkDynamicState> states = new HashSet<>();

	/**
	 * Adds a dynamic state.
	 * @param state Dynamic state
	 */
	public DynamicStateStageBuilder state(VkDynamicState state) {
		states.add(notNull(state));
		return this;
	}

	@Override
	VkPipelineDynamicStateCreateInfo get() {
		// Ignore if no dynamic state
		if(states.isEmpty()) {
			return null;
		}

		// Init descriptor
		final var info = new VkPipelineDynamicStateCreateInfo();
		info.flags = 0;		// Reserved

		// Populate dynamic states
		final int[] array = states.stream().mapToInt(VkDynamicState::value).toArray();
		info.dynamicStateCount = array.length;
		info.pDynamicStates = new IntegerArray(array);

		return info;
	}

	/**
	 * Dynamic pipeline state library.
	 */
	interface Library {
		/**
		 * Dynamically sets viewports.
		 * @param commandBuffer			Command buffer
		 * @param firstViewport			Index of the first viewport
		 * @param viewportCount			Number of viewports
		 * @param pViewports			Dynamic viewports
		 */
		void vkCmdSetViewport(Buffer commandBuffer, int firstViewport, int viewportCount, VkViewport pViewports);

		/**
		 * Dynamically sets scissor rectangles.
		 * @param commandBuffer			Command buffer
		 * @param firstScissor			Index of the first scissor
		 * @param scissorCount			Number of scissors
		 * @param pScissors				Dynamic scissors
		 */
		void vkCmdSetScissor(Buffer commandBuffer, int firstScissor, int scissorCount, VkRect2D pScissors);

		/**
		 * Dynamically sets the line width.
		 * @param commandBuffer			Command buffer
		 * @param lineWidth				Line width
		 */
		void vkCmdSetLineWidth(Buffer commandBuffer, float lineWidth);

		// TODO
		void vkCmdSetDepthBias(Buffer commandBuffer, float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor);

		/**
		 * Dynamically sets the blend constants.
		 * @param commandBuffer			Command buffer
		 * @param blendConstants		Blend constants
		 */
		void vkCmdSetBlendConstants(Buffer commandBuffer, float blendConstants[]);

		// TODO
		void vkCmdSetDepthBounds(Buffer commandBuffer, float minDepthBounds, float maxDepthBounds);

		// TODO
		void vkCmdSetStencilCompareMask(Buffer commandBuffer, VkStencilFaceFlag faceMask, int compareMask);

		// TODO
		void vkCmdSetStencilWriteMask(Buffer commandBuffer, VkStencilFaceFlag faceMask, int writeMask);

		// TODO
		void vkCmdSetStencilReference(Buffer commandBuffer, VkStencilFaceFlag faceMask, int reference);
	}
}
