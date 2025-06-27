package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;
import org.sarge.lib.Percentile;

/**
 * Viewport pipeline stage.
 * @author Sarge
 */
public class ViewportStage {
	/**
	 * Viewport descriptor.
	 */
	public record Viewport(Rectangle rectangle, Percentile min, Percentile max) {
		/**
		 * Constructor.
		 * @param rectangle		Viewport rectangle
		 * @param min			Minimum depth
		 * @param max			Maximum depth
		 * @throws IllegalArgumentException if the {@link #min} depth is less than {@lnk #max}
		 */
		public Viewport {
			requireNonNull(rectangle);
			requireNonNull(min);
			requireNonNull(max);
			if(max.isLessThan(min)) throw new IllegalArgumentException();
		}

		/**
		 * Constructor for a viewport with default depths.
		 * @param rectangle Viewport rectangle
		 */
		public Viewport(Rectangle rectangle) {
			this(rectangle, Percentile.ZERO, Percentile.ONE);
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
		 * @return Viewport descriptor
		 */
		VkViewport populate() {
			final var viewport = new VkViewport();
			viewport.x = rectangle.x();
			viewport.y = rectangle.y();
			viewport.width = rectangle.width();
			viewport.height = rectangle.height();
			viewport.minDepth = min.value();
			viewport.maxDepth = max.value();
			return viewport;
		}
	}

	private final List<Viewport> viewports = new ArrayList<>();
	private final List<Rectangle> scissors = new ArrayList<>();

	/**
	 * Adds a viewport.
	 * @param viewport Viewport to add
	 */
	@RequiredFeature(field="viewportCount", feature="multiViewport")
	public ViewportStage viewport(Viewport viewport) {
		viewports.add(viewport);
		return this;
	}

	/**
	 * Adds a scissor rectangle.
	 * @param rect Scissor rectangle
	 */
	public ViewportStage scissor(Rectangle rect) {
		scissors.add(rect);
		return this;
	}

	/**
	 * Helper for the common case of a viewport and scissor rectangle with the same dimensions.
	 * @param viewport Viewport
	 */
	public ViewportStage viewportAndScissor(Viewport viewport) {
		viewports.add(viewport);
		scissor(viewport.rectangle);
		return this;
	}

	/**
	 * @return Viewport stage descriptor
	 */
	VkPipelineViewportStateCreateInfo descriptor() {
		// Validate
		final int count = viewports.size();
		if(count == 0) throw new IllegalArgumentException("No viewports specified");
		if(scissors.size() != count) throw new IllegalArgumentException("Number of scissors must be the same as the number of viewports");

		// Init descriptor
		final var info = new VkPipelineViewportStateCreateInfo();
		info.flags = 0;

		// Add viewports
		info.viewportCount = count;
		info.pViewports = viewports.stream().map(Viewport::populate).toArray(VkViewport[]::new);

		// Add scissors
		info.scissorCount = count;
		info.pScissors = scissors.stream().map(ViewportStage::convert).toArray(VkRect2D[]::new);

		return info;
	}

	/**
	 * @return Descriptor for the given scissor rectangle
	 */
	private static VkRect2D convert(Rectangle rect) {
		final var offset = new VkOffset2D();
		offset.x = rect.x();
		offset.y = rect.y();

		final var extent = new VkExtent2D();
		extent.width = rect.width();
		extent.height = rect.height();

		final VkRect2D descriptor = new VkRect2D();
		descriptor.offset = offset;
		descriptor.extent = extent;

		return descriptor;
	}
}

//	/**
//	 * Creates a dynamic viewport command.
//	 * @param start			First viewport
//	 * @param viewports		Dynamic viewports
//	 * @return Dynamic viewport command
//	 * @throws IllegalArgumentException if {@link #viewports} is empty or the range is out-of-bounds for this pipeline
//	 */
//	@RequiredFeature(field="start", feature="multiViewport")
//	public Command setDynamicViewport(int start, List<Viewport> viewports) {
//		validate(start, viewports);
//		final VkViewport array = null; // TODO viewports(viewports);
//		return (lib, buffer) -> lib.vkCmdSetViewport(buffer, start, viewports.size(), array);
//	}
//
//	/**
//	 * Creates a dynamic scissor rectangle command.
//	 * @param start			First scissor rectangle
//	 * @param scissors		Dynamic scissors
//	 * @return Dynamic scissors command
//	 * @throws IllegalArgumentException if {@link #scissors} is empty or the range is out-of-bounds for this pipeline
//	 */
//	@RequiredFeature(field="start", feature="multiViewport")
//	public Command setDynamicScissor(int start, List<Rectangle> scissors) {
//		validate(start, scissors);
//		final VkRect2D array = null; // TODO scissors(scissors);
//		return (lib, buffer) -> lib.vkCmdSetScissor(buffer, start, scissors.size(), array);
//	}
//
//	/**
//	 * @throws IllegalArgumentException for an invalid dynamic state command
//	 */
//	private void validate(int start, List<?> list) {
//		requireZeroOrMore(start);
//		requireNotEmpty(list);
//		if(start + list.size() > viewports.size()) throw new IllegalArgumentException("Invalid viewport/scissor range");
//	}
//}
