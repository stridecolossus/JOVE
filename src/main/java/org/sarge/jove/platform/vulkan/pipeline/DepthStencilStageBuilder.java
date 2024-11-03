package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;
import org.sarge.jove.util.BitMask;
import static org.sarge.lib.Validation.*;

/**
 * Builder for the depth-stencil pipeline stage.
 * @author Sarge
 */
public class DepthStencilStageBuilder extends AbstractStageBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	DepthStencilStageBuilder() {
		compare(VkCompareOp.LESS_OR_EQUAL);
	}

	/**
	 * Enables the <i>depth test</i>.
	 */
	public DepthStencilStageBuilder enable() {
		info.depthTestEnable = true;
		return this;
	}

	/**
	 * Enables the <i>depth bounds</i> test.
	 * @param min Minimum depth bound
	 * @param max Maximum depth bound
	 * @see #bounds()
	 */
	@RequiredFeature(field="none", feature="depthBounds")
	@RequiredFeature(field="min", feature="VK_EXT_depth_range_unrestricted")		// TODO - extension
	@RequiredFeature(field="max", feature="VK_EXT_depth_range_unrestricted")
	public DepthStencilStageBuilder bounds(float min, float max) {
		info.depthBoundsTestEnable = true;
		info.minDepthBounds = min;
		info.maxDepthBounds = max;
		return this;
	}

	/**
	 * Enables the <i>depth bounds</i> test with default min/max bounds.
	 * @see #bounds(float, float)
	 */
	public DepthStencilStageBuilder bounds() {
		return bounds(0, 1);
	}

	/**
	 * Creates a command to dynamically configure the <i>depth bounds</i> tests.
	 * @param min Minimum depth bound
	 * @param max Maximum depth bound
	 * @return Dynamic depth bounds command
	 */
	public Command setDynamicDepthBounds(float min, float max) {
		// TODO - validation
		return (lib, buffer) -> lib.vkCmdSetDepthBounds(buffer, min, max);
	}

	/**
	 * Sets the comparison function for the depth test (default is {@link VkCompareOp#LESS_OR_EQUAL}).
	 * @param depthCompareOp Comparison function
	 */
	public DepthStencilStageBuilder compare(VkCompareOp depthCompareOp) {
		info.depthCompareOp = requireNonNull(depthCompareOp);
		return this;
	}

	/**
	 * Enables the <i>stencil test</i>.
	 * @param front 	Front parameters
	 * @param back 		Back parameters
	 */
	public DepthStencilStageBuilder stencil(VkStencilOpState front, VkStencilOpState back) {
		info.stencilTestEnable = true;
		info.front = requireNonNull(front);
		info.back = requireNonNull(back);
		return this;
	}
	// TODO - front/back defaults?

	/**
	 * Stencil masks.
	 */
	public enum StencilMaskType {
		COMPARE,
		WRITE,
		REFERENCE
	}

	/**
	 * Builder for a {@link VkStencilOpState} descriptor.
	 */
	public static class StencilStateBuilder {
		private final VkStencilOpState state = new VkStencilOpState();

		/**
		 * Constructor.
		 */
		public StencilStateBuilder() {
			fail(VkStencilOp.KEEP);
			pass(VkStencilOp.KEEP);
			depthFail(VkStencilOp.KEEP);
		}

		/**
		 * Sets the action for samples that fail the stencil test.
		 * @param op Fail operation
		 */
		public StencilStateBuilder fail(VkStencilOp op) {
			state.failOp = requireNonNull(op);
			return this;
		}

		/**
		 * Sets the action for samples that pass both the depth and stencil tests.
		 * @param op Pass operation
		 */
		public StencilStateBuilder pass(VkStencilOp op) {
			state.passOp = requireNonNull(op);
			return this;
		}

		/**
		 * Sets the action for samples that pass the stencil test but fail the depth test.
		 * @param op Depth-fail operation
		 */
		public StencilStateBuilder depthFail(VkStencilOp op) {
			state.depthFailOp = requireNonNull(op);
			return this;
		}

		/**
		 * Sets a stencil mask.
		 * @param type		Stencil mask type
		 * @param mask		Mask
		 */
		public StencilStateBuilder mask(StencilMaskType type, int mask) {
			switch(type) {
    			case COMPARE 	-> state.compareMask = mask;
    			case WRITE 		-> state.writeMask = mask;
    			case REFERENCE	-> state.reference = mask;
			}
			return this;
		}

		/**
		 * Constructs the stencil state descriptor.
		 * @return Stencil state
		 */
		public VkStencilOpState build() {
			return state;
		}
	}

	/**
	 * Creates a command to dynamically configure a stencil mask.
	 * @param mask		Stencil mask type
	 * @param face		Face flags
	 * @param mask		Mask to set
	 * @return Dynamic stencil compare command
	 * @throws IllegalArgumentException if {@link #face} is empty
	 */
	public Command setDynamicStencilCompareMask(StencilMaskType type, Set<VkStencilFaceFlag> face, int mask) {
		requireNotEmpty(face);
		final var faceMask = new BitMask<>(face);
		return (lib, buffer) -> {
			switch(type) {
    			case COMPARE 	-> lib.vkCmdSetStencilCompareMask(buffer, faceMask, mask);
    			case WRITE 		-> lib.vkCmdSetStencilWriteMask(buffer, faceMask, mask);
    			case REFERENCE 	-> lib.vkCmdSetStencilReference(buffer, faceMask, mask);
			}
		};
	}

	/**
	 * Enables <i>depth writes</i>.
	 * @throws IllegalStateException if the depth test is not enabled
	 * @see #enable()
	 */
	public DepthStencilStageBuilder write() {
		if(!info.depthTestEnable) throw new IllegalStateException();
		info.depthWriteEnable = true;
		return this;
	}

	@Override
	VkPipelineDepthStencilStateCreateInfo get() {
		return info;
	}
}
