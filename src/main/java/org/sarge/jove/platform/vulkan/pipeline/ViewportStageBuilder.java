package org.sarge.jove.platform.vulkan.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkPipelineViewportStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.VkViewport;
import org.sarge.jove.platform.vulkan.util.ExtentHelper;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.StructureHelper;

/**
 * Builder for the viewport stage descriptor.
 */
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
	private final List<VkViewport> viewports = new ArrayList<>();
	private final List<VkRect2D> scissors = new ArrayList<>();

	/**
	 * Adds a viewport.
	 * @param viewport 		Viewport rectangle
	 * @param min			Minimum depth
	 * @param max			Maximum depth
	 * @throws IllegalArgumentException if the min/max values are not in the range 0..1
	 */
	public ViewportStageBuilder viewport(Rectangle rect, float min, float max) {
		// Init viewport rectangle
		final VkViewport viewport = new VkViewport();
		viewport.x = rect.x();
		viewport.y = rect.y();
		viewport.width = rect.width();
		viewport.height = rect.height();

		// Init min/max depth
		viewport.minDepth = Check.isPercentile(min);
		viewport.maxDepth = Check.isPercentile(max);

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
		scissors.add(ExtentHelper.of(scissor));
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
