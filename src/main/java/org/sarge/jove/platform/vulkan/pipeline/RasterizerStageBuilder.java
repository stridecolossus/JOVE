package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import org.sarge.jove.platform.vulkan.VkCullModeFlag;
import org.sarge.jove.platform.vulkan.VkFrontFace;
import org.sarge.jove.platform.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPolygonMode;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

/**
 * Builder for the rasterizer pipeline stage.
 * @author Sarge
 */
public class RasterizerStageBuilder extends AbstractPipelineStageBuilder<VkPipelineRasterizationStateCreateInfo> {
	private boolean depthClampEnable;
	private boolean rasterizerDiscardEnable;
	private VkPolygonMode polygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL;
	private VkCullModeFlag cullMode = VkCullModeFlag.VK_CULL_MODE_BACK_BIT;
	private VkFrontFace frontFace = VkFrontFace.VK_FRONT_FACE_CLOCKWISE;
	private boolean depthBiasEnable;
	private float depthBiasConstantFactor;
	private float depthBiasClamp;
	private float depthBiasSlopeFactor;
	private float lineWidth = 1;

	/**
	 * Sets whether fragment depth values are clamped (default is <code>false</code>)
	 * @param depthClampEnable Whether to clamp depth values
	 * TODO - check feature
	 */
	public RasterizerStageBuilder depthClampEnable(boolean depthClampEnable) {
		this.depthClampEnable = depthClampEnable;
		return this;
	}

	/**
	 * Sets whether geometry is discarded by the rasterizer (basically disabled output to the framebuffer).
	 * @param rasterizerDiscardEnable Whether to discard geometry (default is <code>false</code>)
	 */
	public RasterizerStageBuilder discardEnable(boolean rasterizerDiscardEnable) {
		this.rasterizerDiscardEnable = rasterizerDiscardEnable;
		return this;
	}

	/**
	 * Sets the polygon fill mode.
	 * @param polygonMode Polygon mode (default is {@link VkPolygonMode#VK_POLYGON_MODE_FILL})
	 * TODO - check feature if not fill, line, point
	 */
	public RasterizerStageBuilder polygonMode(VkPolygonMode polygonMode) {
		this.polygonMode = notNull(polygonMode);
		return this;
	}

	/**
	 * Sets the face culling mode.
	 * @param cullMode Face culling mode (default is {@link VkCullModeFlag#VK_CULL_MODE_BACK_BIT})
	 */
	public RasterizerStageBuilder cullMode(VkCullModeFlag cullMode) {
		this.cullMode = notNull(cullMode);
		return this;
	}

	/**
	 * Sets the vertex order for front-facing faces.
	 * @param clockwise Whether the vertex order is clockwise or counter-clockwise (default is {@link VkFrontFace#VK_FRONT_FACE_CLOCKWISE})
	 */
	public RasterizerStageBuilder frontFace(boolean clockwise) {
		this.frontFace = clockwise ? VkFrontFace.VK_FRONT_FACE_CLOCKWISE : VkFrontFace.VK_FRONT_FACE_COUNTER_CLOCKWISE;
		return this;
	}

	/**
	 * Enables depth value bias (disabled by default).
	 * @param depthBiasConstantFactor
	 * @param depthBiasClamp
	 * @param depthBiasSlopeFactor
	 */
	public RasterizerStageBuilder bias(float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor) {
		// TODO - range check?
		this.depthBiasEnable = true;
		this.depthBiasConstantFactor = depthBiasConstantFactor;
		this.depthBiasClamp = depthBiasClamp;
		this.depthBiasSlopeFactor = depthBiasSlopeFactor;
		return this;
	}

	/**
	 * Sets the line width.
	 * @param lineWidth Line width (default is <code>one</code>)
	 * @throws IllegalArgumentException if the line width is less-than one
	 * TODO - check feature if > 1
	 */
	public RasterizerStageBuilder lineWidth(float lineWidth) {
		this.lineWidth = oneOrMore(lineWidth);
		return this;
	}

	@Override
	protected VkPipelineRasterizationStateCreateInfo result() {
		final var info = new VkPipelineRasterizationStateCreateInfo();
		info.depthClampEnable = VulkanBoolean.of(depthClampEnable);
		info.rasterizerDiscardEnable = VulkanBoolean.of(rasterizerDiscardEnable);
		info.polygonMode = polygonMode;
		info.cullMode = cullMode;
		info.frontFace = frontFace;
		info.depthBiasEnable = VulkanBoolean.of(depthBiasEnable);
		info.depthBiasConstantFactor = depthBiasConstantFactor;
		info.depthBiasClamp = depthBiasClamp;
		info.depthBiasSlopeFactor = depthBiasSlopeFactor;
		info.lineWidth = lineWidth;
		return info;
	}
}
