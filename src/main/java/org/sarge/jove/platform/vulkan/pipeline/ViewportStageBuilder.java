package org.sarge.jove.platform.vulkan.pipeline;

import java.util.*;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;
import org.sarge.jove.util.StructureCollector;
import org.sarge.lib.util.*;

/**
 * Builder for the viewport pipeline stage.
 * Note that the number of scissor rectangles <b>must</b> match the number of viewports.
 * @author Sarge
 */
public class ViewportStageBuilder extends AbstractStageBuilder<VkPipelineViewportStateCreateInfo> {
	/**
	 * Viewport descriptor.
	 */
	public record Viewport(Rectangle rectangle, Percentile min, Percentile max) {
		/**
		 * Constructor.
		 * @param rectangle		Viewport rectangle
		 * @param min			Minimum depth
		 * @param max			Maximum depth
		 */
		public Viewport {
			Check.notNull(rectangle);
			Check.notNull(min);
			Check.notNull(max);
		}

		/**
		 * Constructor for a viewport with default depths.
		 * @param rect Viewport rectangle
		 */
		public Viewport(Rectangle rect) {
			this(rect, Percentile.ZERO, Percentile.ONE);
		}

		/**
		 * Flips this viewport vertically.
    	 * This overrides the default behaviour for Vulkan where the Y axis points <b>down</b> by default.
    	 * @see <a href="https://www.saschawillems.de/blog/2019/03/29/flipping-the-vulkan-viewport/">article</a>
		 * @return Flipped viewport
		 */
		public Viewport flip() {
			final int y = rectangle.y() + rectangle.height();
			final int h = - rectangle.height();
			final Rectangle flip = new Rectangle(rectangle.x(), y, rectangle.width(), h);
			return new Viewport(flip, min, max);
		}

		/**
		 * Populates the descriptor for this viewport.
		 */
		void populate(VkViewport info) {
			info.x = rectangle.x();
			info.y = rectangle.y();
			info.width = rectangle.width();
			info.height = rectangle.height();
			info.minDepth = min.floatValue();
			info.maxDepth = max.floatValue();
		}
	}

	private final List<Viewport> viewports = new ArrayList<>();
	private final List<Rectangle> scissors = new ArrayList<>();

	/**
	 * Adds a viewport.
	 * @param viewport Viewport to add
	 */
	@RequiredFeature(field="viewportCount", feature="multiViewport")
	public ViewportStageBuilder viewport(Viewport viewport) {
		Check.notNull(viewport);
		viewports.add(viewport);
		return this;
	}

	/**
	 * Adds a scissor rectangle.
	 * @param rect Scissor rectangle
	 */
	public ViewportStageBuilder scissor(Rectangle rect) {
		Check.notNull(rect);
		scissors.add(rect);
		return this;
	}

	/**
	 * Populates viewport descriptors.
	 */
	private static VkViewport viewports(List<Viewport> viewports) {
		return StructureCollector.pointer(viewports, new VkViewport(), Viewport::populate);
	}

	/**
	 * Populates scissor rectangle descriptors.
	 */
	private static VkRect2D.ByReference scissors(List<Rectangle> scissors) {
		return StructureCollector.pointer(scissors, new VkRect2D.ByReference(), VulkanLibrary::populate);
	}

	@Override
	VkPipelineViewportStateCreateInfo get() {
		// Validate
		final int count = viewports.size();
		if(count == 0) throw new IllegalArgumentException("No viewports specified");
		if(scissors.size() != count) throw new IllegalArgumentException("Number of scissors must be the same as the number of viewports");

		// Init descriptor
		final var info = new VkPipelineViewportStateCreateInfo();
		info.flags = 0;			// Reserved

		// Add viewports
		info.viewportCount = count;
		info.pViewports = viewports(viewports);

		// Add scissors
		info.scissorCount = count;
		info.pScissors = scissors(scissors);

		return info;
	}

	/**
	 * Creates a dynamic viewport command.
	 * @param start			First viewport
	 * @param viewports		Dynamic viewports
	 * @return Dynamic viewport command
	 * @throws IllegalArgumentException if {@link #viewports} is empty or the range is out-of-bounds for this pipeline
	 */
	@RequiredFeature(field="start", feature="multiViewport")
	public Command setDynamicViewport(int start, List<Viewport> viewports) {
		validate(start, viewports);
		final VkViewport array = viewports(viewports);
		return (lib, buffer) -> lib.vkCmdSetViewport(buffer, start, viewports.size(), array);
	}

	/**
	 * Creates a dynamic scissor rectangle command.
	 * @param start			First scissor rectangle
	 * @param scissors		Dynamic scissors
	 * @return Dynamic scissors command
	 * @throws IllegalArgumentException if {@link #scissors} is empty or the range is out-of-bounds for this pipeline
	 */
	@RequiredFeature(field="start", feature="multiViewport")
	public Command setDynamicScissor(int start, List<Rectangle> scissors) {
		validate(start, scissors);
		final VkRect2D array = scissors(scissors);
		return (lib, buffer) -> lib.vkCmdSetScissor(buffer, start, scissors.size(), array);
	}

	/**
	 * @throws IllegalArgumentException for an invalid dynamic state command
	 */
	private void validate(int start, List<?> list) {
		Check.zeroOrMore(start);
		Check.notEmpty(list);
		if(start + list.size() > viewports.size()) throw new IllegalArgumentException("Invalid viewport/scissor range");
	}
}
