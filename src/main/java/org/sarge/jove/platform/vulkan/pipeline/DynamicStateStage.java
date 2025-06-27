package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.CommandBuffer;
import org.sarge.jove.util.EnumMask;

/**
 * Builder for the dynamic state pipeline stage.
 * @see VkPipelineDynamicStateCreateInfo
 * @author Sarge
 */
public class DynamicStateStage {
	private final Set<VkDynamicState> states = new HashSet<>();

	/**
	 * Adds a dynamic state.
	 * @param state Dynamic state
	 */
	public DynamicStateStage state(VkDynamicState state) {
		states.add(requireNonNull(state));
		return this;
	}

	/**
	 * @return Dynamic state descriptor
	 */
	VkPipelineDynamicStateCreateInfo descriptor() {
		// Ignore if no dynamic state
		if(states.isEmpty()) {
			return null;
		}

		// Init descriptor
		final var info = new VkPipelineDynamicStateCreateInfo();
		info.flags = 0;

		// Populate dynamic states
   		info.dynamicStateCount = states.size();
    	info.pDynamicStates = states.stream().mapToInt(VkDynamicState::value).toArray();

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
		void vkCmdSetViewport(CommandBuffer commandBuffer, int firstViewport, int viewportCount, VkViewport pViewports);

		/**
		 * Set dynamic scissor rectangles.
		 * @param commandBuffer			Command buffer
		 * @param firstScissor			Index of the first scissor
		 * @param scissorCount			Number of scissors
		 * @param pScissors				Dynamic scissors
		 */
		void vkCmdSetScissor(CommandBuffer commandBuffer, int firstScissor, int scissorCount, VkRect2D pScissors);

		/**
		 * Sets the line width.
		 * @param commandBuffer			Command buffer
		 * @param lineWidth				Line width
		 */
		void vkCmdSetLineWidth(CommandBuffer commandBuffer, float lineWidth);

		// TODO
		void vkCmdSetDepthBias(CommandBuffer commandBuffer, float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor);

		/**
		 * Sets the colour blend constants.
		 * @param commandBuffer			Command buffer
		 * @param blendConstants		Blend constants
		 */
		void vkCmdSetBlendConstants(CommandBuffer commandBuffer, float blendConstants[]);

		/**
		 * Sets the bounds for the depth test.
		 * @param commandBuffer			Command buffer
		 * @param minDepthBounds		Minimum depth
		 * @param maxDepthBounds		Maximum depth
		 */
		void vkCmdSetDepthBounds(CommandBuffer commandBuffer, float minDepthBounds, float maxDepthBounds);

		/**
		 * Sets the stencil compare mask.
		 * @param commandBuffer			Command buffer
		 * @param faceMask				Face flags
		 * @param compareMask			Compare mask
		 */
		void vkCmdSetStencilCompareMask(CommandBuffer commandBuffer, EnumMask<VkStencilFaceFlag> faceMask, int compareMask);

		/**
		 * Sets the stencil write mask.
		 * @param commandBuffer			Command buffer
		 * @param faceMask				Face flags
		 * @param writeMask				Write mask
		 */
		void vkCmdSetStencilWriteMask(CommandBuffer commandBuffer, EnumMask<VkStencilFaceFlag> faceMask, int writeMask);

		/**
		 * Sets the stencil reference.
		 * @param commandBuffer			Command buffer
		 * @param faceMask				Face flags
		 * @param reference				Reference value
		 */
		void vkCmdSetStencilReference(CommandBuffer commandBuffer, EnumMask<VkStencilFaceFlag> faceMask, int reference);
	}
}
