package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkPipelineViewportStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.VkViewport;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Percentile;

/**
 * Builder for the viewport stage descriptor.
 * @see VkPipelineViewportStateCreateInfo
 * @author Sarge
 */
public class ViewportPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineViewportStateCreateInfo, ViewportPipelineStageBuilder> {
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

	private boolean flip; // = true;

	@Override
	void init(ViewportPipelineStageBuilder builder) {
		viewports.addAll(builder.viewports);
		scissors.addAll(builder.scissors);
		flip = builder.flip;
	}

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
	 * Convenience helper to add a viewport rectangle with default min/max depth <b>and</b> a scissor with the same dimensions.
	 * @param rect Viewport/scissor rectangle
	 */
	public ViewportPipelineStageBuilder viewportScissor(Rectangle rect) {
		viewport(rect);
		scissor(rect);
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
		// TODO - count < limits.maxViewports
		// TODO - count = 1 unless multiple viewports feature

		// Add viewports
		final var info = new VkPipelineViewportStateCreateInfo();
		info.viewportCount = count;
		info.pViewports = StructureHelper.pointer(viewports, VkViewport::new, this::populate);

		// Add scissors
		info.scissorCount = count;
		info.pScissors = StructureHelper.pointer(scissors, VkRect2D.ByReference::new, ViewportPipelineStageBuilder::populate);

		return info;
	}
}
