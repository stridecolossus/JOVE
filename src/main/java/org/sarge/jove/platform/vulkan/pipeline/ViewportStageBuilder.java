package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkPipelineViewportStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.VkViewport;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Percentile;

/**
 * Builder for the viewport stage descriptor.
 * @see VkPipelineViewportStateCreateInfo
 * @author Sarge
 */
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
	/**
	 * Transient viewport descriptor.
	 */
	private record Viewport(Rectangle rect, Percentile min, Percentile max) {
		private Viewport {
			Check.notNull(rect);
			Check.notNull(min);
			Check.notNull(max);
		}

		private void populate(VkViewport viewport, boolean flip) {
			// Populate viewport rectangle
			if(flip) {
				viewport.x = rect.x();
				viewport.y = rect.y() + rect.height();
				viewport.width = rect.width();
				viewport.height = -rect.height();
			}
			else {
				viewport.x = rect.x();
				viewport.y = rect.y();
				viewport.width = rect.width();
				viewport.height = rect.height();
			}

			// Init min/max depth
			viewport.minDepth = min.floatValue();
			viewport.maxDepth = max.floatValue();
		}
	}

	private final List<Viewport> viewports = new ArrayList<>();
	private final List<Rectangle> scissors = new ArrayList<>();

	private boolean flip;

	/**
	 * Sets whether to flip viewport rectangles (default is {@code false}).
	 * <p>
	 * This method is used to over-ride the default behaviour for Vulkan where the Y axis is positive in the <b>down</b> direction.
	 * @see <a href="https://www.saschawillems.de/blog/2019/03/29/flipping-the-vulkan-viewport/">article</a>
	 * <p>
	 * @param flip Whether to flip viewports
	 */
	public ViewportStageBuilder flip(boolean flip) {
		this.flip = flip;
		return this;
	}

	/**
	 * Adds a viewport rectangle.
	 * @param viewport 		Viewport rectangle
	 * @param min			Minimum depth
	 * @param max			Maximum depth
	 */
	public ViewportStageBuilder viewport(Rectangle rect, Percentile min, Percentile max) {
		viewports.add(new Viewport(rect, min, max));
		return this;
	}

	/**
	 * Adds a viewport rectangle with default min/max depth.
	 * @param viewport Viewport rectangle
	 */
	public ViewportStageBuilder viewport(Rectangle viewport) {
		return viewport(viewport, Percentile.ZERO, Percentile.ONE);
	}

	/**
	 * Adds a scissor rectangle.
	 * @param rect Scissor rectangle
	 */
	public ViewportStageBuilder scissor(Rectangle rect) {
		scissors.add(notNull(rect));
		return this;
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
		info.pViewports = StructureHelper.first(viewports, VkViewport::new, (rec, viewport) -> rec.populate(viewport, flip));

		// Add scissors
		info.scissorCount = count;
		info.pScissors = StructureHelper.first(scissors, VkRect2D.ByReference::new, VulkanHelper::populate);

		return info;
	}
}
