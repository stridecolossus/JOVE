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
public class RasterizerStageBuilder extends AbstractPipelineBuilder<VkPipelineRasterizationStateCreateInfo> {
	private final VkPipelineRasterizationStateCreateInfo info = new VkPipelineRasterizationStateCreateInfo();

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
	 * TODO - check feature
	 */
	public RasterizerStageBuilder depthClamp(boolean depthClampEnable) {
		info.depthClampEnable = VulkanBoolean.of(depthClampEnable);
		return this;
	}

	/**
	 * Sets whether geometry is discarded by the rasterizer (basically disabling output to the framebuffer).
	 * @param rasterizerDiscardEnable Whether to discard geometry (default is {@code false})
	 */
	public RasterizerStageBuilder discard(boolean rasterizerDiscardEnable) {
		info.rasterizerDiscardEnable = VulkanBoolean.of(rasterizerDiscardEnable);
		return this;
	}

	/**
	 * Sets the polygon fill mode.
	 * @param polygonMode Polygon mode (default is {@link VkPolygonMode#FILL})
	 * TODO - check feature if not fill, line, point
	 */
	public RasterizerStageBuilder polygon(VkPolygonMode polygonMode) {
		info.polygonMode = notNull(polygonMode);
		return this;
	}

	/**
	 * Sets the face culling mode.
	 * @param cullMode Face culling mode (default is {@link VkCullMode#BACK})
	 */
	public RasterizerStageBuilder cull(VkCullMode cullMode) {
		info.cullMode = notNull(cullMode);
		return this;
	}

	/**
	 * Sets the vertex winding order for front-facing faces.
	 * @param frontFace Winding order (default is {@link VkFrontFace#COUNTER_CLOCKWISE})
	 * @see #clockwise(boolean)
	 */
	public RasterizerStageBuilder winding(VkFrontFace frontFace) {
		info.frontFace = notNull(frontFace);
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

	/**
	 * Sets the line width.
	 * @param lineWidth Line width (default is {@code one})
	 * @throws IllegalArgumentException if the line width is less-than one
	 * TODO - check feature if > 1
	 */
	public RasterizerStageBuilder lineWidth(float lineWidth) {
		info.lineWidth = oneOrMore(lineWidth);
		return this;
	}

	@Override
	VkPipelineRasterizationStateCreateInfo get() {
		return info;
	}
}
