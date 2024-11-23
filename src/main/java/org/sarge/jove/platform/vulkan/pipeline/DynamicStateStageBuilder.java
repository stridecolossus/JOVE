package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.BitMask;

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
		states.add(requireNonNull(state));
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
		info.pDynamicStates = array; // TODO new PointerToIntArray(array);

		return info;
	}

	/**
	 * Dynamic pipeline state library.
	 */
	interface Library {
		/**
		 * Sets dynamic viewports.
		 * @param commandBuffer			Command buffer
		 * @param firstViewport			Index of the first viewport
		 * @param viewportCount			Number of viewports
		 * @param pViewports			Dynamic viewports
		 */
		void vkCmdSetViewport(Buffer commandBuffer, int firstViewport, int viewportCount, VkViewport pViewports);

		/**
		 * Set dynamic scissor rectangles.
		 * @param commandBuffer			Command buffer
		 * @param firstScissor			Index of the first scissor
		 * @param scissorCount			Number of scissors
		 * @param pScissors				Dynamic scissors
		 */
		void vkCmdSetScissor(Buffer commandBuffer, int firstScissor, int scissorCount, VkRect2D pScissors);

		/**
		 * Sets the line width.
		 * @param commandBuffer			Command buffer
		 * @param lineWidth				Line width
		 */
		void vkCmdSetLineWidth(Buffer commandBuffer, float lineWidth);

		// TODO
		void vkCmdSetDepthBias(Buffer commandBuffer, float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor);

		/**
		 * Sets the colour blend constants.
		 * @param commandBuffer			Command buffer
		 * @param blendConstants		Blend constants
		 */
		void vkCmdSetBlendConstants(Buffer commandBuffer, float blendConstants[]);

		/**
		 * Sets the bounds for the depth test.
		 * @param commandBuffer			Command buffer
		 * @param minDepthBounds		Minimum depth
		 * @param maxDepthBounds		Maximum depth
		 */
		void vkCmdSetDepthBounds(Buffer commandBuffer, float minDepthBounds, float maxDepthBounds);

		/**
		 * Sets the stencil compare mask.
		 * @param commandBuffer			Command buffer
		 * @param faceMask				Face flags
		 * @param compareMask			Compare mask
		 */
		void vkCmdSetStencilCompareMask(Buffer commandBuffer, BitMask<VkStencilFaceFlag> faceMask, int compareMask);

		/**
		 * Sets the stencil write mask.
		 * @param commandBuffer			Command buffer
		 * @param faceMask				Face flags
		 * @param writeMask				Write mask
		 */
		void vkCmdSetStencilWriteMask(Buffer commandBuffer, BitMask<VkStencilFaceFlag> faceMask, int writeMask);

		/**
		 * Sets the stencil reference.
		 * @param commandBuffer			Command buffer
		 * @param faceMask				Face flags
		 * @param reference				Reference value
		 */
		void vkCmdSetStencilReference(Buffer commandBuffer, BitMask<VkStencilFaceFlag> faceMask, int reference);
	}
}
