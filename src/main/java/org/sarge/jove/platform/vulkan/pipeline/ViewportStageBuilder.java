package org.sarge.jove.platform.vulkan.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkExtent2D;
import org.sarge.jove.platform.vulkan.VkOffset2D;
import org.sarge.jove.platform.vulkan.VkPipelineViewportStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.VkViewport;
import org.sarge.jove.util.StructureHelper;

/**
 * Builder for the viewport stage descriptor.
 */
public class ViewportStageBuilder extends AbstractPipelineStageBuilder<VkPipelineViewportStateCreateInfo> {
	private final List<VkViewport> viewports = new ArrayList<>();
	private final List<VkRect2D> scissors = new ArrayList<>();

	/**
	 * Adds a viewport.
	 * @param viewport 		Viewport rectangle
	 * @param min			Minimum depth
	 * @param max			Maximum depth
	 */
	public ViewportStageBuilder viewport(Rectangle rect, float min, float max) {
		// Init viewport rectangle
		final VkViewport viewport = new VkViewport();
		viewport.x = rect.x();
		viewport.y = rect.y();
		viewport.width = rect.width();
		viewport.height = rect.height();

		// Init min/max depth
		viewport.minDepth = min;
		viewport.maxDepth = max;

		// Add viewport
		viewports.add(viewport);

		return this;
	}

	/**
	 * Adds a viewport with default min/max depth.
	 * @param viewport Viewport rectangle
	 */
	public ViewportStageBuilder viewport(Rectangle viewport) {
		return viewport(viewport, 0, 1);
	}

	/**
	 * Adds a scissor rectangle.
	 * @param scissor Scissor rectangle
	 */
	public ViewportStageBuilder scissor(Rectangle scissor) {
		// Copy offset
		final VkOffset2D offset = new VkOffset2D();
		offset.x = scissor.x();
		offset.y = scissor.y();

		// Copy extent
		final VkExtent2D extent = new VkExtent2D();
		extent.width = scissor.width();
		extent.width = scissor.height();

		// Create rectangle
		final VkRect2D rect = new VkRect2D();
		rect.offset = offset;
		rect.extent = extent;

		// Add scissor rectangle
		scissors.add(rect);

		return this;
	}

	/**
	 * Constructs this viewport stage.
	 * @return New viewport stage
	 */
	@Override
	protected VkPipelineViewportStateCreateInfo result() {
		// Add viewports
		final VkPipelineViewportStateCreateInfo info = new VkPipelineViewportStateCreateInfo();
		if(viewports.isEmpty()) throw new IllegalArgumentException("No viewports specified");
		info.pViewports = StructureHelper.structures(viewports);
		info.viewportCount = viewports.size();

		// Add scissors
		if(scissors.isEmpty()) throw new IllegalArgumentException("No scissor rectangles specified");
		info.pScissors = StructureHelper.structures(scissors);
		info.scissorCount = scissors.size();

		return info;
	}
}
