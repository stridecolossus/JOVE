package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;

/**
 * Builder for the rasterizer pipeline stage.
 * @author Sarge
 */
public class RasterizerStageBuilder extends AbstractStageBuilder<VkPipelineRasterizationStateCreateInfo> {
	private VkPipelineRasterizationStateCreateInfo info = new VkPipelineRasterizationStateCreateInfo();

	public RasterizerStageBuilder() {
		depthClamp(false);
		discard(false);
		polygon(VkPolygonMode.FILL);
		cull(VkCullMode.BACK);
		winding(VkFrontFace.COUNTER_CLOCKWISE);
		lineWidth(1);
	}

	/**
	 * Sets whether fragment depth values are clamped (default is {@code false})
	 * @param depthClampEnable Whether to clamp depth values
	 */
	public RasterizerStageBuilder depthClamp(boolean depthClampEnable) {
		info.depthClampEnable = depthClampEnable;
		return this;
	}

	/**
	 * Sets whether geometry is discarded by the rasterizer (default is {@code false}).
	 * @param rasterizerDiscardEnable Whether to discard geometry
	 */
	public RasterizerStageBuilder discard(boolean rasterizerDiscardEnable) {
		info.rasterizerDiscardEnable = rasterizerDiscardEnable;
		return this;
	}

	/**
	 * Sets the polygon fill mode (default is {@link VkPolygonMode#FILL}).
	 * @param polygonMode Polygon mode
	 */
	@RequiredFeature(field="polygonMode", feature="fillModeNonSolid")
	public RasterizerStageBuilder polygon(VkPolygonMode polygonMode) {
		info.polygonMode = requireNonNull(polygonMode);
		return this;
	}

	/**
	 * Sets the face culling mode (default is {@link VkCullMode#BACK}).
	 * @param cullMode Face culling mode
	 */
	public RasterizerStageBuilder cull(VkCullMode cullMode) {
		info.cullMode = requireNonNull(cullMode);
		return this;
	}

	/**
	 * Sets the vertex winding order for front-facing faces (default is {@link VkFrontFace#COUNTER_CLOCKWISE}).
	 * @param frontFace Winding order
	 */
	public RasterizerStageBuilder winding(VkFrontFace frontFace) {
		info.frontFace = requireNonNull(frontFace);
		return this;
	}

	/**
	 * Enables depth value bias (disabled by default).
	 * @param depthBiasConstantFactor
	 * @param depthBiasClamp
	 * @param depthBiasSlopeFactor
	 */
	public RasterizerStageBuilder bias(float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor) {
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
	 * Sets the line width (default is {@code one}).
	 * @param lineWidth Line width
	 * @throws IllegalArgumentException if the line width is less-than one
	 */
	@RequiredFeature(field="lineWidth", feature="wideLines")
	public RasterizerStageBuilder lineWidth(float lineWidth) {
		info.lineWidth = requireOneOrMore(lineWidth);
		return this;
	}

	@Override
	VkPipelineRasterizationStateCreateInfo get() {
		return info;
	}

	/**
	 * Creates a command to dynamically set the line width.
	 * @param w Line width
	 * @return Dynamic line width command
	 */
	@RequiredFeature(field="lineWidth", feature="wideLines")
	public Command setDynamicLineWidth(float w) {
		requireOneOrMore(w);
		return (lib, cmd) -> lib.vkCmdSetLineWidth(cmd, w);
	}
}
