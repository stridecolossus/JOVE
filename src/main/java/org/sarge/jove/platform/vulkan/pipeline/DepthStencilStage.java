package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.platform.vulkan.*;

/**
 * The <i>depth stencil</i> pipeline stage configures the optional depth-stencil attachment.
 * @author Sarge
 */
public class DepthStencilStage {
	private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	DepthStencilStage() {
		compare(VkCompareOp.LESS_OR_EQUAL);
	}

	/**
	 * Enables the <i>depth test</i>.
	 */
	public DepthStencilStage enable() {
		info.depthTestEnable = true;
		return this;
	}

	/**
	 * Enables the <i>depth bounds</i> test.
	 * @param min Minimum depth bound
	 * @param max Maximum depth bound
	 * @throws IllegalArgumentException if {@link #min} is not less than or equal to {@link #max}
	 */
	public DepthStencilStage bounds(float min, float max) {
		if(min > max) throw new IllegalArgumentException();
		info.depthBoundsTestEnable = true;
		info.minDepthBounds = min;
		info.maxDepthBounds = max;
		return this;
	}
	// TODO - should these be percentile?

	/**
	 * Enables the <i>depth bounds</i> test with default min/max bounds.
	 * @see #bounds(float, float)
	 */
	public DepthStencilStage bounds() {
		return bounds(0, 1);
	}

//	/**
//	 * Creates a command to dynamically configure the <i>depth bounds</i> tests.
//	 * @param min Minimum depth bound
//	 * @param max Maximum depth bound
//	 * @return Dynamic depth bounds command
//	 */
//	public Command setDynamicDepthBounds(float min, float max) {
//		// TODO - validation
//		return (lib, buffer) -> lib.vkCmdSetDepthBounds(buffer, min, max);
//	}

	/**
	 * Sets the comparison function for the depth test (default is {@link VkCompareOp#LESS_OR_EQUAL}).
	 * @param depthCompareOp Comparison function
	 */
	public DepthStencilStage compare(VkCompareOp depthCompareOp) {
		info.depthCompareOp = requireNonNull(depthCompareOp);
		return this;
	}

	/**
	 * Enables the <i>stencil test</i>.
	 * @param front 	Front parameters
	 * @param back 		Back parameters
	 * @see StencilStateBuilder
	 */
	public DepthStencilStage stencil(VkStencilOpState front, VkStencilOpState back) {
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

//	/**
//	 * Creates a command to dynamically configure a stencil mask.
//	 * @param mask		Stencil mask type
//	 * @param face		Face flags
//	 * @param mask		Mask to set
//	 * @return Dynamic stencil compare command
//	 * @throws IllegalArgumentException if {@link #face} is empty
//	 */
//	public Command setDynamicStencilCompareMask(StencilMaskType type, Set<VkStencilFaceFlag> face, int mask) {
//		requireNotEmpty(face);
//		final var faceMask = new EnumMask<>(face);
//		return (lib, buffer) -> {
//			switch(type) {
//    			case COMPARE 	-> lib.vkCmdSetStencilCompareMask(buffer, faceMask, mask);
//    			case WRITE 		-> lib.vkCmdSetStencilWriteMask(buffer, faceMask, mask);
//    			case REFERENCE 	-> lib.vkCmdSetStencilReference(buffer, faceMask, mask);
//			}
//		};
//	}

	/**
	 * Enables <i>depth writes</i>.
	 * @throws IllegalStateException if the depth test is not enabled
	 * @see #enable()
	 */
	public DepthStencilStage write() {
		if(!info.depthTestEnable) throw new IllegalStateException();
		info.depthWriteEnable = true;
		return this;
	}

	/**
	 * @return Depth-stencil descriptor
	 */
	VkPipelineDepthStencilStateCreateInfo descriptor() {

		info.sType = VkStructureType.PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;

		return info;
	}
	// TODO - NULL if not used/enabled?
}
