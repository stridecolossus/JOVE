package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.jove.platform.vulkan.VkCullMode;
import org.sarge.jove.platform.vulkan.VkFrontFace;
import org.sarge.jove.platform.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPolygonMode;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

/**
 * Builder for the rasterizer pipeline stage.
 * @see VkPipelineRasterizationStateCreateInfo
 * @author Sarge
 */
public class RasterizerPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineRasterizationStateCreateInfo> {
	private VkPipelineRasterizationStateCreateInfo info = new VkPipelineRasterizationStateCreateInfo();

	public RasterizerPipelineStageBuilder() {
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
	 * TODO - check feature
	 */
	public RasterizerPipelineStageBuilder depthClamp(boolean depthClampEnable) {
		info.depthClampEnable = VulkanBoolean.of(depthClampEnable);
		return this;
	}

	/**
	 * Sets whether geometry is discarded by the rasterizer (default is {@code false}).
	 * @param rasterizerDiscardEnable Whether to discard geometry
	 */
	public RasterizerPipelineStageBuilder discard(boolean rasterizerDiscardEnable) {
		info.rasterizerDiscardEnable = VulkanBoolean.of(rasterizerDiscardEnable);
		return this;
	}

	/**
	 * Sets the polygon fill mode (default is {@link VkPolygonMode#FILL}).
	 * @param polygonMode Polygon mode
	 * TODO - check feature if not fill, line, point
	 */
	public RasterizerPipelineStageBuilder polygon(VkPolygonMode polygonMode) {
		info.polygonMode = notNull(polygonMode);
		return this;
	}

	/**
	 * Sets the face culling mode (default is {@link VkCullMode#BACK}).
	 * @param cullMode Face culling mode
	 */
	public RasterizerPipelineStageBuilder cull(VkCullMode cullMode) {
		info.cullMode = notNull(cullMode);
		return this;
	}

	/**
	 * Sets the vertex winding order for front-facing faces (default is {@link VkFrontFace#COUNTER_CLOCKWISE}).
	 * @param frontFace Winding order
	 */
	public RasterizerPipelineStageBuilder winding(VkFrontFace frontFace) {
		info.frontFace = notNull(frontFace);
		return this;
	}

	/**
	 * Enables depth value bias (disabled by default).
	 * @param depthBiasConstantFactor
	 * @param depthBiasClamp
	 * @param depthBiasSlopeFactor
	 */
	public RasterizerPipelineStageBuilder bias(float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor) {
//		// TODO - range check?
//		this.depthBiasEnable = VulkanBoolean.TRUE;
//		info.depthBiasConstantFactor = depthBiasConstantFactor;
//		info.depthBiasClamp = depthBiasClamp;
//		info.depthBiasSlopeFactor = depthBiasSlopeFactor;
//		return this;
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the line width (default is {@code one}).
	 * @param lineWidth Line width
	 * @throws IllegalArgumentException if the line width is less-than one
	 * TODO - check feature if > 1
	 */
	public RasterizerPipelineStageBuilder lineWidth(float lineWidth) {
		info.lineWidth = oneOrMore(lineWidth);
		return this;
	}

	@Override
	VkPipelineRasterizationStateCreateInfo get() {
		return info;
	}
}
