package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * Builder for the rasterizer pipeline stage.
 * @author Sarge
 */
public class RasterizerStage {
	private final VkPipelineRasterizationStateCreateInfo info = new VkPipelineRasterizationStateCreateInfo();

	public RasterizerStage() {
		depthClamp(false);
		discard(false);
		polygon(VkPolygonMode.FILL);
		cull(VkCullModeFlags.BACK);
		winding(VkFrontFace.COUNTER_CLOCKWISE);
		lineWidth(1);
	}

	/**
	 * @return Descriptor for this stage
	 */
	VkPipelineRasterizationStateCreateInfo descriptor() {
		return info;
	}

	/**
	 * Sets whether fragment depth values are clamped.
	 * @param depthClampEnable Whether to clamp depth values
	 */
	public RasterizerStage depthClamp(boolean depthClampEnable) {
		info.depthClampEnable = depthClampEnable;
		return this;
	}

	/**
	 * Sets whether geometry is discarded by the rasterizer.
	 * @param rasterizerDiscardEnable Whether to discard geometry
	 */
	public RasterizerStage discard(boolean rasterizerDiscardEnable) {
		info.rasterizerDiscardEnable = rasterizerDiscardEnable;
		return this;
	}

	/**
	 * Sets the polygon fill mode.
	 * Default is {@link VkPolygonMode#FILL}.
	 * @param polygonMode Polygon mode
	 */
	public RasterizerStage polygon(VkPolygonMode polygonMode) {
		info.polygonMode = requireNonNull(polygonMode);
		return this;
	}

	/**
	 * Sets the face culling mode.
	 * Default is {@link VkCullMode#BACK}.
	 * @param cullMode Face culling mode
	 */
	public RasterizerStage cull(VkCullModeFlags cullMode) {
		info.cullMode = new EnumMask<>(cullMode);
		return this;
	}

	/**
	 * Sets the vertex winding order for front-facing faces.
	 * Default is {@link VkFrontFace#COUNTER_CLOCKWISE}.
	 * @param frontFace Winding order
	 */
	public RasterizerStage winding(VkFrontFace frontFace) {
		info.frontFace = requireNonNull(frontFace);
		return this;
	}

	/**
	 * Enables depth value bias (disabled by default).
	 * @param depthBiasConstantFactor
	 * @param depthBiasClamp
	 * @param depthBiasSlopeFactor
	 */
	public RasterizerStage bias(float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor) {
//		// TODO - range check?
//		this.depthBiasEnable = VulkanBoolean.TRUE;
//		info.depthBiasConstantFactor = depthBiasConstantFactor;
//		info.depthBiasClamp = depthBiasClamp;
//		info.depthBiasSlopeFactor = depthBiasSlopeFactor;
//		return this;
		throw new UnsupportedOperationException();
	}
	// TODO - also dynamic API

	/**
	 * Sets the line width.
	 * @param lineWidth Line width
	 * @throws IllegalArgumentException if the line width is less than one
	 */
	public RasterizerStage lineWidth(float lineWidth) {
		info.lineWidth = requireOneOrMore(lineWidth);
		return this;
	}
}

//
//	/**
//	 * Creates a command to dynamically set the line width.
//	 * @param w Line width
//	 * @return Dynamic line width command
//	 */
//	public Command setDynamicLineWidth(float width) {
//		requireOneOrMore(width);
////		return (lib, cmd) -> lib.vkCmdSetLineWidth(cmd, w);
//		return null;
//	}
//}
