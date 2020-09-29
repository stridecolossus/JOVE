package org.sarge.jove.platform.vulkan.pipeline;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.common.ScreenCoordinate;
import org.sarge.jove.platform.vulkan.VkPipelineViewportStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.VkViewport;
import org.sarge.jove.util.AbstractBuilder;
import org.sarge.jove.util.StructureHelper;

/**
 * Builder for the viewport stage descriptor.
 */
public class ViewportStageBuilder <R> extends AbstractBuilder<R> {
	private final Deque<VkViewport> viewports = new ArrayDeque<>();
	private final List<VkRect2D> scissors = new ArrayList<>();

	/**
	 * Adds a viewport.
	 * @param rect 		Viewport rectangle
	 * @param min		Minimum depth
	 * @param max		Maximum depth
	 */
	public ViewportStageBuilder<R> viewport(Rectangle rect, float min, float max) {
		final ScreenCoordinate coords = rect.pos();
		final Dimensions dim = rect.size();
		final VkViewport viewport = new VkViewport();
		viewport.x = coords.x();
		viewport.y = coords.y();
		viewport.width = dim.width();
		viewport.height = dim.height();
		viewport.minDepth = min;
		viewport.maxDepth = max;
		viewports.add(viewport);
		return this;
	}

	/**
	 * Adds a viewport with default min/max depth.
	 * @param rect Viewport rectangle
	 */
	public ViewportStageBuilder<R> viewport(Rectangle rect) {
		return viewport(rect, 0, 1);
	}

	/**
	 * Adds a scissor rectangle.
	 * @param rect Scissor rectangle
	 */
	public ViewportStageBuilder<R> scissor(Rectangle rect) {
		scissors.add(new VkRect2D(rect));
		return this;
	}

	// TODO
	// - what does multiple viewports actually do?
	// - have to have same number of scissors as viewports? or none?
	// - add auto method for scissor = last viewport?

	/**
	 * Constructs this viewport stage.
	 * @return New viewport stage
	 */
	protected VkPipelineViewportStateCreateInfo buildLocal() {
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
