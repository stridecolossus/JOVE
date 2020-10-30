package org.sarge.jove.platform.vulkan.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkPipelineViewportStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.VkViewport;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.util.Check;

/**
 * Builder for the viewport stage descriptor.
 */
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
	/**
	 * Transient viewport descriptor.
	 */
	private record Viewport(Rectangle rect, float min, float max, boolean flip) {
		public Viewport {
			Check.notNull(rect);
			Check.isPercentile(min);
			Check.isPercentile(max);
		}

		private void populate(VkViewport viewport) {
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
			viewport.minDepth = Check.isPercentile(min);
			viewport.maxDepth = Check.isPercentile(max);
		}
	}

	private final List<Viewport> viewports = new ArrayList<>();
	private final List<Rectangle> scissors = new ArrayList<>();

	private boolean copy = true;
	private boolean flip;

	/**
	 * Sets whether to create a scissor rectangle for each viewport (default is {@code true}).
	 * @param copy Whether to copy a scissor rectangle for viewports
	 */
	public ViewportStageBuilder setCopyScissor(boolean copy) {
		this.copy = copy;
		return this;
	}

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
	 * Adds a viewport.
	 * @param viewport 		Viewport rectangle
	 * @param min			Minimum depth
	 * @param max			Maximum depth
	 * @throws IllegalArgumentException if the min/max values are not in the range 0..1
	 * @see #setCopyScissor(boolean)
	 */
	public ViewportStageBuilder viewport(Rectangle rect, float min, float max) {
		// Add viewport
		viewports.add(new Viewport(rect, min, max, flip));

		// Add scissor
		if(copy) {
			scissor(rect);
		}

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
	 * @param rect Scissor rectangle
	 */
	public ViewportStageBuilder scissor(Rectangle rect) {
		scissors.add(rect);
		return this;
	}

	@Override
	protected VkPipelineViewportStateCreateInfo result() {
		// Add viewports
		final VkPipelineViewportStateCreateInfo info = new VkPipelineViewportStateCreateInfo();
		if(viewports.isEmpty()) throw new IllegalArgumentException("No viewports specified");
		info.viewportCount = viewports.size();
		info.pViewports = VulkanStructure.array(VkViewport::new, viewports, Viewport::populate);

		// Add scissors
		if(scissors.isEmpty()) throw new IllegalArgumentException("No scissor rectangles specified");
		info.scissorCount = scissors.size();
		info.pScissors = VulkanStructure.array(VkRect2D.ByReference::new, scissors, this::rectangle);

		return info;
	}

	// TODO - helper?
	private void rectangle(Rectangle rect, VkRect2D out) {
		out.offset.x = rect.x();
		out.offset.y = rect.y();
		out.extent.width = rect.width();
		out.extent.height = rect.height();
	}
}
