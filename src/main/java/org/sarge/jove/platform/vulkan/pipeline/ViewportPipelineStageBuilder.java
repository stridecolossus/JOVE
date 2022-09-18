package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;
import org.sarge.jove.util.StructureCollector;
import org.sarge.lib.util.*;

/**
 * Builder for the viewport stage descriptor.
 * @author Sarge
 */
public class ViewportPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineViewportStateCreateInfo> {
	/**
	 * Transient viewport descriptor.
	 */
	private record Viewport(Rectangle rect, Percentile min, Percentile max) {
		private Viewport {
			Check.notNull(rect);
			Check.notNull(min);
			Check.notNull(max);
		}
	}

	private final List<Viewport> viewports = new ArrayList<>();
	private final List<Rectangle> scissors = new ArrayList<>();
	private boolean flip;

	/**
	 * Sets whether to flip viewport rectangles (default is {@code true}).
	 * <p>
	 * This method is used to over-ride the default behaviour for Vulkan where the Y axis points <b>down</b> by default.
	 * @see <a href="https://www.saschawillems.de/blog/2019/03/29/flipping-the-vulkan-viewport/">article</a>
	 * <p>
	 * @param flip Whether to flip viewports
	 */
	public ViewportPipelineStageBuilder flip(boolean flip) {
		this.flip = flip;
		return this;
	}

	/**
	 * Adds a viewport rectangle.
	 * @param rect		Viewport rectangle
	 * @param min		Minimum depth
	 * @param max		Maximum depth
	 */
	@RequiredFeature(field="viewportCount", feature="multiViewport")
	public ViewportPipelineStageBuilder viewport(Rectangle rect, Percentile min, Percentile max) {
		viewports.add(new Viewport(rect, min, max));
		return this;
	}

	/**
	 * Adds a viewport rectangle with default min/max depth.
	 * @param rect Viewport rectangle
	 */
	public ViewportPipelineStageBuilder viewport(Rectangle rect) {
		return viewport(rect, Percentile.ZERO, Percentile.ONE);
	}

	/**
	 * Adds a scissor rectangle.
	 * @param rect Scissor rectangle
	 */
	public ViewportPipelineStageBuilder scissor(Rectangle rect) {
		scissors.add(notNull(rect));
		return this;
	}

	/**
	 * Populates a viewport descriptor.
	 */
	private void populate(Viewport viewport, VkViewport info) {
		// Populate viewport rectangle
		final Rectangle rect = viewport.rect;
		info.x = rect.x();
		info.width = rect.width();
		if(flip) {
			info.y = rect.y() + rect.height();
			info.height = -rect.height();
		}
		else {
			info.y = rect.y();
			info.height = rect.height();
		}

		// Init min/max depth
		info.minDepth = viewport.min.floatValue();
		info.maxDepth = viewport.max.floatValue();
	}

	/**
	 * Populates a Vulkan rectangle.
	 * @param in		Rectangle
	 * @param out		Vulkan rectangle
	 */
	private static void populate(Rectangle in, VkRect2D out) {
		out.offset.x = in.x();
		out.offset.y = in.y();
		out.extent.width = in.width();
		out.extent.height = in.height();
	}

	@Override
	VkPipelineViewportStateCreateInfo get() {
		// Validate
		final int count = viewports.size();
		if(count == 0) throw new IllegalArgumentException("No viewports specified");
		if(scissors.size() != count) throw new IllegalArgumentException("Number of scissors must be the same as the number of viewports");

		// Add viewports
		final var info = new VkPipelineViewportStateCreateInfo();
		info.viewportCount = count;
		info.pViewports = StructureCollector.pointer(viewports, new VkViewport(), this::populate);

		// Add scissors
		info.scissorCount = count;
		info.pScissors = StructureCollector.pointer(scissors, new VkRect2D.ByReference(), ViewportPipelineStageBuilder::populate);

		return info;
	}
}
